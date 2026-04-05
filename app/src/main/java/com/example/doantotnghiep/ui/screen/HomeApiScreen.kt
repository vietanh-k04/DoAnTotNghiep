package com.example.doantotnghiep.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.doantotnghiep.data.remote.AqiData
import com.example.doantotnghiep.data.remote.AstroData
import com.example.doantotnghiep.data.remote.DailyWeather
import com.example.doantotnghiep.data.remote.HourlyWeather
import com.example.doantotnghiep.data.remote.RunningCondition
import com.example.doantotnghiep.data.remote.WeatherHeader
import com.example.doantotnghiep.data.remote.WeatherMetrics
import com.example.doantotnghiep.data.remote.WeatherUiState
import com.example.doantotnghiep.ui.theme.GlassBg
import com.example.doantotnghiep.ui.theme.SoftBgBottom
import com.example.doantotnghiep.ui.theme.SoftBgTop
import com.example.doantotnghiep.ui.theme.TextDim
import com.example.doantotnghiep.ui.theme.TextWhite
import com.example.doantotnghiep.ui.viewmodel.WeatherViewModel
import com.example.doantotnghiep.utils.getLottieWeatherResource
import com.example.doantotnghiep.utils.removeAccents

@Composable
fun WeatherScreen(weatherViewModel: WeatherViewModel) {
    val uiState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SoftBgTop, SoftBgBottom)))
    ) {
        when (val state = uiState) {
            is WeatherUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is WeatherUiState.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
            is WeatherUiState.Success -> {
                val data = state.weatherData
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WeatherHeaderSection(data.header)
                    HourlyForecastSection(data.hourlyForecast, data.header.lowTemp, data.header.highTemp)
                    DailyForecastSection(data.dailyForecast)
                    ActivityRunningSection(data.runningCondition)
                    AqiSection(data.aqi)
                    WeatherGridSection(data.metrics)
                    SunAndMoonSection(data.astro)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun WeatherHeaderSection(header: WeatherHeader) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                val locationName = try {
                    if (header.locationName.contains("Ã") || header.locationName.contains("Ä")) {
                        String(header.locationName.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8).removeAccents()
                    } else {
                        header.locationName.removeAccents()
                    }
                } catch (_: Exception) {
                    header.locationName.removeAccents()
                }

                Text(
                    locationName,
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("${header.currentTemp}°", color = TextWhite, fontSize = 80.sp, fontWeight = FontWeight.Light)
            Text(header.conditionText, color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("↑${header.highTemp}° / ↓${header.lowTemp}°", color = TextWhite, fontSize = 16.sp)
            Text("Cảm giác như ${header.feelsLike}°", color = TextDim, fontSize = 16.sp)
        }

        IllustrationPlaceholder(header.conditionIconUrl)
    }
}

@Composable
fun IllustrationPlaceholder(iconUrl: String) {
    val lottieRes = getLottieWeatherResource(iconUrl)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun SmallWeatherIcon(iconUrl: String, modifier: Modifier = Modifier) {
    val lottieRes = getLottieWeatherResource(iconUrl)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))

    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

@Composable
fun HourlyForecastSection(hourlyForecast: List<HourlyWeather>, lowTemp: Int, highTemp: Int) {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Nhiệt độ từ $lowTemp độ C đến $highTemp độ C",
                color = TextWhite,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(Modifier, thickness = 1.dp, color = TextDim.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                items(hourlyForecast) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.time, color = TextDim, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        SmallWeatherIcon(iconUrl = item.iconUrl, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${item.temp}°",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(10.dp)
                            )
                            Text("${item.chanceOfRain}%", color = TextDim, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyForecastSection(dailyForecast: List<DailyWeather>) {
    GlassCard {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = TextDim,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dự báo ${dailyForecast.size} ngày", color = TextDim, fontSize = 14.sp)
            }
            HorizontalDivider(Modifier, thickness = 1.dp, color = TextDim.copy(alpha = 0.2f))

            dailyForecast.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.dayName,
                        color = TextWhite,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            item.chanceOfRain,
                            color = TextDim,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    SmallWeatherIcon(
                        iconUrl = item.iconUrl,
                        modifier = Modifier.size(32.dp).weight(1f)
                    )
                    Text(
                        "${item.highTemp} ${item.lowTemp}",
                        color = TextWhite,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityRunningSection(runningCondition: RunningCondition) {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chạy bộ", color = TextDim, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(runningCondition.status, color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(runningCondition.description, color = TextDim, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun AqiSection(aqi: AqiData) {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AQI", color = TextDim, fontSize = 12.sp)
            Text(
                aqi.status,
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { aqi.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (aqi.aqiIndex <= 2) Color.Green else if (aqi.aqiIndex <= 4) Color(0xFFFF9800) else Color.Red,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun WeatherGridSection(metrics: WeatherMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GridMetricCard(
                modifier = Modifier.weight(1f),
                title = "Chỉ số UV",
                value = metrics.uvIndex.toString(),
                subtitle = if (metrics.uvIndex >= 6f) "Mức độ cao" else if (metrics.uvIndex >= 3f) "Mức trung bình" else "Mức độ thấp",
                icon = Icons.Default.WbSunny
            ) {
                LinearProgressIndicator(
                    progress = { metrics.uvIndex / 11f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (metrics.uvIndex >= 6f) Color.Red else if (metrics.uvIndex >= 3f) Color(0xFFFF9800) else Color.Green,
                    trackColor = Color.White.copy(0.2f)
                )
            }
            GridMetricCard(
                modifier = Modifier.weight(1f),
                title = "Độ ẩm",
                value = "${metrics.humidity}%",
                subtitle = if (metrics.humidity >= 70) "Mức ẩm cao, oi bức" else if (metrics.humidity >= 40) "Mức độ thoải mái" else "Không khí hanh khô",
                icon = Icons.Default.WaterDrop
            ) {
                LinearProgressIndicator(
                    progress = { metrics.humidity / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF4FC3F7),
                    trackColor = Color.White.copy(0.2f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GridMetricCard(
                modifier = Modifier.weight(1f),
                title = "Gió",
                value = "${metrics.windKph} km/h",
                subtitle = "Hướng: ${metrics.windDirection}",
                icon = Icons.Default.Air
            ) {
                Canvas(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    drawCircle(color = TextWhite.copy(0.2f), style = Stroke(width = 4f))
                    drawCircle(
                        color = TextWhite, radius = 4f, center = Offset(size.width / 2, 4f)
                    )
                }
            }
            GridMetricCard(
                modifier = Modifier.weight(1f),
                title = "Điểm sương",
                value = "${metrics.dewPoint}°",
                subtitle = if (metrics.dewPoint > 20) "Cảm giác ngột ngạt" else "Khô ráo, thoải mái",
                icon = Icons.Default.Thermostat
            ) {}
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GridMetricCard(
                modifier = Modifier.weight(1f),
                title = "Áp suất",
                value = "${metrics.pressureMb} mb",
                subtitle = "Mức tiêu chuẩn",
                icon = Icons.Default.Speed
            ) {
                Canvas(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    drawArc(
                        color = TextWhite.copy(0.2f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                    val sweep = ((metrics.pressureMb - 950f) / 100f).coerceIn(0f, 1f) * 270f
                    drawArc(
                        color = TextWhite,
                        startAngle = 135f,
                        sweepAngle = if (sweep > 0) sweep else 100f,
                        useCenter = false,
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )
                }
            }
            GridMetricCard(
                modifier = Modifier.weight(1f),
                title = "Tầm nhìn",
                value = "${metrics.visibilityKm} km",
                subtitle = if (metrics.visibilityKm >= 10) "Trời quang, tầm nhìn tốt" else "Tầm nhìn hạn chế",
                icon = Icons.Default.Visibility
            ) {}
        }
    }
}

@Composable
fun GridMetricCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    customDraw: @Composable ColumnScope.() -> Unit = {}
) {
    GlassCard(modifier = modifier.height(160.dp)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, contentDescription = null, tint = TextDim, modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = TextDim, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (value.isNotEmpty()) {
                Text(value, color = TextWhite, fontSize = 26.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            customDraw()
            
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(subtitle, color = TextDim, fontSize = 12.sp, lineHeight = 16.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun SunAndMoonSection(astro: AstroData) {
    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 20.dp)
                ) {
                    val path = Path().apply {
                        moveTo(0f, size.height)
                        quadraticTo(size.width / 2, -size.height, size.width, size.height)
                    }
                    drawPath(path, color = TextWhite.copy(0.3f), style = Stroke(width = 4f))
                    drawCircle(
                        color = Color(0xFFFFC107), radius = 12f, center = Offset(size.width / 2, 0f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Bình minh", color = TextDim, fontSize = 12.sp)
                    Text(astro.sunrise, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hoàng hôn", color = TextDim, fontSize = 12.sp)
                    Text(astro.sunset, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    GlassCard {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Brightness3,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier
                    .size(60.dp)
                    .weight(1f)
            )
            Column(modifier = Modifier.weight(2f)) {
                Text("Mặt trăng mọc", color = TextDim, fontSize = 12.sp)
                Text(astro.moonrise, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Mặt trăng lặn", color = TextDim, fontSize = 12.sp)
                Text(astro.moonset, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GlassBg)
    ) {
        content()
    }
}