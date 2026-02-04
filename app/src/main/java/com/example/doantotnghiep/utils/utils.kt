package com.example.doantotnghiep.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.doantotnghiep.R
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.toSha256(): String {
    val bytes = this.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
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

