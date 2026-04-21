package com.example.doantotnghiep.data.local.state

import com.example.doantotnghiep.data.local.enum.AlertLevel
import com.example.doantotnghiep.data.remote.StationConfig

data class HistoryScreenState(
    val stations: List<StationConfig> = emptyList(),
    val selectedStation: StationConfig? = null,
    val selectedTimeRange: String = "1 Ngày",
    val logs: List<LogUiModel> = emptyList(),
    val filterLevel: AlertLevel? = null,
    val totalCritical: Int = 0,
    val totalWarning: Int = 0,
    val totalSafe: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

data class LogUiModel(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val level: AlertLevel,
    val distanceRaw: Float,
    val temp: Float,
    val humid: Float,
    val rainVal: Float,
    val timestamp: Long
)