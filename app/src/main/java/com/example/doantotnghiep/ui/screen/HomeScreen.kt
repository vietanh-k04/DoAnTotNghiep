package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.ui.dashboard.*
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.ui.viewmodel.MapViewModel
import com.example.doantotnghiep.utils.*
import com.google.android.gms.maps.model.LatLng

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
    userLocation: LatLng? = null
) {
    val uiState by homeViewModel.uiState.collectAsState()

    val stations by mapViewModel.stationList.collectAsState(initial = emptyList())

    StationScanningRadar(userLocation, stations, homeViewModel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundLight)
    ) {
        HybridBadge(isLocal = uiState.isLocal)

        WaveCard(data = uiState.toWaveCardUiModel())

        EnvironmentSection(SensorData(null, null, uiState.temperature, uiState.humidity, uiState.rainRaw))
    }
}

@Composable
fun StationScanningRadar(userLocation: LatLng?, stations: List<StationMapUiModel>, homeViewModel: HomeViewModel) {
    LaunchedEffect(userLocation, stations) {
        if (userLocation != null && stations.isNotEmpty()) {
            homeViewModel.scanAndSyncData(userLocation.latitude, userLocation.longitude, stations)
        }
    }
}