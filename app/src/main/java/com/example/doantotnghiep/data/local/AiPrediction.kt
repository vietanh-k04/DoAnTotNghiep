package com.example.doantotnghiep.data.local

import androidx.compose.ui.graphics.Color


data class AiPrediction(
    val time: String,
    val level: Float,
    val status: String,
    val color: Color,
    val isPeak: Boolean = false
)
