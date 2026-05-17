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
- **Minh họa:**
<img width="229" height="462" alt="image" src="https://github.com/user-attachments/assets/5af57013-7b0b-45ab-97b1-2622c22b1a89" />
<img width="228" height="462" alt="image" src="https://github.com/user-attachments/assets/be88e611-36aa-4dc7-8370-8c2e263d0561" />
<img width="231" height="465" alt="image" src="https://github.com/user-attachments/assets/462665d0-b9c5-4b4f-bc40-743ef66dcfb1" />

### 2. Màn hình thời tiết (Home API Screen)
- **Mục đích:** Hiển thị thông tin thời tiết ở khu vực nếu người dùng không nằm trong vùng phủ sóng của trạm.
- **Tính năng:** Hiển thị vị trí của người dùng, thông tin thời tiết thời điểm hiện tại, thời tiết theo giờ, theo ngày, các thông số môi trường ...
- **Minh họa:**
<img width="246" height="498" alt="image" src="https://github.com/user-attachments/assets/6f34912f-3402-4a3e-a550-0d0c06c8cbc4" />
<img width="249" height="500" alt="image" src="https://github.com/user-attachments/assets/e699217c-d702-4680-ac94-801503138b57" />
<img width="242" height="489" alt="image" src="https://github.com/user-attachments/assets/75e2a5be-d6fb-44d2-8eff-23a5fbbed061" />
<img width="244" height="489" alt="image" src="https://github.com/user-attachments/assets/fffc11d1-65ba-40b3-b09b-d3cea2ce571f" />

### 3. Màn hình Bản đồ (Map Screen)
- **Mục đích:** Quản lý không gian địa lý của toàn bộ hệ thống.
- **Tính năng:** Hiển thị các trạm đo trên bản đồ Google Maps. Vòng tròn trạng thái (Xanh - An toàn, Đỏ - Nguy hiểm). Cho phép Admin chỉnh sửa ngưỡng cảnh báo của trạm, dùng hàm băm mật mã để mã hoá mật khẩu.
- **Minh họa:**
<img width="183" height="369" alt="image" src="https://github.com/user-attachments/assets/f2449f48-bd0f-4db8-8aa6-bf5f9fb30cfc" />
<img width="184" height="368" alt="image" src="https://github.com/user-attachments/assets/80a04521-9d2e-4278-bd56-a5e1e49ceec7" />
<img width="182" height="366" alt="image" src="https://github.com/user-attachments/assets/608f8cad-0dbd-4d8c-ba15-62c8433efbec" />

### 4. Màn hình Phân tích AI (Analytic Screen)
- **Mục đích:** Hiển thị kết quả dự báo từ máy chủ AI.
- **Tính năng:** Biểu đồ xu hướng mực nước trong 24h. Nhãn dự báo thông minh (Tăng/Giảm/Đạt đỉnh). Hiển thị độ tin cậy của mô hình.
- **Minh họa:**
<img width="335" height="676" alt="image" src="https://github.com/user-attachments/assets/85719f5f-7af0-45c0-8438-3099c0239ad0" />
<img width="336" height="680" alt="image" src="https://github.com/user-attachments/assets/7151f7c2-581e-4f03-99ab-0333dcaf66de" />

### 5. Màn hình Lịch sử & Thông báo (History & Notifications)
- **Mục đích:** Tra cứu dữ liệu quá khứ và nhật ký cảnh báo.
- **Tính năng:** Biểu đồ lịch sử mực nước theo các giờ.
- **Minh họa:**
<img width="229" height="462" alt="image" src="https://github.com/user-attachments/assets/5aeb79d7-b842-4eb6-ba05-b5e58b27f6fe" />
<img width="228" height="459" alt="image" src="https://github.com/user-attachments/assets/1a7153fe-77e6-4a03-93b6-4211dcbba6d9" />
<img width="222" height="399" alt="image" src="https://github.com/user-attachments/assets/6bdbd52d-fc04-4bc1-be64-3d29a1576606" />

### 6. Màn hình Cài đặt (Setting Screen)
- **Mục đích:** Chứa các cài đặt của app và hướng dẫn sử dụng.
- **Tính năng:** Tính năng đa ngôn ngữ, điều hướng tới cộng đồng Telegram.
- **Minh họa:**
<img width="720" height="1600" alt="Screenshot_20260517_211834" src="https://github.com/user-attachments/assets/d43fdf91-fa51-4aaa-8b8e-aeda6ce1798c" />
<img width="720" height="1600" alt="Screenshot_20260517_211904" src="https://github.com/user-attachments/assets/885c24f0-c9fa-48bc-89da-61efea60f963" />
<img width="720" height="1600" alt="Screenshot_20260517_211916" src="https://github.com/user-attachments/assets/bbc0f115-8a75-4751-99e3-35ea45f0f187" />

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
