package com.example.doantotnghiep.data.remote

data class StationLogs (
    val stationId: String?,
    val logs: Map<String, SensorData>?
)