package com.example.doantotnghiep.data.repository

import com.example.doantotnghiep.data.model.SensorData
import com.example.doantotnghiep.data.model.StationConfig
import com.example.doantotnghiep.data.model.StationLogs
import com.example.doantotnghiep.utils.toSha256
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FloodRepository @Inject constructor(private val dbRef: DatabaseReference) {
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

    suspend fun getStationLogs(stationId: String?) : StationLogs? {
        return try {
            val snapshot = dbRef.child("stations").child(stationId ?: "").child("logs").get().await()
            snapshot.getValue(StationLogs::class.java)
        } catch (_: Exception) {
            null
        }

    }

    suspend fun getAllStations(): List<StationConfig> {
        return try {
            val snapshot = dbRef.child("stations").get().await()
            snapshot.children.mapNotNull {
                it.child("config").getValue(StationConfig::class.java)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun updateDeviceKey(stationId: String?, rawKey: String?): Boolean {
        return try {
            val hashedKey = rawKey?.toSha256()
            dbRef.child("stations").child(stationId ?: "").child("config").child("device_key").setValue(hashedKey).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}