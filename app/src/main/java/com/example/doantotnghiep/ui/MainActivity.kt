package com.example.doantotnghiep.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.doantotnghiep.data.local.enum.ScreenRoute
import com.example.doantotnghiep.ui.bar.FloodGuardBottomBar
import com.example.doantotnghiep.ui.bar.FloodGuardTopBar
import com.example.doantotnghiep.ui.dialog.AlertPopups
import com.example.doantotnghiep.ui.screen.analytic.AnalyticScreen
import com.example.doantotnghiep.ui.screen.history.HistoryScreen
import com.example.doantotnghiep.ui.screen.home.HomeScreen
import com.example.doantotnghiep.ui.screen.map.MapScreenWrapper
import com.example.doantotnghiep.ui.screen.splash.SplashScreen
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.theme.DoAnTotNghiepTheme
import com.example.doantotnghiep.ui.viewmodel.HistoryViewModel
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel
import com.example.doantotnghiep.ui.viewmodel.MapViewModel
import com.example.doantotnghiep.ui.viewmodel.WeatherViewModel
import com.example.doantotnghiep.utils.rememberLocationState
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerMessagingNotification()

        setContent {
            DoAnTotNghiepTheme {
                MainLayout()
            }
        }
    }

}

@Composable
fun MainLayout(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(ScreenRoute.SPLASH) }

    val locationState = rememberLocationState()

    val historyViewModel: HistoryViewModel = hiltViewModel()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val mapViewModel: MapViewModel = hiltViewModel()
    val weatherViewModel: WeatherViewModel = hiltViewModel()

    val homeUiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { if (currentScreen != ScreenRoute.SPLASH) FloodGuardTopBar() },
        containerColor = BackgroundLight,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 800),
                    label = "screen_transition"
                ) { targetScreen ->
                    when(targetScreen) {
                        ScreenRoute.SPLASH -> SplashScreen(
                            onSplashFinished = { currentScreen = ScreenRoute.HOME }
                        )
                        ScreenRoute.HOME -> HomeScreen(
                            userLocation = locationState.location,
                            onNavigate = { currentScreen = it },
                            homeViewModel = homeViewModel,
                            mapViewModel = mapViewModel,
                            weatherViewModel = weatherViewModel
                        )
                        ScreenRoute.MAP -> MapScreenWrapper(
                            locationState = locationState,
                            viewModel = mapViewModel,
                            onNavigateToHistory = { stationConfig ->
                                historyViewModel.selectStation(stationConfig)
                                currentScreen = ScreenRoute.HISTORY
                            }
                        )
                        ScreenRoute.ANALYTIC -> AnalyticScreen()
                        ScreenRoute.HISTORY -> HistoryScreen(viewModel = historyViewModel)
                    }
                }
            }

            if (currentScreen != ScreenRoute.SPLASH) {
                AlertPopups(
                    showRecalibrate = homeUiState.showRecalibratePopup,
                    showObstruction = homeUiState.showObstructionPopup,
                    onDismissRecalibrate = { homeViewModel.dismissRecalibratePopup() },
                    onDismissObstruction = { homeViewModel.dismissObstructionPopup() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            AnimatedVisibility(
                visible = currentScreen != ScreenRoute.SPLASH,
                enter = fadeIn(animationSpec = tween(800)),
                exit = fadeOut(animationSpec = tween(800)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloodGuardBottomBar(
                    currentRoute = currentScreen,
                    onNavigate = { currentScreen = it }
                )
            }
        }
    }
}

fun registerMessagingNotification() {
    FirebaseMessaging.getInstance().subscribeToTopic("flood_warning")
        .addOnCompleteListener { }
}