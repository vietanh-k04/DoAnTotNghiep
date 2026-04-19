package com.example.doantotnghiep.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.doantotnghiep.DEFAULT_CITY
import com.example.doantotnghiep.R
import com.example.doantotnghiep.TRANSITION_DURATION_MS
import com.example.doantotnghiep.data.local.enum.ScreenRoute
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.ui.dashboard.EnvironmentSection
import com.example.doantotnghiep.ui.dashboard.HybridBadge
import com.example.doantotnghiep.ui.dashboard.WaveCard
import com.example.doantotnghiep.ui.dialog.NoStationDialog
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel
import com.example.doantotnghiep.ui.viewmodel.MapViewModel
import com.example.doantotnghiep.ui.viewmodel.WeatherViewModel
import com.example.doantotnghiep.utils.appBackground
import com.example.doantotnghiep.utils.homeTransitionSpec
import com.example.doantotnghiep.utils.toWaveCardUiModel
import com.google.android.gms.maps.model.LatLng

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    mapViewModel: MapViewModel,
    weatherViewModel: WeatherViewModel,
    userLocation: LatLng? = null,
    onNavigate: (ScreenRoute) -> Unit = {}
) {
    var isApiScreen by rememberSaveable { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    
    WeatherLifecycleSync(lifecycleOwner, userLocation, weatherViewModel)
    
    Box(modifier = Modifier.fillMaxSize()) {
        ScreenTransitionContent(
            isApiScreen = isApiScreen,
            homeViewModel = homeViewModel,
            mapViewModel = mapViewModel,
            weatherViewModel = weatherViewModel,
            userLocation = userLocation,
            onSwitchToApi = { isApiScreen = true },
            onNavigate = onNavigate
        )

        SwitchScreenFab(
            onClick = { isApiScreen = !isApiScreen },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp)
        )
    }
}

@Composable
private fun WeatherLifecycleSync(
    lifecycleOwner: LifecycleOwner,
    userLocation: LatLng?,
    weatherViewModel: WeatherViewModel
) {
    DisposableEffect(lifecycleOwner, userLocation) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val query = if (userLocation != null) {
                    "${userLocation.latitude},${userLocation.longitude}"
                } else {
                    DEFAULT_CITY
                }
                weatherViewModel.fetchWeather(query)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
private fun ScreenTransitionContent(
    isApiScreen: Boolean,
    homeViewModel: HomeViewModel,
    mapViewModel: MapViewModel,
    weatherViewModel: WeatherViewModel,
    userLocation: LatLng?,
    onSwitchToApi: () -> Unit,
    onNavigate: (ScreenRoute) -> Unit
) {
    AnimatedContent(
        targetState = isApiScreen,
        transitionSpec = { homeTransitionSpec(TRANSITION_DURATION_MS) },
        label = "HomeScreenTransition"
    ) { targetState ->
        if (targetState) {
            WeatherScreen(weatherViewModel)
        } else {
            LocalHomeScreen(
                homeViewModel = homeViewModel,
                mapViewModel = mapViewModel,
                userLocation = userLocation,
                onSwitchToApi = onSwitchToApi,
                onNavigate = onNavigate
            )
        }
    }
}

@Composable
private fun SwitchScreenFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.4f)), CircleShape),

        containerColor = Color.White.copy(alpha = 0.6f),

        contentColor = Color(0xFF007AFF),

        shape = CircleShape,

        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp,
            focusedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = stringResource(R.string.FAB_DESC),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun LocalHomeScreen(
    homeViewModel: HomeViewModel,
    mapViewModel: MapViewModel,
    userLocation: LatLng?,
    onSwitchToApi: () -> Unit,
    onNavigate: (ScreenRoute) -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val stations by mapViewModel.stationList.collectAsState(initial = emptyList())
    var hasShownDialog by rememberSaveable { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue = if (!uiState.isLocal) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = TRANSITION_DURATION_MS),
        label = "HomeScreenBlurAnimation"
    )

    LaunchedEffect(userLocation, stations) {
        homeViewModel.updateLocationAndStations(userLocation?.latitude, userLocation?.longitude, stations)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HybridBadge(isLocal = uiState.isLocal)

            AnimatedVisibility(
                visible = uiState.isLocal,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 50 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { 50 })
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    WaveCard(data = uiState.toWaveCardUiModel())

                    EnvironmentSection(
                        sensorData = SensorData(
                            null, null, uiState.temperature, uiState.humidity, uiState.rainRaw
                        ),
                        onHistoryClick = { onNavigate(ScreenRoute.HISTORY) }
                    )
                }
            }
        }

        if (!uiState.isLocal && !hasShownDialog) {
            NoStationDialog(
                onDismiss = { hasShownDialog = true },
                onConfirm = { 
                    hasShownDialog = true
                    onSwitchToApi() 
                }
            )
        }
    }
}