package com.example.doantotnghiep.notification

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.doantotnghiep.data.ai.FloodPredictionHelper
import com.example.doantotnghiep.data.local.HourlyData
import com.example.doantotnghiep.data.repository.FloodRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CompletableDeferred
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@HiltWorker
class FloodPredictionWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FloodRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    private val predictionHelper = FloodPredictionHelper(context)

    override suspend fun doWork(): Result {
        try {
            Log.d("FloodWorker", "Worker đang chạy ngầm để kiểm tra ngập...")

            val prefs = context.getSharedPreferences("flood_prefs", Context.MODE_PRIVATE)
            val lastAlertTime = prefs.getLong("last_local_alert_time", 0L)
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAlertTime < 2 * 60 * 60 * 1000) {
                Log.d("FloodWorker", "Chưa hết thời gian chờ (cooldown). Bỏ qua.")
                return Result.success()
            }

            var isModelLoaded = false
            predictionHelper.initializeModel(
                onSuccess = { isModelLoaded = true },
                onFailure = { Log.e("FloodWorker", "Lỗi tải model") }
            )

            val deferred = CompletableDeferred<Boolean>()
            predictionHelper.initializeModel(
                onSuccess = { deferred.complete(true) },
                onFailure = { deferred.complete(false) }
            )

            if (!isModelLoaded) return Result.retry()

            val stations = repository.getAllStations()
            if (stations.isEmpty()) return Result.success()
            val targetStation = stations.first()
            val stationId = targetStation.id ?: return Result.success()
            val dangerThreshold = (targetStation.dangerThreshold ?: 350.0).toFloat()
            val offset = targetStation.calibrationOffset ?: 400

            val logsList = repository.getStationLogs(stationId)
            if (logsList.isNullOrEmpty()) return Result.success()

            val sortedLogs = logsList.sortedBy { it.timestamp }.takeLast(24)
            if (sortedLogs.size < 24) return Result.success()

            val historicalData = sortedLogs.map { data ->
                val rawTimestamp = data.timestamp ?: System.currentTimeMillis()
                val timestamp = if (rawTimestamp < 10000000000L) rawTimestamp * 1000 else rawTimestamp
                val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
                val decimalHour = cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / 60f

                val rawDistance = data.distanceRaw ?: 0
                val waterLevelCm = (offset - rawDistance).toFloat().coerceAtLeast(0f)
                val rainVal = data.rainVal?.toFloat() ?: 1024f
                val mappedRainCm = if (rainVal > 900f) 0f else ((1024f - rainVal) / 1024f) * 1.5f

                HourlyData(
                    rainfallCm = mappedRainCm,
                    waterVolume = waterLevelCm * 10f,
                    hrSin = sin(2 * Math.PI * decimalHour / 24).toFloat(),
                    hrCos = cos(2 * Math.PI * decimalHour / 24).toFloat(),
                    waterLevelCm = waterLevelCm
                )
            }.toMutableList()

            val lastTimestamp = sortedLogs.last().timestamp?.let {
                if (it < 10000000000L) it * 1000 else it
            } ?: System.currentTimeMillis()

            val pointsAhead = 288
            val startCal = Calendar.getInstance().apply { timeInMillis = lastTimestamp }

            var alertMinutes: Int? = null
            var alertLevel: Float? = null

            for (i in 1..pointsAhead) {
                val nextLevelCm = predictionHelper.predictFutureWaterLevel(historicalData.takeLast(24))

                if (nextLevelCm >= dangerThreshold) {
                    alertMinutes = i * 5
                    alertLevel = nextLevelCm
                    break
                }

                startCal.add(Calendar.MINUTE, 5)
                val decimalHour = startCal.get(Calendar.HOUR_OF_DAY) + startCal.get(Calendar.MINUTE) / 60f
                historicalData.add(
                    HourlyData(
                        rainfallCm = historicalData.last().rainfallCm * 0.5f,
                        waterVolume = nextLevelCm * 10f,
                        hrSin = sin(2 * Math.PI * decimalHour / 24).toFloat(),
                        hrCos = cos(2 * Math.PI * decimalHour / 24).toFloat(),
                        waterLevelCm = nextLevelCm
                    )
                )
            }

            if (alertMinutes != null) {
                val title = "⚠️ CẢNH BÁO DỰ BÁO NGẬP LỤT TỪ AI"
                val message = "Dự kiến trạm ${targetStation.name} sẽ đạt mức BÁO ĐỘNG ĐỎ (${String.format("%.1f", alertLevel)}cm) trong khoảng $alertMinutes phút nữa. Hãy chuẩn bị ứng phó!"

                notificationHelper.sendLocalFloodAlert(title, message)

                prefs.edit { putLong("last_local_alert_time", System.currentTimeMillis()) }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e("FloodWorker", "Lỗi chạy worker: ${e.message}")
            return Result.failure()
        }
    }
}