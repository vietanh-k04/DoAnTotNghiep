package com.example.doantotnghiep.data.remote

data class ErrorLog(
    val id: String = "",
    val message: String? = null,
    val rawValue: Int? = null,
    val timestamp: Long? = null,
    val type: String? = null
)
