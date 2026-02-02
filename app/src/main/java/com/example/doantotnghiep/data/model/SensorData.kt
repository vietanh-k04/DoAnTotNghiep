package com.example.doantotnghiep.data.model

data class SensorData(
    val timestamp: Long? = null,
    val distanceRaw: Int? = null,
    val temp: Double? = null,
    val humid: Double? = null,
    val rainVal: Int? = null,
)
