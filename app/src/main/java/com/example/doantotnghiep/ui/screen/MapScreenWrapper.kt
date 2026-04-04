package com.example.doantotnghiep.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doantotnghiep.data.local.state.LocationState
import com.example.doantotnghiep.ui.map.MapScreen
import com.example.doantotnghiep.ui.viewmodel.MapViewModel

@Composable
fun MapScreenWrapper(locationState: LocationState, viewModel: MapViewModel = hiltViewModel()) {
    val stations by viewModel.stationList.collectAsState()
    MapScreen(
        stations = stations,
        isLocationGranted = locationState.hasPermission,
        userLocation = locationState.location,
        onUpdateStationConfig = { id, name, offset, warning, danger, onComplete ->
            viewModel.updateStationConfig(id, name, offset, warning, danger, onComplete)
        }
    )
}
