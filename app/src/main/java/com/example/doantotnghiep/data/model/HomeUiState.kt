package com.example.doantotnghiep.data.model

import com.example.doantotnghiep.R

data class HomeUiState(
    val waterLevel: Double = 0.0,
    val waterLevelUnit: String = "",
    val status: Int = R.string.status_safe,
    val trend: Int = R.string.dashboard_stable,
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val rainRaw: Int = 0,
    val timestamp: Long = 0L,
    val battery: Double = 0.0,
    val lastUpdated: String = "",
    val isLocal: Boolean = true,
    val maxHeight: Double = 1.0,
    val waterPercent: Float = 0f,
)
