package com.example.doantotnghiep.ui.viewmodel

import android.content.Context
import com.example.doantotnghiep.data.ai.FloodPredictionHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.RADIUS_LIMIT
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.local.enum.Status
import com.example.doantotnghiep.data.local.enum.Trend
import com.example.doantotnghiep.data.local.enum.WaterLevelState
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.utils.WaterLevelValidator
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
class MapViewModel @Inject constructor(
    val floodRepository: FloodRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "MapViewModel"
    private val predictionHelper = FloodPredictionHelper(context)
    private var isModelLoaded = false
    private val _stationsMap = MutableStateFlow<Map<String, StationMapUiModel>>(emptyMap())

    private val _stationList = MutableStateFlow<List<StationMapUiModel>>(emptyList())
    val stationList: StateFlow<List<StationMapUiModel>> = _stationList

    private var loadDataJob: Job? = null

    init {
        predictionHelper.initializeModel(
            onSuccess = {
                isModelLoaded = true
                loadAllStation()
            },
            onFailure = {
                loadAllStation()
            }
        )
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

                            val previousLevel = if (configChanged) -1.0 else (previousData?.distanceRaw?.toDouble() ?: -1.0)

                            val validationState = WaterLevelValidator.validate(rawWaterLevel, previousLevel)
                            val isInvalid = validationState != WaterLevelState.VALID

                            if (isInvalid) {
                                val currentMap = _stationsMap.value
                                val existingUiModel = currentMap[stationId]
                                
                                if (existingUiModel != null) {
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
                            }

                            val level = if(rawWaterLevel < 0) 0 else rawWaterLevel.toInt()

                            val timestampMs = if (currentTimestamp < 10000000000L) currentTimestamp * 1000 else currentTimestamp
                            val isStationActive = (System.currentTimeMillis() - timestampMs) <= 3600_000L

                            val warning = currentConfig.warningThreshold ?: 0.0
                            val danger = currentConfig.dangerThreshold ?: 0.0
                            val statusStr = when {
                                !isStationActive -> Status.OFFLINE
                                level >= danger -> Status.DANGER
                                level >= warning -> Status.WARNING
                                else -> Status.SAFE
                            }

                            // Calculate 1h trend and predictions
                            var finalTrendValue = Trend.STABLE
                            var finalTrendPoints = listOf(0.1f, 0.2f, 0.15f, 0.3f)

                            if (isModelLoaded) {
                                val logsList = floodRepository.getStationLogs(stationId) ?: emptyList()
                                if (logsList.isNotEmpty()) {
                                    var sortedLogs = logsList.sortedBy { it.timestamp }.takeLast(24)
                                    if (sortedLogs.size < 24) {
                                        val missingCount = 24 - sortedLogs.size
                                        val firstLog = sortedLogs.first()
                                        val paddedLogs = mutableListOf<SensorData>()
                                        val firstTimestamp = firstLog.timestamp ?: System.currentTimeMillis()
                                        val step = if (firstTimestamp < 10000000000L) 300L else 300_000L
                                        for (i in missingCount downTo 1) {
                                            paddedLogs.add(firstLog.copy(timestamp = firstTimestamp - (i * step)))
                                        }
                                        paddedLogs.addAll(sortedLogs)
                                        sortedLogs = paddedLogs
                                    }

                                    val historicalData = sortedLogs.map { data ->
                                        val rawTs = data.timestamp ?: System.currentTimeMillis()
                                        val tsMs = if (rawTs < 10000000000L) rawTs * 1000 else rawTs
                                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = tsMs }
                                        val decimalHour = cal.get(java.util.Calendar.HOUR_OF_DAY) + cal.get(java.util.Calendar.MINUTE) / 60f
                                        val hrSin = kotlin.math.sin(2 * Math.PI * decimalHour / 24).toFloat()
                                        val hrCos = kotlin.math.cos(2 * Math.PI * decimalHour / 24).toFloat()
                                        val logRawWaterLevel = (offset - (data.distanceRaw ?: 0)).toFloat().coerceAtLeast(0f)
                                        com.example.doantotnghiep.data.local.HourlyData(
                                            rainfallCm = 0f,
                                            waterVolume = logRawWaterLevel * 10f,
                                            hrSin = hrSin,
                                            hrCos = hrCos,
                                            waterLevelCm = logRawWaterLevel
                                        )
                                    }

                                    val predictionsCm = generatePredictions(historicalData, 12, timestampMs)
                                    
                                    if (predictionsCm.isNotEmpty()) {
                                        val firstPred = predictionsCm.first()
                                        val lastPred = predictionsCm.last()
                                        finalTrendValue = when {
                                            lastPred > firstPred + 2f -> Trend.RISING
                                            lastPred < firstPred - 2f -> Trend.FALLING
                                            else -> Trend.STABLE
                                        }

                                        val maxLevel = maxOf(danger.toFloat(), predictionsCm.maxOrNull() ?: 0f, 1f) * 1.2f
                                        finalTrendPoints = predictionsCm.map { it / maxLevel }
                                    }
                                }
                            }

                            val uiModel = StationMapUiModel(
                                sensorData = SensorData(sensorData.timestamp, level, sensorData.temp, sensorData.humid, sensorData.rainVal),
                                stationConfig = StationConfig(id = currentConfig.id, name = currentConfig.name, latitude = currentConfig.latitude, longitude = currentConfig.longitude, deviceKey = currentConfig.deviceKey, calibrationOffset = currentConfig.calibrationOffset, warningThreshold = currentConfig.warningThreshold, dangerThreshold = currentConfig.dangerThreshold),
                                trendValue = finalTrendValue,
                                trendPoints = finalTrendPoints,
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

    private suspend fun generatePredictions(historical: List<com.example.doantotnghiep.data.local.HourlyData>, pointsAhead: Int, lastTimestamp: Long): List<Float> {
        val currentList = historical.toMutableList()
        val predictions = mutableListOf<Float>()
        val startCal = java.util.Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        for (i in 1..pointsAhead) {
            val nextLevelCm = predictionHelper.predictFutureWaterLevel(currentList.takeLast(24))
            predictions.add(nextLevelCm)

            startCal.add(java.util.Calendar.MINUTE, 5)
            val nextHour = startCal.get(java.util.Calendar.HOUR_OF_DAY)
            val nextMinute = startCal.get(java.util.Calendar.MINUTE)
            val decimalHour = nextHour + nextMinute / 60f
            val newSin = kotlin.math.sin(2 * Math.PI * decimalHour / 24).toFloat()
            val newCos = kotlin.math.cos(2 * Math.PI * decimalHour / 24).toFloat()

            currentList.add(
                com.example.doantotnghiep.data.local.HourlyData(
                    rainfallCm = 0f,
                    waterVolume = nextLevelCm * 10f,
                    hrSin = newSin,
                    hrCos = newCos,
                    waterLevelCm = nextLevelCm
                )
            )
        }
        return predictions
    }
}