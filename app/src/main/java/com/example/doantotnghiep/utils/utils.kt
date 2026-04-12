package com.example.doantotnghiep.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.example.doantotnghiep.R
import com.example.doantotnghiep.data.local.enum.WaterLevelState
import com.example.doantotnghiep.data.local.state.LocationState
import com.example.doantotnghiep.ui.theme.StatusDanger
import com.example.doantotnghiep.ui.theme.TrendDownColor
import com.example.doantotnghiep.ui.theme.TrendUpColor
import com.example.doantotnghiep.ui.theme.WaterBlue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import java.security.MessageDigest
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.toSha256(): String {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun String.removeAccents(): String {
    if (this.isEmpty()) return this

    val vietnameseChars = mapOf(
        'a' to listOf('á', 'à', 'ả', 'ã', 'ạ', 'ă', 'ắ', 'ằ', 'ẳ', 'ẵ', 'ặ', 'â', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ'),
        'e' to listOf('é', 'è', 'ẻ', 'ẽ', 'ẹ', 'ê', 'ế', 'ề', 'ể', 'ễ', 'ệ'),
        'i' to listOf('í', 'ì', 'ỉ', 'ĩ', 'ị'),
        'o' to listOf('ó', 'ò', 'ỏ', 'õ', 'ọ', 'ô', 'ố', 'ồ', 'ổ', 'ỗ', 'ộ', 'ơ', 'ớ', 'ờ', 'ở', 'ỡ', 'ợ'),
        'u' to listOf('ú', 'ù', 'ủ', 'ũ', 'ụ', 'ư', 'ứ', 'ừ', 'ử', 'ữ', 'ự'),
        'y' to listOf('ý', 'ỳ', 'ỷ', 'ỹ', 'ỵ'),
        'd' to listOf('đ'),
        'A' to listOf('Á', 'À', 'Ả', 'Ã', 'Ạ', 'Ă', 'Ắ', 'Ằ', 'Ẳ', 'Ẵ', 'Ặ', 'Â', 'Ấ', 'Ầ', 'Ẩ', 'Ẫ', 'Ậ'),
        'E' to listOf('É', 'È', 'Ẻ', 'Ẽ', 'Ẹ', 'Ê', 'Ế', 'Ề', 'Ể', 'Ễ', 'Ệ'),
        'I' to listOf('Í', 'Ì', 'Ỉ', 'Ĩ', 'Ị'),
        'O' to listOf('Ó', 'Ò', 'Ỏ', 'Õ', 'Ọ', 'Ô', 'Ố', 'Ồ', 'Ổ', 'Ỗ', 'Ộ', 'Ơ', 'Ớ', 'Ờ', 'Ở', 'Ỡ', 'Ợ'),
        'U' to listOf('Ú', 'Ù', 'Ủ', 'Ũ', 'Ụ', 'Ư', 'Ứ', 'Ừ', 'Ử', 'Ữ', 'Ự'),
        'Y' to listOf('Ý', 'Ỳ', 'Ỷ', 'Ỹ', 'Ỵ'),
        'D' to listOf('Đ')
    )

    var result = this
    result = Normalizer.normalize(result, Normalizer.Form.NFD)
    result = "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(result, "")

    for ((unaccented, accentedList) in vietnameseChars) {
        for (accented in accentedList) {
            result = result.replace(accented.toString(), unaccented.toString())
        }
    }

    return result.split(" ").joinToString(" ") { word ->
        if (word.isNotEmpty()) {
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } else {
            ""
        }
    }
}

@Composable
fun formatTimeAgo(timestamp: Long): String {
    if (timestamp <= 0) return stringResource(R.string.time_not_update)

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val minuteMillis = 60 * 1000L
    val hourMillis = 60 * minuteMillis

    return when {
        diff < minuteMillis -> stringResource(R.string.time_just_finished)

        diff < 2 * minuteMillis -> stringResource(R.string.time_1m_ago)
        diff < 50 * minuteMillis -> stringResource(R.string.time_m_ago, "${diff / minuteMillis}")

        diff < 90 * minuteMillis -> stringResource(R.string.time_1h_ago)
        diff < 24 * hourMillis -> stringResource(R.string.time_h_ago, "${diff / hourMillis}")

        diff < 48 * hourMillis -> stringResource(R.string.time_yesterday)

        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

object LocationUtils {
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }
}

@SuppressLint("MissingPermission")
@Composable
fun rememberLocationState(): LocationState {
    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }
    )

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = LatLng(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).addOnSuccessListener { currLocation ->
                        if (currLocation != null) {
                            userLocation = LatLng(currLocation.latitude, currLocation.longitude)
                        }
                    }
                }
            }
        } else {
            val permissionsToRequest = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    return LocationState(
        hasPermission = hasLocationPermission,
        location = userLocation
    )
}

object WaterLevelValidator {
    fun validate(currentLevel: Double, lastValidLevel: Double): WaterLevelState {
        if (currentLevel < -3) return WaterLevelState.ERROR_NEGATIVE
        if (lastValidLevel >= 0.0 && (currentLevel - lastValidLevel) >= 15.0) return WaterLevelState.ERROR_OBSTRUCTION
        return WaterLevelState.VALID
    }

    fun isStabilized(currentLevel: Double, lastValidLevel: Double): Boolean {
        return lastValidLevel >= 0.0 && kotlin.math.abs(currentLevel - lastValidLevel) < 5.0
    }
}

@Composable
fun getTrendStatusText(isIncreasing: Boolean, isDecreasing: Boolean) = when {
    isIncreasing -> stringResource(R.string.TREND_INCREASING)
    isDecreasing -> stringResource(R.string.TREND_DECREASING)
    else -> stringResource(R.string.TREND_STABLE)
}

@Composable
fun getTrendInfo(
    latestWaterLevel: Int?,
    previousWaterLevel: Int?,
    isInactive: Boolean
): Triple<String, Color, ImageVector> {
    return when {
        isInactive -> Triple(stringResource(R.string.INACTIVE), StatusDanger, Icons.Default.WarningAmber)
        latestWaterLevel != null && previousWaterLevel != null -> {
            when {
                latestWaterLevel > previousWaterLevel -> Triple(stringResource(R.string.TREND_UP), TrendUpColor, Icons.AutoMirrored.Rounded.TrendingUp)
                latestWaterLevel < previousWaterLevel -> Triple(stringResource(R.string.TREND_DOWN), TrendDownColor, Icons.AutoMirrored.Rounded.TrendingDown)
                else -> Triple(stringResource(R.string.TREND_STABLE), WaterBlue, Icons.AutoMirrored.Rounded.TrendingFlat)
            }
        }
        else -> Triple(stringResource(R.string.LATEST), TrendDownColor, Icons.AutoMirrored.Rounded.TrendingFlat)
    }
}