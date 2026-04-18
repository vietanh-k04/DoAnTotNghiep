package com.example.doantotnghiep.data.local.state

import com.example.doantotnghiep.data.local.AiPrediction
import com.example.doantotnghiep.data.remote.StationConfig

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
    val dangerThresholdPercent: Float = 0.5f,
    val selectedTime: String = "1h",
    val stations: List<StationConfig> = emptyList(),
    val selectedStation: StationConfig? = null
)
