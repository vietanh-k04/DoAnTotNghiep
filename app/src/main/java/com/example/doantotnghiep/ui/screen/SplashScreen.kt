package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.LottieCompositionFactory.fromRawResSync
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.example.doantotnghiep.R
import com.example.doantotnghiep.ui.theme.BackgroundLight
import com.example.doantotnghiep.ui.theme.BlueRecorded
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val context = LocalContext.current
    val composition = remember {
        fromRawResSync(context, R.raw.splash_animation).value
    }
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true
    )

    LaunchedEffect(key1 = true) {
        delay(5000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(400.dp)
            )

            Text(
                modifier = Modifier.padding(horizontal = 40.dp),
                text = "ỨNG DỤNG CẢNH BÁO LŨ SỚM",
                textAlign = TextAlign.Center,
                color = BlueRecorded,
                fontSize = 30.sp,
                lineHeight = 45.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
