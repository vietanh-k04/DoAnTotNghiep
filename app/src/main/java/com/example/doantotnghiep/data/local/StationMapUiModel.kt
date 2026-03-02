package com.example.doantotnghiep.data.local

data class StationMapUiModel (
    val id: String? = "",
    val name: String? = "",
    val currentLevel: Double = 0.0,
    val status: Status = Status.SAFE,
    val trendValue: Trend = Trend.STABLE,
    val trendPoints: List<Float> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val coverageRadius: Double = 3000.0,
    val temp: Double = 0.0,
    val humid: Double = 0.0,
    val rainVal: Int = 0
)