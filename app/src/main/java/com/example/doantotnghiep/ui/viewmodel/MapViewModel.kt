package com.example.doantotnghiep.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.data.local.*
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(val floodRepository: FloodRepository) : ViewModel() {
    private val _stationsMap = MutableStateFlow<Map<String, StationMapUiModel>>(emptyMap())

    val stationList: StateFlow<List<StationMapUiModel>> = MutableStateFlow(emptyList())

    init {
        loadAllStation()
    }

    private fun loadAllStation() {
        viewModelScope.launch {
            val config = floodRepository.getAllStations()

            config.forEach { config ->
                val stationId = config.id ?: return@forEach

                launch {
                    floodRepository.getRealtimeDatabase(stationId).collect { sensorData ->
                        if(sensorData != null) {
                            val offset = config.calibrationOffset ?: 0
                            val level = (offset - (sensorData.distanceRaw ?: 0)).toDouble()

                            val warning = config.warningThreshold ?: 0.0
                            val danger = config.dangerThreshold ?: 0.0
                            val statusStr = when {
                                level >= danger -> Status.DANGER
                                level >= warning -> Status.WARNING
                                else -> Status.SAFE
                            }

                            val uiModel = StationMapUiModel(
                                sensorData = SensorData(sensorData.timestamp, if(level < 0) 0 else level.toInt(), sensorData.temp, sensorData.humid, sensorData.rainVal),
                                stationConfig = StationConfig(id = config.id, name = config.name, latitude = config.latitude, longitude = config.longitude, deviceKey = config.deviceKey, calibrationOffset = config.calibrationOffset, warningThreshold = config.warningThreshold, dangerThreshold = config.dangerThreshold),
                                trendValue = Trend.STABLE,
                                trendPoints = listOf(10f, 20f, 15f, 30f),
                                status = statusStr,
                                coverageRadius = 3000.0,
                            )

                            _stationsMap.update { currentMap ->
                                val newMap = currentMap.toMutableMap()
                                newMap[stationId] = uiModel
                                newMap
                            }

                            (stationList as MutableStateFlow).value = _stationsMap.value.values.toList()
                        }
                    }
                }
            }
        }
    }
}