package com.example.doantotnghiep.data.remote

import com.google.firebase.database.PropertyName

data class NotificationLog(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: Int = 1,
    val timestamp: Long = 0L,
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false
)
