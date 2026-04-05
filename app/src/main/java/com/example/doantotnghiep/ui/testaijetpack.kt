package com.example.doantotnghiep.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.doantotnghiep.data.ai.FloodPredictionHelper
import com.example.doantotnghiep.data.ai.getMock24HourData
import kotlinx.coroutines.launch

@Composable
fun TestFloodModelScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Khởi tạo Helper (Thực tế sau này bạn sẽ dùng ViewModel)
    val helper = remember { FloodPredictionHelper(context) }

    // Biến lưu trạng thái trên màn hình
    var resultText by remember { mutableStateOf("Chưa chạy") }

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Trạng thái: $resultText", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            resultText = "1. Đang kết nối Firebase và tải model..."

            // Bước 1: Gọi hàm tải model
            helper.initializeModel(
                onSuccess = {
                    resultText = "2. Tải model thành công! Đang dự đoán..."

                    // Bước 2: Model đã tải xong, ném data vào chạy trong luồng nền
                    coroutineScope.launch {
                        try {
                            val mockData = getMock24HourData()
                            val prediction = helper.predictFutureWaterLevel(mockData)
                            resultText = "3. THÀNH CÔNG! Dự đoán mực nước giờ tiếp theo: ${String.format("%.2f", prediction)} cm"
                        } catch (e: Exception) {
                            resultText = "Lỗi khi chạy model: ${e.message}"
                        }
                    }
                },
                onFailure = { error ->
                    resultText = "Lỗi tải model: ${error.message}"
                }
            )
        }) {
            Text("Chạy thử AI")
        }
    }
}