package com.example.doantotnghiep.data.local

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class GuideStepData(
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val iconTint: Color = Color.Unspecified,
    val imageRes: Int? = null
)
