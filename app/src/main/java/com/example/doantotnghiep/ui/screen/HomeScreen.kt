package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doantotnghiep.ui.dashboard.*
import com.example.doantotnghiep.ui.theme.BackgroundLight

@Composable
fun HomeScreen() {
    val sensorData = null

    val stationConfig = null

    val currentLevel = 50.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundLight)
    ) {
        HybridBadge(isLocal = true)

        WaveCard(waterLevel = currentLevel, maxHeight = 150.0, status = "", lastUpdate = "")

        EnvironmentSection(sensorData)

        Spacer(modifier = Modifier.height(24.dp))
    }
}