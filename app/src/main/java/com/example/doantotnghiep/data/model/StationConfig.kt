package com.example.doantotnghiep.data.model

data class StationConfig(
    val id: String?,
    val name: String?,
    val latitude: Double?,
    val longitude: Double?,
    val deviceKey: String?,
    val calibrationOffset: Int?,
    val alertThreshold: Int?,
)
