const functions = require("firebase-functions/v1");
const admin = require('firebase-admin');
admin.initializeApp();

exports.processFloodData = functions.database.ref('/stations/{stationId}/data')
    .onWrite(async (change, context) => {
        const data = change.after.val();
        if (!data) return null;

        const stationId = context.params.stationId;

        const configSnap = await admin.database().ref(`/stations/${stationId}/config`).once('value');
        const config = configSnap.val();
        if (!config) return null;

        const currentLevel = (config.calibrationOffset || 0) - (data.distanceRaw || 0);
        const alertStateRef = admin.database().ref(`/stations/${stationId}/alertState`);
        const alertStateSnap = await alertStateRef.once('value');
        const alertState = alertStateSnap.val() || {};

        const lastWaterLevel = alertState.lastWaterLevel !== undefined ? alertState.lastWaterLevel : -1.0;

        let isInvalid = false;

        if (currentLevel < 0) {
            isInvalid = true;
        } else if (lastWaterLevel >= 0 && (currentLevel - lastWaterLevel) >= 15.0) {
            isInvalid = true;
        } else if (lastWaterLevel >= 0 && Math.abs(currentLevel - lastWaterLevel) < 5.0) {
            isInvalid = false;
        }

        if (isInvalid) {
            console.log(`Bỏ qua dữ liệu nhiễu trạm ${stationId}. Mực nước đo được: ${currentLevel}, cũ: ${lastWaterLevel}`);
            return null;
        }

        const danger = config.dangerThreshold || 0.0;
        const warning = config.warningThreshold || 0.0;

        let currentStatus = "SAFE";
        let logType = 0;
        let title = "";
        let message = "";

        if (currentLevel >= danger) {
            currentStatus = "DANGER";
            logType = 2;
            title = "BÁO ĐỘNG ĐỎ!";
            message = `${config.name} vượt mức NGUY HIỂM. Mực nước: ${currentLevel}cm`;
        } else if (currentLevel >= warning) {
            currentStatus = "WARNING";
            logType = 1;
            title = "CẢNH BÁO NƯỚC DÂNG";
            message = `${config.name} vượt mức CẢNH BÁO. Mực nước: ${currentLevel}cm`;
        } else {
            await alertStateRef.update({
                lastWaterLevel: currentLevel
            });
            await alertStateRef.child('lastStatus').remove();
            await alertStateRef.child('lastAlertTime').remove();
            return null;
        }

        const lastStatus = alertState.lastStatus || "SAFE";
        const lastAlertTime = alertState.lastAlertTime || 0;
        const currentTime = Date.now();

        const COOLDOWN_MS = 30 * 60 * 1000;

        const isStatusChanged = currentStatus !== lastStatus;
        const isCooldownPassed = (currentTime - lastAlertTime) > COOLDOWN_MS;

        if (!isStatusChanged && !isCooldownPassed) {
            console.log(`Bỏ qua gửi trạm ${stationId}: Đang ở trạng thái ${currentStatus}, chờ hết ${COOLDOWN_MS/60000} phút.`);
            await alertStateRef.update({ lastWaterLevel: currentLevel });
            return null;
        }

        await alertStateRef.update({
            lastStatus: currentStatus,
            lastAlertTime: currentTime,
            lastWaterLevel: currentLevel
        });

        const logRef = admin.database().ref('/notification_logs').push();
        await logRef.set({
            title: title,
            message: message,
            type: logType,
            timestamp: admin.database.ServerValue.TIMESTAMP,
            isRead: false
        });

        const payload = {
            topic: "flood_warning",
            data: { title: title, body: message, status: currentStatus }
        };

        try {
            await admin.messaging().send(payload);
            console.log(`Đã gửi FCM & Log cho trạm ${stationId} (Lý do: ${isStatusChanged ? "Đổi trạng thái" : "Hết Cooldown"})`);
        } catch (error) {
            console.error("Lỗi khi gửi FCM:", error);
        }

        return null;
    });