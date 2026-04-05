package com.example.doantotnghiep.data.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName

@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: Int = 1,
    val timestamp: Long = 0L,
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false
)
