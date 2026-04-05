package com.example.doantotnghiep.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.doantotnghiep.data.remote.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLog>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLog(log: NotificationLog)

    @Query("UPDATE notification_logs SET isRead = 1 WHERE id = :logId")
    suspend fun markAsRead(logId: String)

    @Query("UPDATE notification_logs SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("SELECT COUNT(*) FROM notification_logs")
    suspend fun getLogCount(): Int

    @Query("DELETE FROM notification_logs WHERE id IN (SELECT id FROM notification_logs ORDER BY timestamp ASC LIMIT 5)")
    suspend fun deleteOldestFive()

    @Query("SELECT MAX(timestamp) FROM notification_logs")
    suspend fun getMaxTimestamp(): Long?
}
