package com.example.doantotnghiep.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.doantotnghiep.R
import com.example.doantotnghiep.ui.bar.FloodGuardBottomBar
import com.example.doantotnghiep.ui.bar.FloodGuardTopBar
import com.example.doantotnghiep.ui.screen.HomeScreen
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.theme.DoAnTotNghiepTheme
import com.example.doantotnghiep.ui.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoAnTotNghiepTheme {
                MainLayout()
            }
        }
    }
}

@Composable
fun MainLayout(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf("home") }
    Scaffold(
        modifier = modifier,
        topBar = { FloodGuardTopBar() },
        bottomBar = { FloodGuardBottomBar(currentRoute = currentScreen, onNavigate = { currentScreen = it }) },
        containerColor = BackgroundLight,
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(currentScreen) {

                stringResource(R.string.bar_home) -> HomeScreen()
                /*stringResource(R.string.bar_map) -> MapScreen()
                stringResource(R.string.bar_analytic) -> AnalyticScreen()*/
            }
        }
    }
}