package com.example.doantotnghiep.data.ai

import android.content.Context
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter

class FloodPredictionHelper(private val context: Context) {
    private val featMean = floatArrayOf(0.0167269f, 1518.8413f, 0.0f, 0.0f, 43.589306f)
    private val featScale = floatArrayOf(0.0188076f, 3291.5815f, 0.707106f, 0.707106f, 2.856556f)
    private val targMean = 43.601562f
    private val targScale = 2.8390098f
    private val CM_TO_FEET = 0.0328084f
    private val FEET_TO_CM = 30.48f

    private var interpreter: Interpreter? = null

    fun initializeModel(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()
            .build()

        FirebaseModelDownloader.getInstance()
            .getModel(
                "urban_flood_model",
                DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND,
                conditions
            )
            .addOnSuccessListener { model ->
                val modelFile = model.file
                if (modelFile != null) {
                    interpreter = Interpreter(modelFile)
                    Log.d("FloodPrediction", "Tải model thành công!")
                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FloodPrediction", "Lỗi tải model: ${exception.message}")
                onFailure(exception)
            }
    }

    suspend fun predictFutureWaterLevel(historicalData: List<HourlyData>): Float {
        return withContext(Dispatchers.Default) {
            if (historicalData.size != 24) {
                throw IllegalArgumentException("Mô hình yêu cầu chính xác 24 mốc thời gian (hiện tại: ${historicalData.size})")
            }
            if (interpreter == null) {
                throw IllegalStateException("Mô hình chưa được tải xong từ Firebase!")
            }

            val inputArray = Array(1) { Array(24) { FloatArray(5) } }

            for (i in 0 until 24) {
                val hour = historicalData[i]
                val scaledFeatures = preprocessInput(
                    hour.rainfallCm,
                    hour.waterVolume,
                    hour.hrSin,
                    hour.hrCos,
                    hour.waterLevelCm
                )
                inputArray[0][i] = scaledFeatures
            }

            val outputArray = Array(1) { FloatArray(1) }

            interpreter?.run(inputArray, outputArray)

            val tfliteRawOutput = outputArray[0][0]

            postprocessOutput(tfliteRawOutput)
        }
    }

    fun preprocessInput(
        rainfallCm: Float,
        waterVolume: Float,
        hrSin: Float,
        hrCos: Float,
        waterLevelCm: Float
    ): FloatArray {
        val rainfallFt = rainfallCm * CM_TO_FEET
        val waterLevelFt = waterLevelCm * CM_TO_FEET

        val scaledRainfall = (rainfallFt - featMean[0]) / featScale[0]
        val scaledVolume = (waterVolume - featMean[1]) / featScale[1]
        val scaledHrSin = (hrSin - featMean[2]) / featScale[2]
        val scaledHrCos = (hrCos - featMean[3]) / featScale[3]
        val scaledWaterLevel = (waterLevelFt - featMean[4]) / featScale[4]

        return floatArrayOf(scaledRainfall, scaledVolume, scaledHrSin, scaledHrCos, scaledWaterLevel)
    }

    fun postprocessOutput(tflitePrediction: Float): Float {
        val predictedWaterLevelFt = (tflitePrediction * targScale) + targMean
        val predictedWaterLevelCm = predictedWaterLevelFt * FEET_TO_CM

        return predictedWaterLevelCm
    }
}