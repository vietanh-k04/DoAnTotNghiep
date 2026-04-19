const functions = require("firebase-functions/v1");
const admin = require('firebase-admin');

exports.sendFCMAlert = functions.database.ref('/notification_logs/{logId}')
    .onCreate(async (snapshot, context) => {
        const logData = snapshot.val();

        const payload = {
            topic: "flood_warning",
            data: {
                title: logData.title,
                body: logData.message,
                status: logData.status
            }
        };

        try {
            await admin.messaging().send(payload);
            console.log(`Đã gửi FCM cho logId: ${context.params.logId}`);
        } catch (error) {
            console.error("Lỗi khi gửi FCM:", error);
        }
        return null;
    });