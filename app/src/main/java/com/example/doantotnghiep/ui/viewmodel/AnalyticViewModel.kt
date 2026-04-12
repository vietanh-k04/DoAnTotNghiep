package com.example.doantotnghiep.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.data.ai.FloodPredictionHelper
import com.example.doantotnghiep.data.local.AiPrediction
import com.example.doantotnghiep.data.local.HourlyData
import com.example.doantotnghiep.data.repository.FloodRepository
import com.example.doantotnghiep.ui.theme.BlueRecorded
import com.example.doantotnghiep.ui.theme.OrangePredicted
import com.example.doantotnghiep.ui.theme.RedDanger
import com.example.doantotnghiep.ui.theme.TextWhite
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

data class AnalyticUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val isStationActive: Boolean = true,
    val currentWaterLevel: Float = 0f,
    val isIncreasing: Boolean = false,
    val isDecreasing: Boolean = false,
    val recordedPoints: List<Float> = emptyList(),
    val predictedPoints: List<Float> = emptyList(),
    val predictions: List<AiPrediction> = emptyList(),
    val dangerThreshold: Float = 3.5f,
    val dangerThresholdPercent: Float = 0.5f, // Thêm property này
    val selectedTime: String = "24h"
)

@HiltViewModel
class AnalyticViewModel @Inject constructor(
    private val repository: FloodRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val TAG = "AnalyticViewModel"
    private val predictionHelper = FloodPredictionHelper(context)

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

    private fun fetchDataAndPredict() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // 1. Lấy danh sách trạm, chọn trạm đầu tiên (hoặc lấy từ cache/HomeViewModel)
                val stations = repository.getAllStations()
                val stationId = stations.firstOrNull()?.id
                if (stationId == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Không tìm thấy trạm nào!") }
                    return@launch
                }

                val config = repository.getStationConfig(stationId)
                val logsList = repository.getStationLogs(stationId) ?: emptyList()

                val offset = config?.calibrationOffset ?: 400
                val danger = (config?.dangerThreshold ?: 350.0).toFloat() // Sử dụng đơn vị cm

                if (logsList.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, error = "Không có dữ liệu trạm.") }
                    return@launch
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
                    
                    HourlyData(
                        rainfallCm = 0f, 
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
                
                // Trạm coi như không hoạt động nếu bản ghi cuối cùng cách đây quá 1 giờ
                val isStationActive = (System.currentTimeMillis() - lastTimestamp) <= 3600_000L
                
                val pointsToPredict = when (_uiState.value.selectedTime) {
                    "1h" -> 12 
                    "6h" -> 72  
                    "12h" -> 144
                    "24h" -> 288 
                    else -> 288
                }

                val predictionsList = generatePredictions(historicalData, pointsToPredict, lastTimestamp)
                
                val maxLevelToDraw = maxOf(
                    danger * 1.2f,
                    currentLevelCm * 1.2f,
                    (predictionsList.maxOrNull() ?: 0f) * 1.2f
                )
                
                val recordedPointsForChart = historicalData.takeLast(5).map { 
                    it.waterLevelCm / maxLevelToDraw
                }
                
                val predictedPointsForChart = mutableListOf(recordedPointsForChart.last())
                
                // Lấy các điểm dự đoán cách đều nhau để vẽ
                val step = if (pointsToPredict > 5) pointsToPredict / 5 else 1
                for (i in 0 until pointsToPredict step step) {
                    if (i < predictionsList.size) {
                        predictedPointsForChart.add(predictionsList[i] / maxLevelToDraw)
                    }
                }
                
                // Lấy điểm dự đoán cuối cùng (nếu chưa được add do step)
                if (predictedPointsForChart.size < 6 && predictionsList.isNotEmpty()) {
                    predictedPointsForChart.add(predictionsList.last() / maxLevelToDraw)
                }

                // Tính toán tỷ lệ phần trăm của Danger Threshold để vẽ trên UI
                val dangerThresholdPercent = danger / maxLevelToDraw

                // 5. Build danh sách AiPrediction cho UI
                val aiPredictions = buildPredictionList(predictionsList, danger, lastTimestamp)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isStationActive = isStationActive,
                        currentWaterLevel = currentLevelCm,
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
                Log.e(TAG, "Error fetching/predicting: ", e)
                _uiState.update { it.copy(isLoading = false, error = "Lỗi xử lý: ${e.message}") }
            }
        }
    }

    private suspend fun generatePredictions(historical: List<HourlyData>, pointsAhead: Int, lastTimestamp: Long): List<Float> {
        val currentList = historical.toMutableList()
        val predictions = mutableListOf<Float>()
        
        val startCal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

        for (i in 1..pointsAhead) {
            val nextLevelCm = predictionHelper.predictFutureWaterLevel(currentList.takeLast(24))
            predictions.add(nextLevelCm)

            // Mô phỏng dữ liệu cho 5 phút tiếp theo
            startCal.add(Calendar.MINUTE, 5)
            val nextHour = startCal.get(Calendar.HOUR_OF_DAY)
            val nextMinute = startCal.get(Calendar.MINUTE)
            val decimalHour = nextHour + nextMinute / 60f
            val newSin = sin(2 * Math.PI * decimalHour / 24).toFloat()
            val newCos = cos(2 * Math.PI * decimalHour / 24).toFloat()

            currentList.add(
                HourlyData(
                    rainfallCm = 0f, // Giả sử không mưa trong tương lai
                    waterVolume = nextLevelCm * 10f, 
                    hrSin = newSin,
                    hrCos = newCos,
                    waterLevelCm = nextLevelCm
                )
            )
        }
        return predictions
    }

    private fun buildPredictionList(predictionsCm: List<Float>, dangerThreshold: Float, lastTimestamp: Long): List<AiPrediction> {
        val result = mutableListOf<AiPrediction>()
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Chọn ra tối đa 4 mốc thời gian nổi bật để hiển thị
        var maxPredicted = 0f
        var maxIndex = -1

        predictionsCm.forEachIndexed { index, value ->
            if (value > maxPredicted) {
                maxPredicted = value
                maxIndex = index
            }
        }

        // Lấy 4 điểm (cách đều hoặc có mốc đỉnh)
        val step = Math.max(1, predictionsCm.size / 4)
        for (i in predictionsCm.indices step step) {
            if (result.size >= 4) break
            
            val cal = Calendar.getInstance().apply {
                timeInMillis = lastTimestamp
                add(Calendar.MINUTE, (i + 1) * 5)
            }
            val timeStr = format.format(cal.time)
            val level = predictionsCm[i]
            
            val isPeak = (i == maxIndex)
            val isCritical = level >= dangerThreshold
            
            val (status, color) = when {
                isCritical -> "CẢNH BÁO" to RedDanger
                isPeak -> "ĐẠT ĐỈNH" to OrangePredicted
                level > predictionsCm.firstOrNull() ?: 0f -> "TĂNG LÊN" to BlueRecorded
                else -> "RÚT XUỐNG" to TextWhite
            }

            result.add(AiPrediction(timeStr, String.format(Locale.US, "%.1f", level).toFloat(), status, color, isPeak))
        }

        return result
    }
}
