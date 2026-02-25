package com.example.doantotnghiep.data.remote

data class StationConfig(
    val id: String? = "",
    var name: String? = "",
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var deviceKey: String? = "",
    var calibrationOffset: Int? = 0,
    var warningThreshold: Double? = 0.0,
    var dangerThreshold: Double? = 0.0
)
