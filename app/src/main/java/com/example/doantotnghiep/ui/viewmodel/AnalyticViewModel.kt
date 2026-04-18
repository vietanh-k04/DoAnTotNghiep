package com.example.doantotnghiep.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.data.ai.FloodPredictionHelper
import com.example.doantotnghiep.data.local.AiPrediction
import com.example.doantotnghiep.data.local.HourlyData
import com.example.doantotnghiep.data.local.state.AnalyticUiState
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.ui.theme.BlueRecorded
import com.example.doantotnghiep.ui.theme.OrangePredicted
import com.example.doantotnghiep.ui.theme.RedDanger
import com.example.doantotnghiep.ui.theme.StatusSuccess
import com.example.doantotnghiep.ui.theme.TextWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private const val TAG = "AnalyticViewModel"

@HiltViewModel
class AnalyticViewModel @Inject constructor(
    private val repository: FloodRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val predictionHelper = FloodPredictionHelper(context)

    private var dataJob: Job? = null
    private var realtimeJob: Job? = null

    private val _uiState = MutableStateFlow(AnalyticUiState())
    val uiState: StateFlow<AnalyticUiState> = _uiState

    init {
        initModelAndLoadData()
    }

    private fun initModelAndLoadData() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        predictionHelper.initializeModel(
            onSuccess = {
                Log.d(TAG, "Model loaded successfully. Fetching data...")
                fetchDataAndPredict()
            },
            onFailure = { exception ->
                Log.e(TAG, "Failed to load model: ${exception.message}")
                _uiState.update { it.copy(isLoading = false, error = "Không thể tải mô hình AI: ${exception.message}") }
            }
        )
    }

    fun setTimeFrame(timeFrame: String) {
        _uiState.update { it.copy(selectedTime = timeFrame) }
        fetchDataAndPredict()
    }

    fun selectStation(station: StationConfig) {
        _uiState.update { it.copy(selectedStation = station) }
        fetchDataAndPredict()
    }

    private fun fetchDataAndPredict() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                var stations = _uiState.value.stations
                if (stations.isEmpty()) {
                    stations = repository.getAllStations()
                    _uiState.update { it.copy(stations = stations) }
                }

                val currentSelected = _uiState.value.selectedStation
                val stationToObserve = if (currentSelected != null && stations.any { it.id == currentSelected.id }) {
                    currentSelected
                } else {
                    stations.firstOrNull()?.also { first ->
                        _uiState.update { it.copy(selectedStation = first) }
                    }
                }

                val stationId = stationToObserve?.id
                if (stationId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy trạm nào!") }
                    return@launch
                }

                realtimeJob?.cancel()
                realtimeJob = viewModelScope.launch {
                    combine(
                        repository.observeStationConfig(stationId),
                        repository.getRealtimeDatabase(stationId)
                    ) { config, sensorData -> Pair(config, sensorData) }
                    .collect { (config, sensorData) ->
                        if (sensorData != null) {
                            val offset = config?.calibrationOffset ?: 400
                            val rawDistance = sensorData.distanceRaw ?: 0
                            val realtimeLevelCm = (offset - rawDistance).toFloat().coerceAtLeast(0f)
                            _uiState.update { it.copy(currentWaterLevel = realtimeLevelCm) }
                        }
                    }
                }

                combine(
                    repository.observeStationConfig(stationId),
                    repository.observeStationLogs(stationId)
                ) { config, logsList -> Pair(config, logsList) }
                .collect { (config, logsList) ->
                    val offset = config?.calibrationOffset ?: 400
                    val danger = (config?.dangerThreshold ?: 350.0).toFloat()
                    processLogsList(logsList, offset, danger)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching flow: ", e)
                _uiState.update { it.copy(isLoading = false, error = "Lỗi xử lý: ${e.message}") }
            }
        }
    }

    private suspend fun processLogsList(logsList: List<com.example.doantotnghiep.data.remote.SensorData>, offset: Int, danger: Float) {
        try {
            if (logsList.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "Không có dữ liệu trạm.") }
                return
            }
                
                var sortedLogs = logsList.sortedBy { it.timestamp }.takeLast(24)
                
                if (sortedLogs.size < 24) {
                    val missingCount = 24 - sortedLogs.size
                    val firstLog = sortedLogs.first()
                    val paddedLogs = mutableListOf<com.example.doantotnghiep.data.remote.SensorData>()
                    val firstTimestamp = firstLog.timestamp ?: System.currentTimeMillis()
                    val step = if (firstTimestamp < 10000000000L) 300L else 300_000L // 5 phút

                    for (i in missingCount downTo 1) {
                        paddedLogs.add(
                            firstLog.copy(
                                timestamp = firstTimestamp - (i * step)
                            )
                        )
                    }
                    paddedLogs.addAll(sortedLogs)
                    sortedLogs = paddedLogs
                }

                val historicalData = sortedLogs.map { data ->
                    val rawTimestamp = data.timestamp ?: System.currentTimeMillis()
                    val timestamp = if (rawTimestamp < 10000000000L) rawTimestamp * 1000 else rawTimestamp
                    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                    val decimalHour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f
                    val hrSin = sin(2 * Math.PI * decimalHour / 24).toFloat()
                    val hrCos = cos(2 * Math.PI * decimalHour / 24).toFloat()

                    val rawDistance = data.distanceRaw ?: 0
                    val waterLevelCm = (offset - rawDistance).toFloat().coerceAtLeast(0f)

                    val rainVal = data.rainVal?.toFloat() ?: 1024f
                    val rainFraction = (1024f - rainVal).coerceIn(0f, 1024f) / 1024f
                    val mappedRainCm = if (rainVal > 900f) 0f else rainFraction * 1.5f
                    
                    HourlyData(
                        rainfallCm = mappedRainCm, 
                        waterVolume = waterLevelCm * 10f, 
                        hrSin = hrSin,
                        hrCos = hrCos,
                        waterLevelCm = waterLevelCm
                    )
                }
                
                val currentLevelCm = historicalData.last().waterLevelCm
                val previousLevelCm = historicalData[historicalData.size - 2].waterLevelCm
                val isIncreasing = currentLevelCm > previousLevelCm
                val isDecreasing = currentLevelCm < previousLevelCm

                val lastRecord = sortedLogs.last()
                val rawLast = lastRecord.timestamp ?: System.currentTimeMillis()
                val lastTimestamp = if (rawLast < 10000000000L) rawLast * 1000 else rawLast

                val isStationActive = (System.currentTimeMillis() - lastTimestamp) <= 3600_000L
                
                val pointsToPredict = when (_uiState.value.selectedTime) {
                    "1h" -> 12 
                    "6h" -> 72  
                    "12h" -> 144
                    "24h" -> 288 
                    else -> 288
                }

                val rawPredictionsList = generatePredictions(historicalData, pointsToPredict, lastTimestamp)

                val predictionsList = if (rawPredictionsList.isNotEmpty()) {
                    val jumpOffset = rawPredictionsList.first() - currentLevelCm
                    rawPredictionsList.map { (it - jumpOffset).coerceAtLeast(0f) }
                } else {
                    rawPredictionsList
                }
                
                val maxLevelToDraw = maxOf(
                    danger * 1.2f,
                    currentLevelCm * 1.2f,
                    (predictionsList.maxOrNull() ?: 0f) * 1.2f
                )
                
                val recordedPointsForChart = historicalData.takeLast(5).map { 
                    it.waterLevelCm / maxLevelToDraw
                }
                
                val predictedPointsForChart = mutableListOf(recordedPointsForChart.last())

                val step = pointsToPredict / 5
                for (i in 0 until pointsToPredict step step) {
                    if (i < predictionsList.size) {
                        predictedPointsForChart.add(predictionsList[i] / maxLevelToDraw)
                    }
                }

                if (predictedPointsForChart.size < 6 && predictionsList.isNotEmpty()) {
                    predictedPointsForChart.add(predictionsList.last() / maxLevelToDraw)
                }

                val dangerThresholdPercent = danger / maxLevelToDraw

                val aiPredictions = buildPredictionList(predictionsList, danger, lastTimestamp, currentLevelCm)

                _uiState.update {
                    val realtimeLevel = it.currentWaterLevel 
                    val displayLevel = if (realtimeLevel > 0f) realtimeLevel else currentLevelCm
                    
                    it.copy(
                        isLoading = false,
                        isStationActive = isStationActive,
                        currentWaterLevel = displayLevel,
                        isIncreasing = isIncreasing,
                        isDecreasing = isDecreasing,
                        recordedPoints = recordedPointsForChart,
                        predictedPoints = predictedPointsForChart,
                        predictions = aiPredictions,
                        dangerThreshold = danger,
                        dangerThresholdPercent = dangerThresholdPercent
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error predicting: ", e)
                _uiState.update { it.copy(isLoading = false, error = "Lỗi xử lý: ${e.message}") }
            }
    }

    private suspend fun generatePredictions(historical: List<HourlyData>, pointsAhead: Int, lastTimestamp: Long): List<Float> {
        val currentList = historical.toMutableList()
        val predictions = mutableListOf<Float>()
        
        val startCal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        (1..pointsAhead).forEach { _ ->
            val nextLevelCm = predictionHelper.predictFutureWaterLevel(currentList.takeLast(24))
            predictions.add(nextLevelCm)

            startCal.add(Calendar.MINUTE, 5)
            val nextHour = startCal.get(Calendar.HOUR_OF_DAY)
            val nextMinute = startCal.get(Calendar.MINUTE)
            val decimalHour = nextHour + nextMinute / 60f
            val newSin = sin(2 * Math.PI * decimalHour / 24).toFloat()
            val newCos = cos(2 * Math.PI * decimalHour / 24).toFloat()

            val lastRainfallCm = currentList.last().rainfallCm
            var futureRainfall = lastRainfallCm * 0.5f
            if (futureRainfall < 0.1f) futureRainfall = 0f

            currentList.add(
                HourlyData(
                    rainfallCm = futureRainfall,
                    waterVolume = nextLevelCm * 10f,
                    hrSin = newSin,
                    hrCos = newCos,
                    waterLevelCm = nextLevelCm
                )
            )
        }
        return predictions
    }

    private fun buildPredictionList(predictionsCm: List<Float>, dangerThreshold: Float, lastTimestamp: Long, currentLevelCm: Float): List<AiPrediction> {
        val result = mutableListOf<AiPrediction>()
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

        val step = 1.coerceAtLeast(predictionsCm.size / 4)
        val displayedIndices = mutableListOf<Int>()
        for (i in predictionsCm.indices step step) {
            if (displayedIndices.size >= 4) break
            displayedIndices.add(i)
        }

        var maxCardLevel = -1f
        var maxCardIndex = -1
        for (idx in displayedIndices) {
            if (predictionsCm[idx] > maxCardLevel) {
                maxCardLevel = predictionsCm[idx]
                maxCardIndex = idx
            }
        }

        var previousCardLevel = currentLevelCm
        
        for (i in displayedIndices) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = lastTimestamp
                add(Calendar.MINUTE, (i + 1) * 5)
            }
            val timeStr = format.format(cal.time)
            val level = predictionsCm[i]
            
            val isPeak = (i == maxCardIndex) && (level > currentLevelCm)
            val isCritical = level >= dangerThreshold
            
            val diff = level - previousCardLevel
            val (status, color) = when {
                isCritical -> "CẢNH BÁO" to RedDanger
                isPeak -> "ĐẠT ĐỈNH" to OrangePredicted
                abs(diff) < 0.5f -> "ỔN ĐỊNH" to StatusSuccess
                diff > 0f -> "TĂNG LÊN" to BlueRecorded
                else -> "RÚT XUỐNG" to TextWhite
            }

            result.add(AiPrediction(timeStr, String.format(Locale.US, "%.1f", level).toFloat(), status, color, isPeak))
            previousCardLevel = level
        }

        return result
    }
}
