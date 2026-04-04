package com.example.doantotnghiep.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.doantotnghiep.data.local.enum.ScreenRoute
import com.example.doantotnghiep.ui.bar.FloodGuardBottomBar
import com.example.doantotnghiep.ui.bar.FloodGuardTopBar
import com.example.doantotnghiep.ui.screen.AnalyticScreen
import com.example.doantotnghiep.ui.screen.HistoryScreen
import com.example.doantotnghiep.ui.screen.HomeScreen
import com.example.doantotnghiep.ui.screen.MapScreenWrapper
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.theme.DoAnTotNghiepTheme
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
    var currentScreen by remember { mutableStateOf(ScreenRoute.HOME) }

    val locationState = rememberLocationState()

    Scaffold(
        modifier = modifier,
        topBar = { FloodGuardTopBar() },
        containerColor = BackgroundLight,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                when(currentScreen) {
                    ScreenRoute.HOME -> HomeScreen(userLocation = locationState.location)
                    ScreenRoute.MAP -> MapScreenWrapper(locationState = locationState)
                    ScreenRoute.ANALYTIC -> AnalyticScreen()
                    ScreenRoute.HISTORY -> HistoryScreen()
                }
            }
            
            FloodGuardBottomBar(
                currentRoute = currentScreen,
                onNavigate = { currentScreen = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

fun registerMessagingNotification() {
    FirebaseMessaging.getInstance().subscribeToTopic("flood_warning")
        .addOnCompleteListener { }
}