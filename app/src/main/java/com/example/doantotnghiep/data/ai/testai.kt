package com.example.doantotnghiep.data.ai

fun getMock24HourData(): List<HourlyData> {
    val mockList = mutableListOf<HourlyData>()
    var currentWaterLevel = 0f

    for (i in 0 until 24) {
        mockList.add(
            HourlyData(
                rainfallCm = 0.5f,
                waterVolume = 1500f,
                hrSin = 0.0f,
                hrCos = 1.0f,
                waterLevelCm = currentWaterLevel
            )
        )
        currentWaterLevel += 0.2f
    }
    return mockList
}

