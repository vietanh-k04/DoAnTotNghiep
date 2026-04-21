"""
FloodGuard AI Server — chạy 24/7 trên Railway.app
=====================================================
Flow:
  1. Khởi động: download model + scalers từ Firebase Storage
  2. Mỗi 5 phút: đọc tất cả stations từ Firebase RTDB
  3. Mỗi station: lấy 24 log gần nhất, chạy 288 bước TFLite inference
  4. Ghi kết quả vào /stations/{id}/ai_result để app Android đọc
  5. Nếu dự báo ngập + qua cooldown → gửi Telegram + FCM + ghi notification_log
"""

import math
import os
import time
import uuid
import schedule
from datetime import datetime, timedelta

import firebase_admin
from firebase_admin import credentials, db, storage

from predictor import FloodPredictor

# ─── Cấu hình (đọc từ Environment Variables) ──────────────────────────────
FIREBASE_DB_URL      = os.environ["FIREBASE_DATABASE_URL"]
FIREBASE_STORAGE_BUCKET = os.environ.get("FIREBASE_STORAGE_BUCKET",
                                         "doantotnghiep-aee96.firebasestorage.app")
COOLDOWN_HOURS       = 2
CHECK_INTERVAL       = 5       # phút
NUM_STEPS            = 288     # 288 × 5 min = 24h

LOCAL_MODEL_PATH     = "/tmp/urban_flood_model.h5"
LOCAL_SCALERS_PATH   = "/tmp/scalers.pkl"

# ─── Bước 1: Khởi tạo Firebase (DB + Storage) ─────────────────────────────
# Hỗ trợ 2 cách load credentials:
#   - GOOGLE_CREDENTIALS_JSON: toàn bộ JSON dưới dạng string (dùng trên Railway)
#   - SERVICE_ACCOUNT_PATH: đường dẫn tới file .json (dùng khi dev local)
import json as _json

_cred_json = os.environ.get("GOOGLE_CREDENTIALS_JSON")
if _cred_json:
    cred = credentials.Certificate(_json.loads(_cred_json))
    print("[Main] Dùng credentials từ environment variable.")
else:
    _path = os.environ.get("SERVICE_ACCOUNT_PATH", "serviceAccountKey.json")
    cred  = credentials.Certificate(_path)
    print(f"[Main] Dùng credentials từ file: {_path}")

firebase_admin.initialize_app(cred, {
    "databaseURL":   FIREBASE_DB_URL,
    "storageBucket": FIREBASE_STORAGE_BUCKET,
})
print("[Main] Firebase Admin SDK khởi tạo thành công.")

# ─── Bước 2: Download model + scalers từ Firebase Storage ─────────────────
def download_from_storage():
    print("[Main] Đang tải model và scalers từ Firebase Storage...")
    bucket = storage.bucket()

    bucket.blob("urban_flood_model.h5").download_to_filename(LOCAL_MODEL_PATH)
    print(f"[Main] ✅ Đã tải urban_flood_model.h5 → {LOCAL_MODEL_PATH}")

    bucket.blob("scalers.pkl").download_to_filename(LOCAL_SCALERS_PATH)
    print(f"[Main] ✅ Đã tải scalers.pkl → {LOCAL_SCALERS_PATH}")

download_from_storage()

# ─── Bước 3: Khởi tạo predictor ───────────────────────────────────────────
predictor = FloodPredictor(LOCAL_MODEL_PATH, LOCAL_SCALERS_PATH)
print("[Main] Model TFLite + Scalers sẵn sàng.")


# ─── Cooldown: lưu trong stations/{id}/alertState để không tạo node riêng ──
def _get_cooldown_ok(station_id: str) -> bool:
    alert_ref = db.reference(f"stations/{station_id}/alertState")
    
    # Dọn dẹp các trường cũ nếu có (lastAlertMs, lastAiAlertTime) và node root bị thừa
    db.reference("flood_alert_state").delete()
    db.reference(f"stations/{station_id}/alertState/lastAlertMs").delete()
    db.reference(f"stations/{station_id}/alertState/lastAiAlertTime").delete()

    last_ms   = alert_ref.child("lastAlertTime").get() or 0
    elapsed_h = (time.time() * 1000 - last_ms) / 3_600_000
    return elapsed_h >= COOLDOWN_HOURS

def _set_last_alert(station_id: str, water_level_cm: float = 0.0):
    now_ms = int(time.time() * 1000)
    db.reference(f"stations/{station_id}/alertState").update({
        "lastAlertTime":  now_ms,
        "lastWaterLevel": round(water_level_cm, 1),
    })


# ─── Helpers: Xử lý dữ liệu sensor ───────────────────────────────────────
def _to_ms(ts: int) -> int:
    return ts * 1000 if ts < 10_000_000_000 else ts

