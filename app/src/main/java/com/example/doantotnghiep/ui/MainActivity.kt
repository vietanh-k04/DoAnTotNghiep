package com.example.doantotnghiep.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.doantotnghiep.data.local.ScreenRoute
import com.example.doantotnghiep.ui.bar.FloodGuardBottomBar
import com.example.doantotnghiep.ui.bar.FloodGuardTopBar
import com.example.doantotnghiep.ui.screen.HomeScreen
import com.example.doantotnghiep.ui.screen.MapScreenWrapper
import com.example.doantotnghiep.ui.screen.rememberLocationState
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.theme.DoAnTotNghiepTheme
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

    GetPermissionNotification()
    Scaffold(
        modifier = modifier,
        topBar = { FloodGuardTopBar() },
        bottomBar = { FloodGuardBottomBar(currentRoute = currentScreen, onNavigate = { currentScreen = it }) },
        containerColor = BackgroundLight,
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(currentScreen) {
                ScreenRoute.HOME -> HomeScreen(userLocation = locationState.location)
                ScreenRoute.MAP -> MapScreenWrapper()
                /*ScreenRoute.ANALYTIC -> AnalyticScreen()*/

            }
        }
    }
}

@Composable
fun GetPermissionNotification() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

fun registerMessagingNotification() {
    FirebaseMessaging.getInstance().subscribeToTopic("flood_warning")
        .addOnCompleteListener { }
}