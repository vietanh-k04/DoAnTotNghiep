package com.example.doantotnghiep.data.model

import com.example.doantotnghiep.R

data class WaveCardUiModel (
    val waterLevel: Double = 0.0,
    val maxHeight: Double = 0.0,
    val status: Int = R.string.status_safe,
    val trend: Int = R.string.dashboard_stable,
    val lastUpdated: String = "",
    val waterPercent: Float = 0f
)