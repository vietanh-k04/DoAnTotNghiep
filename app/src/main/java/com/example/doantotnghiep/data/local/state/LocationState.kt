package com.example.doantotnghiep.data.local.state

import com.google.android.gms.maps.model.LatLng

data class LocationState(
    val hasPermission: Boolean,
    val location: LatLng?
)
