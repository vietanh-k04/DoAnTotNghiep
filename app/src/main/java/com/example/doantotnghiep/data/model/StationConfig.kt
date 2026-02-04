package com.example.doantotnghiep.data.model

data class StationConfig(
    val id: String? = "",
    val name: String? = "",
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0,
    val deviceKey: String? = "",
    val calibrationOffset: Int? = 0,
    val warningThreshold: Double? = 0.0,
    val dangerThreshold: Double? = 0.0
)