def _pad_logs(logs: list[dict]) -> list[dict]:
    if len(logs) >= 24:
        return logs
    missing  = 24 - len(logs)
    first    = logs[0]
    first_ts = _to_ms(first.get("timestamp", 0) or 0)
    step_ms  = 300_000
    padded   = [
        {**first, "timestamp": (first_ts - (missing - i) * step_ms) // 1000}
        for i in range(missing)
    ]
    return padded + logs

def _logs_to_historical(logs: list[dict], offset: int) -> list[dict]:
    result = []
    for log in logs:
        ts_ms       = _to_ms(log.get("timestamp", 0) or 0)
        dt          = datetime.fromtimestamp(ts_ms / 1000)
        decimal_h   = dt.hour + dt.minute / 60.0

        raw_dist       = log.get("distanceRaw", 0) or 0
        water_level_cm = max(0.0, float(offset - raw_dist))

        rain_val    = float(log.get("rainVal", 1024) or 1024)
        rain_frac   = (1024.0 - rain_val) / 1024.0
        rainfall_cm = 0.0 if rain_val > 900 else rain_frac * 1.5

        result.append({
            "rainfall_cm":    rainfall_cm,
            "water_volume":   water_level_cm * 10.0,
            "hr_sin":         math.sin(2 * math.pi * decimal_h / 24),
            "hr_cos":         math.cos(2 * math.pi * decimal_h / 24),
            "water_level_cm": water_level_cm,
        })
    return result


# ─── Timeframes: tên → số bước inference ─────────────────────────────────
TIMEFRAMES = {"1h": 12, "6h": 72, "12h": 144, "24h": 288}


def _build_timeframe(predictions_slice: list[float], danger: float,
                     last_ts_ms: int, current_level_cm: float) -> dict:
    """
    Build predictedLevels (6 điểm chart) và predictions (4 thẻ) cho một mốc thời gian.
    predictions_slice: phần predictions đã được cắt theo mốc (vd: [:12] cho 1h)
    """
    n = len(predictions_slice)
    if n == 0:
        return {"predictedLevels": [], "predictions": []}

    # 6 điểm chart (evenly sampled)
    step       = max(1, n // 5)
    levels_chart = [predictions_slice[i] for i in range(0, n, step) if i < n][:5]
    levels_chart.append(predictions_slice[-1])

    # 4 thẻ dự báo (evenly sampled từ slice này)
    card_step    = max(1, n // 4)
    card_indices = list(range(0, n, card_step))[:4]
    max_idx      = max(card_indices, key=lambda i: predictions_slice[i])
    prev_lvl     = current_level_cm
    cards        = []
    base_dt      = datetime.fromtimestamp(last_ts_ms / 1000)

    for idx in card_indices:
        dt_card     = base_dt + timedelta(minutes=(idx + 1) * 5)
        level       = predictions_slice[idx]
        is_peak     = (idx == max_idx) and (level > current_level_cm)
        is_critical = level >= danger
        diff        = level - prev_lvl

        if is_critical:
            status = "CẢNH BÁO"
        elif is_peak:
            status = "ĐẠT ĐỈNH"
        elif abs(diff) < 0.5:
            status = "ỔN ĐỊNH"
        elif diff > 0:
            status = "TĂNG LÊN"
        else:
            status = "RÚT XUỐNG"

        cards.append({
            "time":       dt_card.strftime("%H:%M"),
            "level":      round(level, 1),
            "status":     status,
            "isPeak":     is_peak,
            "isCritical": is_critical,
        })
        prev_lvl = level

    return {
        "predictedLevels": [round(v, 1) for v in levels_chart],
        "predictions":     cards,
    }


def _build_ai_result(predictions_cm: list[float], danger: float,
                     last_ts_ms: int, current_level_cm: float) -> dict:
    """Build kết quả đầy đủ cho cả 4 mốc thời gian."""

    # Build từng timeframe từ đoạn đầu của predictions_cm
    timeframes = {
        name: _build_timeframe(predictions_cm[:steps], danger, last_ts_ms, current_level_cm)
        for name, steps in TIMEFRAMES.items()
    }

    # Alert: tìm bước đầu tiên vượt ngưỡng trong toàn bộ 288 bước
    alert_minutes = None
    alert_level   = None
    for i, lvl in enumerate(predictions_cm):
        if lvl >= danger:
            alert_minutes = (i + 1) * 5
            alert_level   = round(lvl, 1)
            break

    return {
        "updatedAt":    int(time.time() * 1000),
        "alertMinutes": alert_minutes,
        "alertLevel":   alert_level,
        "timeframes":   timeframes,
    }


# ─── Xử lý từng station ───────────────────────────────────────────────────
def process_station(station_id: str, config: dict):
    try:
        offset  = int(config.get("calibrationOffset") or 400)
        danger  = float(config.get("dangerThreshold")  or 350.0)
        name    = config.get("name") or station_id

        logs_raw = (
            db.reference(f"stations/{station_id}/logs")
              .order_by_child("timestamp")
              .limit_to_last(24)
              .get()
        )
        if not logs_raw:
            print(f"[{station_id}] Không có log.")
            return

        logs = sorted(logs_raw.values(), key=lambda x: x.get("timestamp", 0))
        logs = _pad_logs(logs)
        hist = _logs_to_historical(logs, offset)

        last_ts = _to_ms(logs[-1].get("timestamp", 0) or 0)

        print(f"[{station_id}] Đang chạy {NUM_STEPS} bước inference...")
        t0           = time.time()
        predictions  = predictor.predict_steps(hist, last_ts, NUM_STEPS)
        print(f"[{station_id}] ✅ Inference xong trong {time.time() - t0:.1f}s")

        current_level = hist[-1]["water_level_cm"]
        ai_result     = _build_ai_result(predictions, danger, last_ts, current_level)

        db.reference(f"stations/{station_id}/ai_result").set(ai_result)
        print(f"[{station_id}] Đã ghi ai_result lên RTDB.")

        # Cảnh báo
        if ai_result["alertMinutes"] and _get_cooldown_ok(station_id):
            minutes = ai_result["alertMinutes"]
            level   = ai_result["alertLevel"]

            # Xác định mức độ: dựa vào alertLevel so với dangerThreshold
            is_danger = level >= danger
            fcm_status = "DANGER" if is_danger else "WARNING"
            icon       = "🚨" if is_danger else "⚠️"

            # Nội dung ngắn → FCM body & Telegram ngắn
            fcm_title = f"{icon} CẢNH BÁO NGẬP LỤT — Trạm {name}"
            fcm_body  = f"Dự báo đạt {level}cm trong {minutes} phút nữa!"
            # Nội dung dài → sendTelegram.js sẽ dùng field "message_long"
            tele_long = (
                f"{icon} <b>DỰ BÁO NGẬP LỤT — Trạm {name}</b>\n\n"
                f"📍 Dự kiến mực nước đạt <b>{level}cm</b> "
                f"(ngưỡng báo động: {danger:.0f}cm)\n"
                f"⏱ Trong khoảng <b>{minutes} phút</b> nữa\n\n"
                f"🚨 Hãy chuẩn bị ứng phó ngay!"
            )
            # KHÔNG gọi notifier trực tiếp — Firebase Functions đã trigger
            # từ notification_logs, gọi thêm sẽ gửi 2 lần!
            # Ghi lastAlertMs + lastWaterLevel vào stations/{id}/alertState
            _set_last_alert(station_id, water_level_cm=current_level)

            # Ghi log → Firebase Functions (sendTelegram.js + sendFCM.js)
            # tự trigger và gửi thông báo — chỉ 1 lần duy nhất
            db.reference("notification_logs").push({
                "id":           str(uuid.uuid4()),
                "title":        fcm_title,
                "message":      fcm_body,       # ngắn → FCM + fallback Telegram
                "message_long": tele_long,      # dài  → sendTelegram.js ưu tiên
                "status":       fcm_status,     # "DANGER" / "WARNING"
                "type":         2,
                "timestamp":    int(time.time() * 1000),
                "isRead":       False,
            })
            print(f"[{station_id}] 🚨 Đã ghi log — Firebase Functions sẽ gửi alert!")
        else:
            print(f"[{station_id}] ✅ An toàn hoặc còn trong cooldown.")

    except Exception as e:
        print(f"[{station_id}] ❌ LỖI: {e}")
        import traceback; traceback.print_exc()


# ─── Job định kỳ ──────────────────────────────────────────────────────────
def check_all_stations():
    print(f"\n{'='*55}")
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Bắt đầu kiểm tra...")
    try:
        stations_raw = db.reference("stations").get()
        if not stations_raw:
            print("[Main] Không tìm thấy station nào.")
            return
        for station_id, station_data in stations_raw.items():
            process_station(station_id, station_data.get("config") or {})
    except Exception as e:
        print(f"[Main] ❌ Lỗi load stations: {e}")


# ─── Entry point ──────────────────────────────────────────────────────────
if __name__ == "__main__":
    print(f"[Main] FloodGuard AI Server khởi động. Kiểm tra mỗi {CHECK_INTERVAL} phút.")
    check_all_stations()
    schedule.every(CHECK_INTERVAL).minutes.do(check_all_stations)
    while True:
        schedule.run_pending()
        time.sleep(30)
