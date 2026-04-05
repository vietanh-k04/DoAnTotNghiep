package com.example.doantotnghiep.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doantotnghiep.data.local.enum.AlertLevel
import com.example.doantotnghiep.data.local.state.HistoryScreenState
import com.example.doantotnghiep.data.local.state.LogUiModel
import com.example.doantotnghiep.data.remote.SensorData
import com.example.doantotnghiep.data.remote.StationConfig
import com.example.doantotnghiep.data.repository.FloodRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@Suppress("DEPRECATION")
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val floodRepository: FloodRepository,
    private val dbRef: DatabaseReference
) : ViewModel() {

    private val TAG = "HistoryViewModel"

    private val _stations = MutableStateFlow<List<StationConfig>>(emptyList())
    private val _selectedStation = MutableStateFlow<StationConfig?>(null)
    private val _selectedTimeRange = MutableStateFlow("1 Giờ")
    
    // Lắng nghe logs realtime của trạm đang chọn
    private val _logs = MutableStateFlow<List<LogUiModel>>(emptyList())

    private var logsListener: ValueEventListener? = null
    private var currentStationRef: DatabaseReference? = null

    // Kết hợp tất cả trạng thái lại thành 1 UI State duy nhất
    val uiState: StateFlow<HistoryScreenState> = combine(
        _stations,
        _selectedStation,
        _selectedTimeRange,
        _logs
    ) { stations, selectedStation, timeRange, logs ->
        
        // Filter by time range logic (thời gian tính bằng ms)
        val currentTime = System.currentTimeMillis()
        val timeDiff = when (timeRange) {
            "1 Giờ" -> 1L * 60 * 60 * 1000
            "6 Giờ" -> 6L * 60 * 60 * 1000
            "12 Giờ" -> 12L * 60 * 60 * 1000
            else -> 1L * 60 * 60 * 1000
        }
        val cutoffTime = currentTime - timeDiff

        // Filter logs by cutoff time
        val filteredLogs = logs.filter { it.timestamp >= cutoffTime }

        HistoryScreenState(
            stations = stations,
            selectedStation = selectedStation,
            selectedTimeRange = timeRange,
            logs = filteredLogs,
            isLoading = stations.isEmpty(),
            error = null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryScreenState(isLoading = true)
    )

    init {
        fetchStations()
    }

    private fun fetchStations() {
        viewModelScope.launch {
            val list = floodRepository.getAllStations()
            _stations.value = list
            if (list.isNotEmpty()) {
                selectStation(list.first())
            }
        }
    }

    fun selectStation(station: StationConfig) {
        _selectedStation.value = station
        observeLogs(station)
    }

    fun selectTimeRange(range: String) {
        _selectedTimeRange.value = range
    }

    private fun observeLogs(station: StationConfig) {
        // Remove old listener if exists
        currentStationRef?.let { ref ->
            logsListener?.let { ref.removeEventListener(it) }
        }

        // Lấy đúng đường dẫn data trong Firebase
        val ref = dbRef.child("stations").child(station.id ?: "").child("logs")
        currentStationRef = ref

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<LogUiModel>()
                val sdfDate = SimpleDateFormat("dd MMM", Locale("vi", "VN"))
                val sdfTime = SimpleDateFormat("HH:mm a", Locale.getDefault())

                // Ngưỡng cảnh báo lấy từ config của trạm (so sánh theo mực nước)
                val warnThresh = station.warningThreshold?.toFloat() ?: 20f
                val dangerThresh = station.dangerThreshold?.toFloat() ?: 50f

                for (child in snapshot.children) {
                    val raw = child.getValue(SensorData::class.java) ?: continue
                    
                    val timestamp = raw.timestamp ?: continue
                    val distanceRaw = raw.distanceRaw?.toFloat() ?: 0f
                    val temp = raw.temp?.toFloat() ?: 0f
                    val humid = raw.humid?.toFloat() ?: 0f
                    val rainVal = raw.rainVal?.toFloat() ?: 0f
                    
                    val dateObj = Date(timestamp)
                    
                    val stationHeight = station.calibrationOffset?.toFloat() ?: 400f
                    val waterLevel = (stationHeight - distanceRaw).coerceAtLeast(0f)

                    val level = when {
                        waterLevel >= dangerThresh -> AlertLevel.CRITICAL
                        waterLevel >= warnThresh -> AlertLevel.WARNING
                        else -> AlertLevel.SAFE                
                    }

                    val title = when (level) {
                        AlertLevel.CRITICAL -> "Cảnh Báo Ngập Nặng"
                        AlertLevel.WARNING -> "Mực Nước Dâng Cao"
                        AlertLevel.SAFE -> "Mức Nước An Toàn"
                    }

                    list.add(
                        LogUiModel(
                            id = child.key ?: "",
                            title = title,
                            description = "Nhiệt độ: ${temp}°C | Độ ẩm: ${humid}% | Mưa: $rainVal",
                            date = sdfDate.format(dateObj),
                            time = sdfTime.format(dateObj),
                            level = level,
                            distanceRaw = distanceRaw,
                            temp = temp,
                            humid = humid,
                            rainVal = rainVal,
                            timestamp = timestamp
                        )
                    )
                }

                // Sắp xếp mới nhất lên đầu (Timestamp lớn nhất)
                val sortedList = list.sortedByDescending { it.timestamp }
                _logs.value = sortedList
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error log if needed
            }
        }
        
        logsListener = listener
        ref.addValueEventListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        currentStationRef?.let { ref ->
            logsListener?.let { ref.removeEventListener(it) }
        }
    }
}