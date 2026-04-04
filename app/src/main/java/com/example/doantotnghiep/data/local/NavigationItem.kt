package com.example.doantotnghiep.data.local

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.doantotnghiep.data.local.enum.ScreenRoute

data class NavigationItem(
    val title: String?,
    val icon: ImageVector?,
    val route: ScreenRoute?
)
