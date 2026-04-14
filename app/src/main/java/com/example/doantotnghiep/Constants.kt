package com.example.doantotnghiep

import com.google.android.gms.maps.model.LatLng

const val API_KEY = "801f4634810540e5b9a80215252301"

const val RADIUS_LIMIT = 3000f

const val WARNING_THRESHOLD = 0.15f

const val DANGER_THRESHOLD = 0.5f

const val TRANSITION_DURATION_MS = 800

const val DEFAULT_CITY = "Hanoi"

const val MOCK_AI_CONFIDENCE_PERCENT = "89%"

const val MOCK_AI_CONFIDENCE_FLOAT = 0.89f

val TIME_FRAMES = listOf("1h", "6h", "12h", "24h")

val TIME_RANGES = listOf("1 Giờ", "6 Giờ", "12 Giờ")
const val INACTIVE_TIMEOUT_MS = 30 * 60 * 1000L
const val DEFAULT_STATION_HEIGHT = 400f
const val MAX_CHART_POINTS = 50

val HANOI_LOCATION = LatLng(21.0285, 105.8246)
const val DEFAULT_ZOOM = 11f
const val FOCUS_ZOOM = 14f
const val DOUBLE_CLICK_ZOOM = 15f
const val ANIM_DURATION_MS = 1500
const val LONG_ANIM_DURATION_MS = 2000