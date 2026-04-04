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
import kotlinx.coroutines.delay
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
                    var isFirstRead = true
                    var timeoutJob: Job? = null

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

                            if (isFirstRead) {
                                isFirstRead = false
                                lastTimestamp = currentTimestamp
                                // Bỏ qua giá trị đầu tiên vì nó có thể là data cũ (cache)
                                return@collect
                            }

                            // Chỉ bỏ qua nếu CẢ timestamp không đổi VÀ config không đổi
                            if (currentTimestamp == lastTimestamp && !configChanged) {
                                return@collect
                            }

                            // Cập nhật lại thời gian lastTimestamp nếu có dữ liệu IoT mới
                            if (currentTimestamp != lastTimestamp) {
                                lastTimestamp = currentTimestamp
                                // Reset timeout job vì trạm vẫn đang sống
                                timeoutJob?.cancel()
                                timeoutJob = launch {
                                    delay(15000)
                                    _stationsMap.update { currentMap ->
                                        val newMap = currentMap.toMutableMap()
                                        newMap.remove(stationId)
                                        newMap
                                    }
                                    _stationList.value = _stationsMap.value.values.toList()
                                }
                            }

                            val offset = currentConfig.calibrationOffset ?: 0
                            val level = (offset - (sensorData.distanceRaw ?: 0)).toDouble()

                            val warning = currentConfig.warningThreshold ?: 0.0
                            val danger = currentConfig.dangerThreshold ?: 0.0
                            val statusStr = when {
                                level >= danger -> Status.DANGER
                                level >= warning -> Status.WARNING
                                else -> Status.SAFE
                            }

                            val uiModel = StationMapUiModel(
                                sensorData = SensorData(sensorData.timestamp, if(level < 0) 0 else level.toInt(), sensorData.temp, sensorData.humid, sensorData.rainVal),
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
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = floodRepository.updateStationConfig(stationId, name, offset, warningThreshold, dangerThreshold)
            
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
                                    dangerThreshold = dangerThreshold
                                )
                            )
                        }
                        newMap
                    }
                    _stationList.value = _stationsMap.value.values.toList()

                    // Bỏ gọi loadAllStation() ở đây để tránh làm mất config và timeout flow
                }

                onComplete(success)
            }
        }
    }
}