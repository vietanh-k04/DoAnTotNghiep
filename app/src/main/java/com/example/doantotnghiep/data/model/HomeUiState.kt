package com.example.doantotnghiep.data.model

import com.example.doantotnghiep.R

data class HomeUiState(
    val waterLevel: Double = 0.0,
    val waterLevelUnit: String = "",
    val status: String = "",
    val trend: Int = R.string.dashboard_stable,
    val temperature: Double = 0.0,
    val humidity: Double = 0.0,
    val rainRaw: Int = 0,
    val timestamp: Long = 0L,
    val battery: Double = 0.0,
    val lastUpdated: String = "",
    val isLocal: Boolean = true
)
