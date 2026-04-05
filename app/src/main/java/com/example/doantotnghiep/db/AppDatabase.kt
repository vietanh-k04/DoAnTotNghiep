package com.example.doantotnghiep.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.doantotnghiep.data.remote.NotificationLog
import com.example.doantotnghiep.db.dao.NotificationDao

@Database(entities = [NotificationLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}