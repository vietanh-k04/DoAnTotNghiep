package com.example.doantotnghiep.data.local

import com.example.doantotnghiep.data.local.enum.Status
import com.example.doantotnghiep.data.local.enum.Trend
import com.example.doantotnghiep.data.remote.*

data class StationMapUiModel (
    val status: Status = Status.SAFE,
    val trendValue: Trend = Trend.STABLE,
    val trendPoints: List<Float> = emptyList(),
    val coverageRadius: Double = 3000.0,
    val sensorData: SensorData = SensorData(),
    var stationConfig: StationConfig = StationConfig()
)