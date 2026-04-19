"""
Gửi thông báo ra Telegram Bot và Firebase Cloud Messaging (FCM).
"""

import os
import time
import requests
import firebase_admin
from firebase_admin import messaging


TELEGRAM_BOT_TOKEN = os.environ.get("TELEGRAM_BOT_TOKEN", "")
TELEGRAM_CHAT_ID   = os.environ.get("TELEGRAM_CHAT_ID", "")


def send_telegram(message: str) -> bool:
    """Gửi tin nhắn text về Telegram Bot."""
    if not TELEGRAM_BOT_TOKEN or not TELEGRAM_CHAT_ID:
        print("[Notifier] Chưa cấu hình TELEGRAM_BOT_TOKEN / TELEGRAM_CHAT_ID")
        return False

    url  = f"https://api.telegram.org/bot{TELEGRAM_BOT_TOKEN}/sendMessage"
    data = {
        "chat_id":    TELEGRAM_CHAT_ID,
        "text":       message,
        "parse_mode": "HTML",
    }
    try:
        resp = requests.post(url, json=data, timeout=10)
        resp.raise_for_status()
        print(f"[Notifier] Telegram OK: {resp.status_code}")
        return True
    except Exception as e:
        print(f"[Notifier] Telegram lỗi: {e}")
        return False


def send_fcm(title: str, body: str, topic: str = "flood_warning",
             status: str = "DANGER") -> bool:
    """
    Gửi FCM push notification tới tất cả thiết bị đã subscribe topic.
    Topic 'flood_warning' khớp với MainActivity.kt: subscribeToTopic("flood_warning")
    status: "DANGER" | "WARNING" — dùng để Android phân biệt màu + rung
    """
    try:
        message = messaging.Message(
            notification=messaging.Notification(title=title, body=body),
            # data payload → MyFirebaseMessagingService đọc được dù app đang đóng
            data={
                "title":  title,
                "body":   body,
                "status": status,
            },
            android=messaging.AndroidConfig(
                priority="high",
                notification=messaging.AndroidNotification(
                    sound="default",
                    channel_id="flood_alerts",
                ),
            ),
            topic=topic,
        )
        response = messaging.send(message)
        print(f"[Notifier] FCM OK: {response}")
        return True
    except Exception as e:
        print(f"[Notifier] FCM lỗi: {e}")
        return False
