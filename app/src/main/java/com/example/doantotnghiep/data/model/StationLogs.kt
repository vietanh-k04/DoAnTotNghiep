package com.example.doantotnghiep.data.model

data class StationLogs (
    val stationId: String?,
    val logs: Map<String, SensorData>?
)