package com.example.doantotnghiep.data.remote

data class AiResult(
    val updatedAt: Long = 0L,
    val alertMinutes: Long? = null,
    val alertLevel: Double? = null,
)

data class TimeframeData(
    val predictedLevels: List<Double>? = null,
    val predictions: List<AiPredictionData>? = null,
)

data class AiPredictionData(
    val time: String = "",
    val level: Double = 0.0,
    val status: String = "",
    val isPeak: Boolean = false,
    val isCritical: Boolean = false,
)
