package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doantotnghiep.data.model.SensorData
import com.example.doantotnghiep.ui.dashboard.*
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doantotnghiep.utils.*

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundLight)
    ) {
        HybridBadge(isLocal = uiState.isLocal)

        WaveCard(data = uiState.toWaveCardUiModel())

        EnvironmentSection(SensorData(null, null, uiState.temperature, uiState.humidity, uiState.rainRaw))

        Spacer(modifier = Modifier.height(24.dp))
    }
}