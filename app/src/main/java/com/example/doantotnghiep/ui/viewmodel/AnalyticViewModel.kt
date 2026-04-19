package com.example.doantotnghiep.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.data.local.AiPrediction
import com.example.doantotnghiep.data.local.state.AnalyticUiState
import com.example.doantotnghiep.data.remote.AiPredictionData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.ui.theme.BlueRecorded
import com.example.doantotnghiep.ui.theme.OrangePredicted
import com.example.doantotnghiep.ui.theme.RedDanger
import com.example.doantotnghiep.ui.theme.StatusSuccess
import com.example.doantotnghiep.ui.theme.TextWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AnalyticViewModel"

@HiltViewModel
class AnalyticViewModel @Inject constructor(
    private val repository: FloodRepository,
) : ViewModel() {

    private var dataJob: Job? = null
    private var realtimeJob: Job? = null
    private var aiTimeframeJob: Job? = null

    private val _uiState = MutableStateFlow(AnalyticUiState())
    val uiState: StateFlow<AnalyticUiState> = _uiState

    init {
        fetchDataAndObserve()
    }

    fun setTimeFrame(timeFrame: String) {
        _uiState.update { it.copy(selectedTime = timeFrame) }

        val stationId = _uiState.value.selectedStation?.id
        restartTimeframeObservation(stationId, timeFrame)
    }

    fun selectStation(station: StationConfig) {
        _uiState.update { it.copy(selectedStation = station) }
        fetchDataAndObserve()
    }

    private fun fetchDataAndObserve() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                var stations = _uiState.value.stations
                if (stations.isEmpty()) {
                    stations = repository.getAllStations()
                    _uiState.update { it.copy(stations = stations) }
                }

                val stationToObserve = resolveStation(stations)
                val stationId = stationToObserve?.id ?: run {
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
                            val rawDist = sensorData.distanceRaw ?: 0
                            val level = (offset - rawDist).toFloat().coerceAtLeast(0f)
                            _uiState.update { it.copy(currentWaterLevel = level) }
                        }
                    }
                }

                restartTimeframeObservation(stationId, _uiState.value.selectedTime)

                combine(
                    repository.observeStationConfig(stationId),
                    repository.observeStationLogs(stationId)
                ) { config, logs -> Pair(config, logs) }
                .collect { (config, logs) ->
                    val offset = config?.calibrationOffset ?: 400
                    val danger = (config?.dangerThreshold ?: 350.0).toFloat()
                    processLogs(logs, offset, danger)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error: ", e)
                _uiState.update { it.copy(isLoading = false, error = "Lỗi xử lý: ${e.message}") }
            }
        }
    }

    private fun processLogs(
        logsList: List<com.example.doantotnghiep.data.remote.SensorData>,
        offset: Int,
        danger: Float
    ) {
        if (logsList.isEmpty()) return

        val sorted = logsList.sortedBy { it.timestamp }.takeLast(24)
        val levels = sorted.map { data ->
            val raw = data.distanceRaw ?: 0
            (offset - raw).toFloat().coerceAtLeast(0f)
        }

        val current  = levels.last()
        val previous = levels[levels.size - 2]
        val isIncreasing = current > previous
        val isDecreasing = current < previous

        val lastRaw = sorted.last().timestamp ?: System.currentTimeMillis()
        val lastMs  = if (lastRaw < 10_000_000_000L) lastRaw * 1000 else lastRaw
        val isActive = (System.currentTimeMillis() - lastMs) <= 3_600_000L

        val maxLevel = maxOf(
            danger * 1.2f,
            current * 1.2f,
            (_uiState.value.predictedPoints.maxOrNull()?.let {
                it * maxOf(danger * 1.2f, current * 1.2f)
            }) ?: 0f
        )
        val recordedChart = levels.takeLast(5).map {
            (it / maxOf(maxLevel, 1f)).coerceIn(0f, 1f)
        }

        _uiState.update {
            val display = if (it.currentWaterLevel > 0f) it.currentWaterLevel else current
            it.copy(
                isLoading       = false,
                isStationActive = isActive,
                currentWaterLevel = display,
                isIncreasing    = isIncreasing,
                isDecreasing    = isDecreasing,
                recordedPoints  = recordedChart,
            )
        }
    }

    private fun restartTimeframeObservation(stationId: String?, timeframe: String) {
        aiTimeframeJob?.cancel()
        aiTimeframeJob = viewModelScope.launch {
            combine(
                repository.observeStationConfig(stationId),
                repository.observeAiTimeframe(stationId, timeframe)
            ) { config, tfData -> Pair(config, tfData) }
            .collect { (config, tfData) ->
                val danger = (config?.dangerThreshold ?: 350.0).toFloat()
                if (tfData == null) return@collect

                val predictedLevels = tfData.predictedLevels?.map { it.toFloat() } ?: emptyList()
                val maxLevel = maxOf(
                    danger * 1.2f,
                    _uiState.value.currentWaterLevel * 1.2f,
                    (predictedLevels.maxOrNull() ?: 0f) * 1.2f
                )
                val predictedPointsChart = predictedLevels.map {
                    (it / maxOf(maxLevel, 1f)).coerceIn(0f, 1f)
                }
                val predictionCards = (tfData.predictions ?: emptyList()).map { it.toAiPrediction() }

                _uiState.update {
                    it.copy(
                        isLoading              = false,
                        dangerThreshold        = danger,
                        dangerThresholdPercent = danger / maxOf(maxLevel, 1f),
                        predictedPoints        = predictedPointsChart,
                        predictions            = predictionCards,
                    )
                }
            }
        }
    }

    private fun resolveStation(stations: List<StationConfig>): StationConfig? {
        val current = _uiState.value.selectedStation
        return if (current != null && stations.any { it.id == current.id }) {
            current
        } else {
            stations.firstOrNull()?.also { first ->
                _uiState.update { it.copy(selectedStation = first) }
            }
        }
    }
}


private fun AiPredictionData.toAiPrediction(): AiPrediction {
    val color = when {
        isCritical          -> RedDanger
        status == "ĐẠT ĐỈNH"  -> OrangePredicted
        status == "TĂNG LÊN"  -> BlueRecorded
        status == "RÚT XUỐNG" -> StatusSuccess
        status == "ỔN ĐỊNH"   -> StatusSuccess
        else                  -> TextWhite
    }
    return AiPrediction(
        time   = time,
        level  = level.toFloat(),
        status = status,
        color  = color,
        isPeak = isPeak,
    )
}
