package com.example.doantotnghiep.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.RADIUS_LIMIT
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.local.enum.Status
import com.example.doantotnghiep.data.local.enum.Trend
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(val floodRepository: FloodRepository) : ViewModel() {
    private val _stationsMap = MutableStateFlow<Map<String, StationMapUiModel>>(emptyMap())

    private val _stationList = MutableStateFlow<List<StationMapUiModel>>(emptyList())
    val stationList: StateFlow<List<StationMapUiModel>> = _stationList

    private var loadDataJob: Job? = null

    init {
        loadAllStation()
    }

    private fun loadAllStation() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            val configList = floodRepository.getAllStations()

            configList.forEach { initialConfig ->
                val stationId = initialConfig.id ?: return@forEach

                launch {
                    var lastTimestamp: Long? = null
                    var lastConfig: StationConfig? = null

                    combine(
                        floodRepository.observeStationConfig(stationId),
                        floodRepository.getRealtimeDatabase(stationId)
                    ) { config, sensorData ->
                        Pair(config ?: initialConfig, sensorData)
                    }.collect { (currentConfig, sensorData) ->
                        if(sensorData != null) {
                            val currentTimestamp = sensorData.timestamp ?: 0L

                            val configChanged = lastConfig != currentConfig
                            lastConfig = currentConfig

                            if (currentTimestamp == lastTimestamp && !configChanged) {
                                return@collect
                            }

                            if (currentTimestamp != lastTimestamp) {
                                lastTimestamp = currentTimestamp
                            }

                            val offset = currentConfig.calibrationOffset ?: 0
                            val rawWaterLevel = (offset - (sensorData.distanceRaw ?: 0)).toDouble()

                            val previousData = _stationsMap.value[stationId]?.sensorData
                            // Nếu cấu hình thay đổi (vd: người dùng hiệu chuẩn), bỏ qua validate bằng cách đặt previousLevel = -1.0
                            val previousLevel = if (configChanged) -1.0 else (previousData?.distanceRaw?.toDouble() ?: -1.0)

                            val validationState = com.example.doantotnghiep.utils.WaterLevelValidator.validate(rawWaterLevel, previousLevel)
                            val isInvalid = validationState != com.example.doantotnghiep.utils.WaterLevelState.VALID

                            if (isInvalid) {
                                val currentMap = _stationsMap.value
                                val existingUiModel = currentMap[stationId]
                                
                                if (existingUiModel != null) {
                                    // Nếu là dữ liệu nhiễu và trạm đã có trên bản đồ, chỉ update môi trường
                                    _stationsMap.update { map ->
                                        val newMap = map.toMutableMap()
                                        newMap[stationId] = existingUiModel.copy(
                                            sensorData = existingUiModel.sensorData.copy(
                                                temp = sensorData.temp,
                                                humid = sensorData.humid,
                                                rainVal = sensorData.rainVal,
                                                timestamp = sensorData.timestamp
                                            )
                                        )
                                        newMap
                                    }
                                    _stationList.value = _stationsMap.value.values.toList()
                                    return@collect
                                }
                                // Nếu trạm chưa xuất hiện (existingUiModel == null), KHÔNG return.
                                // Bắt buộc phải tạo trạm để hiển thị, nếu không người dùng sẽ không thấy trạm để hiệu chuẩn.
                            }

                            val level = if(rawWaterLevel < 0) 0 else rawWaterLevel.toInt()

                            val warning = currentConfig.warningThreshold ?: 0.0
                            val danger = currentConfig.dangerThreshold ?: 0.0
                            val statusStr = when {
                                level >= danger -> Status.DANGER
                                level >= warning -> Status.WARNING
                                else -> Status.SAFE
                            }

                            val uiModel = StationMapUiModel(
                                sensorData = SensorData(sensorData.timestamp, level, sensorData.temp, sensorData.humid, sensorData.rainVal),
                                stationConfig = StationConfig(id = currentConfig.id, name = currentConfig.name, latitude = currentConfig.latitude, longitude = currentConfig.longitude, deviceKey = currentConfig.deviceKey, calibrationOffset = currentConfig.calibrationOffset, warningThreshold = currentConfig.warningThreshold, dangerThreshold = currentConfig.dangerThreshold),
                                trendValue = Trend.STABLE,
                                trendPoints = listOf(10f, 20f, 15f, 30f),
                                status = statusStr,
                                coverageRadius = RADIUS_LIMIT.toDouble(),
                            )

                            _stationsMap.update { currentMap ->
                                val newMap = currentMap.toMutableMap()
                                newMap[stationId] = uiModel
                                newMap
                            }

                            _stationList.value = _stationsMap.value.values.toList()
                        }
                    }
                }
            }
        }
    }

    fun updateStationConfig(
        stationId: String,
        name: String,
        offset: Int,
        warningThreshold: Double,
        dangerThreshold: Double,
        latitude: Double?,
        longitude: Double?,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = floodRepository.updateStationConfig(stationId, name, offset, warningThreshold, dangerThreshold, latitude, longitude)
            
            withContext(Dispatchers.Main) {
                if (success) {
                    _stationsMap.update { currentMap ->
                        val newMap = currentMap.toMutableMap()
                        val existing = newMap[stationId]
                        if (existing != null) {
                            newMap[stationId] = existing.copy(
                                stationConfig = existing.stationConfig.copy(
                                    name = name,
                                    calibrationOffset = offset,
                                    warningThreshold = warningThreshold,
                                    dangerThreshold = dangerThreshold,
                                    latitude = latitude ?: existing.stationConfig.latitude,
                                    longitude = longitude ?: existing.stationConfig.longitude
                                )
                            )
                        }
                        newMap
                    }
                    _stationList.value = _stationsMap.value.values.toList()

                }

                onComplete(success)
            }
        }
    }
}