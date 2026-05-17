# 🌊 FloodGuard: KIẾN TRÚC HỆ THỐNG CẢNH BÁO LŨ SỚM TÍCH HỢP TRÍ TUỆ NHÂN TẠO VÀ TRUYỀN THÔNG ĐA KÊNH TRONG HỆ SINH THÁI SỐ

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![TensorFlow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)

**FloodGuard** là đồ án tốt nghiệp nhằm xây dựng kiến trúc cảnh báo ngập lụt, ứng dụng các kiến thức đã biết để hoạt động ổn định

👨‍💻 **Tác giả:** Nguyễn Việt Anh & Phạm Trần Ngọc Chiến  
👩‍🏫 **Giảng viên hướng dẫn:** Th.S Nguyễn Thu Hiền  

---

## 🌟 Điểm Nổi Bật Của Dự Án (Core Features)

### 1. Đăng lên báo ở Hội thảo khoa học quốc gia NSA với mã số ISBN
### 2. Xây dựng được kiến trúc hệ thống hoàn chỉnh, hoạt động trơn chu
### 3. Xây đựng server backend, server AI để xử lý dữ liệu
### 4. Làm được hệ thống cảnh báo đa kênh (Bắn notification & Telegram)
### 5. App Android thiết kế với những công nghệ mới, chức năng đa ngôn ngữ ...

---

## 🏗️ Kiến Trúc Hệ Thống (System Architecture)

Hệ thống được chia thành 3 lớp chức năng chuyên biệt hoạt động độc lập nhưng đồng bộ liền mạch:

1. **Lớp Thiết Bị Ngoại Vi (IoT Node):** Trạm quan trắc thực địa liên tục lấy mẫu và truyền dữ liệu.
2. **Lớp Xử Lý Trung Tâm (Cloud/AI):** - *Firebase Realtime Database:* Đóng vai trò Data Hub đồng bộ trạng thái.
   - *Backend Server (Python/Railway):* Lắng nghe dữ liệu mới, chạy suy luận mô hình LSTM và trả kết quả về Firebase.
3. **Lớp Ứng Dụng (Mobile App):** "Máy khách mỏng" tập trung vào nhiệm vụ vẽ biểu đồ tương tác, hiển thị lịch sử và nhận thông báo khẩn.

---

---

## 📱 Giao Diện Ứng Dụng Android (Mobile App Interface)

Ứng dụng được thiết kế với ngôn ngữ Material Design 3, tối ưu cho việc theo dõi dữ liệu khẩn cấp.

### 1. Màn hình Giám sát (Home Screen)
- **Mục đích:** Cung cấp cái nhìn tức thời về tình trạng tại trạm gần nhất.
- **Tính năng:** Hiển thị mực nước hiện tại thông qua hiệu ứng sóng nước (Water Wave Effect). Cập nhật Nhiệt độ, Độ ẩm và Trạng thái mưa từ cảm biến.
- **Minh họa:** `[Ảnh/Video màn hình Home]`

### 2. Màn hình Phân tích AI (Analytic Screen)
- **Mục đích:** Hiển thị kết quả dự báo từ máy chủ AI.
- **Tính năng:** Biểu đồ xu hướng mực nước trong 24h. Nhãn dự báo thông minh (Tăng/Giảm/Đạt đỉnh). Hiển thị độ tin cậy của mô hình.
- **Minh họa:** `[Ảnh/Video màn hình Analytic]`

### 3. Màn hình Bản đồ (Map Screen)
- **Mục đích:** Quản lý không gian địa lý của toàn bộ hệ thống.
- **Tính năng:** Hiển thị các trạm đo trên bản đồ Google Maps. Vòng tròn trạng thái (Xanh - An toàn, Đỏ - Nguy hiểm). Cho phép Admin chỉnh sửa ngưỡng cảnh báo của trạm.
- **Minh họa:** `[Ảnh/Video màn hình Map]`

### 4. Màn hình Lịch sử & Thông báo (History & Notifications)
- **Mục đích:** Tra cứu dữ liệu quá khứ và nhật ký cảnh báo.
- **Tính năng:** Biểu đồ lịch sử mực nước theo các giờ.
- **Minh họa:** `[Ảnh/Video màn hình History/Notification]`

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

### Mobile (Android)
- **Ngôn ngữ:** Kotlin
- **Giao diện:** Jetpack Compose, Material Design 3
- **Kiến trúc:** MVVM, Clean Architecture
- **Dependency Injection:** Dagger Hilt
- **Dịch vụ & Bản đồ:** Google Maps API, Weather API, Retrofit2
- **Data:** DataStore

### AI & Backend Server
- **Ngôn ngữ:** Python
- **Machine Learning:** TensorFlow / Keras (LSTM Network)
- **Phân tích dữ liệu:** Pandas, NumPy, Scikit-learn
- **Cloud Hosting:** Railway.app (Hoạt động 24/7)

### Serverless & Database
- **Nền tảng trung tâm:** Google Firebase (Realtime Database, Cloud Messaging)
- **Luồng xử lý tự động:** Firebase Cloud Functions (Node.js)
