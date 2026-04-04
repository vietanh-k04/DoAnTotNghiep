package com.example.doantotnghiep.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.doantotnghiep.data.local.StationMapUiModel
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.ui.dashboard.EnvironmentSection
import com.example.doantotnghiep.ui.dashboard.HybridBadge
import com.example.doantotnghiep.ui.dashboard.WaveCard
import com.example.doantotnghiep.ui.dialog.NoStationDialog
import com.example.doantotnghiep.ui.theme.SoftBgBottom
import com.example.doantotnghiep.ui.theme.SoftBgTop
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel
import com.example.doantotnghiep.ui.viewmodel.MapViewModel
import com.example.doantotnghiep.ui.viewmodel.WeatherViewModel
import com.example.doantotnghiep.utils.toWaveCardUiModel
import com.google.android.gms.maps.model.LatLng

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    mapViewModel: MapViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel(),
    userLocation: LatLng? = null
) {
    var isApiScreen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, userLocation) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val query = if (userLocation != null) {
                    "${userLocation.latitude},${userLocation.longitude}"
                } else {
                    "Hanoi"
                }
                // Sẽ tự động skip nếu chưa qua 15 phút, nhờ logic mới trong WeatherViewModel
                weatherViewModel.fetchWeather(query)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = isApiScreen,
            transitionSpec = {
                val duration = 800
                val easing = FastOutSlowInEasing

                if (targetState) {
                    (scaleIn(
                        animationSpec = tween(duration, easing = easing),
                        initialScale = 0.0f,
                        transformOrigin = TransformOrigin(0.9f, 0.85f)
                    ) + fadeIn(
                        animationSpec = tween(duration, easing = easing)
                    )).togetherWith(
                        fadeOut(animationSpec = tween(duration / 2, easing = easing))
                    ).apply {
                        targetContentZIndex = 1f
                    }
                } else {
                    fadeIn(
                        animationSpec = tween(duration, easing = easing)
                    ).togetherWith(
                        scaleOut(
                            animationSpec = tween(duration, easing = easing),
                            targetScale = 0.0f,
                            transformOrigin = TransformOrigin(0.9f, 0.85f)
                        ) + fadeOut(
                            animationSpec = tween(duration, easing = easing)
                        )
                    ).apply {
                        targetContentZIndex = -1f
                    }
                }
            },
            label = "HomeScreenTransition"
        ) { targetState ->
            if (targetState) {
                WeatherScreen(weatherViewModel)
            } else {
                LocalHomeScreen(
                    homeViewModel = homeViewModel,
                    mapViewModel = mapViewModel,
                    userLocation = userLocation,
                    onSwitchToApi = { isApiScreen = true }
                )
            }
        }

        FloatingActionButton(
            onClick = { isApiScreen = !isApiScreen },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Chuyển đổi màn hình"
            )
        }
    }
}

@Composable
fun LocalHomeScreen(
    homeViewModel: HomeViewModel,
    mapViewModel: MapViewModel,
    userLocation: LatLng?,
    onSwitchToApi: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val stations by mapViewModel.stationList.collectAsState(initial = emptyList())
    var hasShownDialog by rememberSaveable { mutableStateOf(false) }

    val blurRadius by animateDpAsState(
        targetValue = if (!uiState.isLocal) 16.dp else 0.dp,
        animationSpec = tween(durationMillis = 800),
        label = "HomeScreenBlurAnimation"
    )

    StationScanningRadar(userLocation, stations, homeViewModel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom)))
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

            WaveCard(data = uiState.toWaveCardUiModel())

            EnvironmentSection(
                SensorData(
                    null,
                    null,
                    uiState.temperature,
                    uiState.humidity,
                    uiState.rainRaw
                )
            )
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

@Composable
fun StationScanningRadar(userLocation: LatLng?, stations: List<StationMapUiModel>, homeViewModel: HomeViewModel) {
    LaunchedEffect(userLocation, stations) {
        if (userLocation != null && stations.isNotEmpty()) {
            homeViewModel.scanAndSyncData(userLocation.latitude, userLocation.longitude, stations)
        }
    }
}
