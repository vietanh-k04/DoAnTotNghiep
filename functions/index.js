const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendFloodAlert = functions.database.ref('/notification_logs/{logId}')
    .onCreate(async (snapshot, context) => {
        const logData = snapshot.val();
        console.log("Có thông báo mới:", logData);

        const payload = {
            notification: {
                title: logData.title || "Cảnh báo ngập lụt",
                body: logData.message || "Có thông báo mới từ trạm quan trắc.",
                sound: "default"
            },
            data: {
                logType: String(logData.type || "1"), 
                logId: context.params.logId
            }
        };

        try {
            const response = await admin.messaging().sendToTopic("flood_warning", payload);
            console.log("Đã gửi Push Notification thành công:", response);
            return null;
        } catch (error) {
            console.error("Lỗi khi gửi Push Notification:", error);
            return null;
        }
    });