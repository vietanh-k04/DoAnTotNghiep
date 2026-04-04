package com.example.doantotnghiep.data.local

import com.example.doantotnghiep.data.local.enum.AlertLevel

data class AlertHistoryModel(
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val level: AlertLevel
)