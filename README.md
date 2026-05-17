# 🌊 FloodGuard: Hệ Thống Cảnh Báo Ngập Lụt Thông Minh (IoT & AI)

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

**FloodGuard** là đồ án tốt nghiệp chuyên ngành Công nghệ Thông tin, được xây dựng nhằm cung cấp một giải pháp toàn diện giám sát, phân tích và cảnh báo sớm nguy cơ ngập lụt. Hệ thống là sự kết hợp chặt chẽ giữa thiết bị phần cứng IoT tại thực địa, máy chủ Trí tuệ nhân tạo (AI) trên đám mây và ứng dụng di động Android trực quan.

👨‍💻 **Tác giả:** Nguyễn Việt Anh & Phạm Trần Ngọc Chiến  
👩‍🏫 **Giảng viên hướng dẫn:** Th.S Nguyễn Thu Hiền  

---

## 🌟 Điểm Nổi Bật Của Dự Án (Core Features)

Dự án thể hiện giải pháp hệ thống hoàn chỉnh (End-to-End Architecture), giải quyết bài toán thủy văn từ khâu thu thập dữ liệu thô cho đến cảnh báo người dùng cuối.

### 1. Hạ Tầng IoT Quan Trắc Thời Gian Thực
- **Vi điều khiển trung tâm:** Sử dụng NodeMCU ESP8266 làm Gateway quản lý và truyền tải dữ liệu qua Wi-Fi.
- **Hệ thống cảm biến:** Tích hợp cảm biến siêu âm (HC-SR04) để đo khoảng cách mực nước, cảm biến mưa (Rain Sensor), và cảm biến môi trường (DHT11) giám sát nhiệt độ/độ ẩm.
- **Tối ưu băng thông:** Dữ liệu thô được tiền xử lý, khử nhiễu ngay tại trạm và đóng gói chuẩn JSON trước khi đẩy lên Firebase với độ trễ tính bằng mili-giây.

### 2. Mô Hình AI Dự Báo Học Sâu (Deep Learning)
- **Mô hình LSTM:** Trái tim của hệ thống phân tích là mạng nơ-ron bộ nhớ dài - ngắn hạn (LSTM), được huấn luyện trên bộ dữ liệu thủy văn thực tế.
- **Hiệu năng xuất sắc:** Mô hình đạt hệ số hiệu quả NSE **0.9057** và sai số RMSE chỉ **0.14035**.
- **Dự báo cuốn chiếu:** Phân tích 24 bản ghi lịch sử gần nhất để tự động suy luận và đưa ra kịch bản biến động mực nước cho các khung giờ quan trọng: **1h, 6h, 12h và 24h** tới.

### 3. Ứng Dụng Android Đạt Chuẩn Doanh Nghiệp (Enterprise-Level App)
- **Kiến trúc hiện đại:** Xây dựng hoàn toàn bằng **Kotlin** kết hợp kiến trúc **MVVM** và **Clean Architecture**. Tích hợp **Dagger Hilt** để quản lý Dependency Injection.
- **Jetpack Compose:** Giao diện người dùng được thiết kế bằng UI Toolkit mới nhất, kết hợp Lottie Animations tạo hiệu ứng sóng nước sinh động mô phỏng mực nước thực tế.
- **Đồng bộ thời gian thực:** Trực quan hóa đồ thị động lực nước, hiển thị thẻ trạng thái cảnh báo (An toàn / Cảnh báo / Nguy hiểm) ngay lập tức khi Firebase có biến động.
- **Tích hợp API phụ trợ:** Tự động chuyển đổi sang giao diện dự báo thời tiết (Weather API) khi người dùng không ở gần trạm quan trắc.

### 4. Hệ Thống Cảnh Báo Đa Kênh (Multi-Channel Alerts)
- Áp dụng cơ chế cảnh báo tập trung qua **Firebase Cloud Functions** (Node.js).
- Ngay khi phát hiện mực nước thực tế hoặc dự báo chạm ngưỡng rủi ro, hệ thống lập tức phát lệnh:
  - Bắn **Push Notification (FCM)** đến thiết bị Android của người dân.
  - Gửi báo cáo khẩn cấp qua **Telegram Bot** cho lực lượng chức năng điều phối cứu hộ.

---

## 🏗️ Kiến Trúc Hệ Thống (System Architecture)

Hệ thống được chia thành 3 lớp chức năng chuyên biệt hoạt động độc lập nhưng đồng bộ liền mạch:

1. **Lớp Thiết Bị Ngoại Vi (IoT Node):** Trạm quan trắc thực địa liên tục lấy mẫu và truyền dữ liệu.
2. **Lớp Xử Lý Trung Tâm (Cloud/AI):** - *Firebase Realtime Database:* Đóng vai trò Data Hub đồng bộ trạng thái.
   - *Backend Server (Python/Railway):* Lắng nghe dữ liệu mới, chạy suy luận mô hình LSTM và trả kết quả về Firebase.
3. **Lớp Ứng Dụng (Mobile App):** "Máy khách mỏng" tập trung vào nhiệm vụ vẽ biểu đồ tương tác, hiển thị lịch sử và nhận thông báo khẩn.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### Mobile (Android)
- **Ngôn ngữ:** Kotlin
- **Giao diện:** Jetpack Compose, Material Design 3
- **Kiến trúc:** MVVM, Clean Architecture
- **Dependency Injection:** Dagger Hilt
- **Dịch vụ & Bản đồ:** Google Maps Compose SDK, Retrofit2, Play Services Location

### AI & Backend Server
- **Ngôn ngữ:** Python
- **Machine Learning:** TensorFlow / Keras (LSTM Network)
- **Phân tích dữ liệu:** Pandas, NumPy, Scikit-learn
- **Cloud Hosting:** Railway.app (Hoạt động 24/7)

### Serverless & Database
- **Nền tảng trung tâm:** Google Firebase (Realtime Database, Cloud Messaging)
- **Luồng xử lý tự động:** Firebase Cloud Functions (Node.js)
