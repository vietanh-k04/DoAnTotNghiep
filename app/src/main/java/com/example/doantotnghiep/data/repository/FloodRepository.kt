package com.example.doantotnghiep.data.repository

import com.example.doantotnghiep.db.dao.NotificationDao
import com.example.doantotnghiep.data.remote.NotificationLog
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.remote.StationLogs
import com.example.doantotnghiep.utils.toSha256
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FloodRepository @Inject constructor(
    private val dbRef: DatabaseReference,
    private val notificationDao: NotificationDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    fun getRealtimeDatabase(stationId: String?): Flow<SensorData?> = callbackFlow {
        val ref = dbRef.child("stations").child(stationId ?: "").child("data")
        val listener = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val data = p0.getValue(SensorData::class.java)
                trySend(data)
            }

            override fun onCancelled(p0: DatabaseError) { }


        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getStationConfig(stationId: String?): StationConfig? {
        return try {
            val snapshot = dbRef.child("stations").child(stationId ?: "").child("config").get().await()
            snapshot.getValue(StationConfig::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun observeStationConfig(stationId: String?): Flow<StationConfig?> = callbackFlow {
        val ref = dbRef.child("stations").child(stationId ?: "").child("config")
        val listener = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val config = p0.getValue(StationConfig::class.java)
                trySend(config)
            }

            override fun onCancelled(p0: DatabaseError) { }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getStationLogs(stationId: String?) : List<SensorData>? {
        return try {
            val snapshot = dbRef.child("stations").child(stationId ?: "").child("logs").get().await()
            val list = mutableListOf<SensorData>()
            for (child in snapshot.children) {
                val data = child.getValue(SensorData::class.java)
                if (data != null) {
                    list.add(data)
                }
            }
            list
        } catch (_: Exception) {
            null
        }
    }

    fun observeStationLogs(stationId: String?): Flow<List<SensorData>> = callbackFlow {
        val ref = dbRef.child("stations").child(stationId ?: "").child("logs").orderByChild("timestamp").limitToLast(24)
        val listener = object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val list = mutableListOf<SensorData>()
                for (child in p0.children) {
                    val data = child.getValue(SensorData::class.java)
                    if (data != null) {
                        list.add(data)
                    }
                }
                trySend(list)
            }

            override fun onCancelled(p0: DatabaseError) { }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun getAllStations(): List<StationConfig> {
        var retries = 3
        while (retries > 0) {
            try {
                val snapshot = dbRef.child("stations").get().await()
                return snapshot.children.mapNotNull {
                    it.child("config").getValue(StationConfig::class.java)
                }
            } catch (e: Exception) {
                retries--
                if (retries == 0) return emptyList()
                kotlinx.coroutines.delay(2000)
            }
        }
        return emptyList()
    }

    suspend fun updateDeviceKey(stationId: String?, rawKey: String?): Boolean {
        return try {
            val hashedKey = rawKey?.toSha256()
            dbRef.child("stations").child(stationId ?: "").child("config").child("deviceKey").setValue(hashedKey).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun updateStationConfig(
        stationId: String,
        name: String,
        offset: Int,
        warningThreshold: Double,
        dangerThreshold: Double,
        latitude: Double?,
        longitude: Double?
    ): Boolean {
        return try {
            val updates = mutableMapOf<String, Any>(
                "name" to name,
                "calibrationOffset" to offset,
                "warningThreshold" to warningThreshold,
                "dangerThreshold" to dangerThreshold
            )
            if (latitude != null) updates["latitude"] = latitude
            if (longitude != null) updates["longitude"] = longitude

            dbRef.child("stations").child(stationId).child("config").updateChildren(updates).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun syncNotificationLogs() {
        scope.launch {
            val maxTimestamp = notificationDao.getMaxTimestamp() ?: 0L
            
            val ref = if (maxTimestamp == 0L) {
                dbRef.child("notification_logs").orderByChild("timestamp").limitToLast(20)
            } else {
                dbRef.child("notification_logs").orderByChild("timestamp").startAt((maxTimestamp + 1).toDouble())
            }

            ref.addChildEventListener(object : com.google.firebase.database.ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val log = snapshot.getValue(NotificationLog::class.java)?.copy(id = snapshot.key ?: "")
                    if (log != null) {
                        scope.launch {
                            notificationDao.insertLog(log)
                            if (notificationDao.getLogCount() > 20) {
                                notificationDao.deleteOldestFive()
                            }
                        }
                    }
                }
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun getNotificationLogs(): Flow<List<NotificationLog>> {
        return notificationDao.getAllLogs()
    }

    suspend fun markAsRead(logId: String) {
        notificationDao.markAsRead(logId)
    }

    suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }
}