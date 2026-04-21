const functions = require("firebase-functions/v1");
const admin = require('firebase-admin');

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
        } else if (lastWaterLevel >= 0 && Math.abs(currentLevel - lastWaterLevel) >= 15.0) {
            isInvalid = true;
        }

        if (isInvalid) {
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
            title = "CẢNH BÁO CỰC KỲ NGUY HIỂM!!!";
            message = `Trạm ${config.name} vượt mức CỰC KỲ NGUY HIỂM. Mực nước hiện tại: ${currentLevel}cm. Yêu cầu người dân nhanh chóng sơ tán khẩn cấp!`;
        } else if (currentLevel >= warning) {
            currentStatus = "WARNING";
            logType = 1;
            title = "CẢNH BÁO NGUY HIỂM";
            message = `Trạm ${config.name} vượt mức CẢNH BÁO. Mực nước hiện tại: ${currentLevel}cm. Đề nghị người dân chủ động các phương án phòng chống lũ.`;
        } else {
            await alertStateRef.update({ lastWaterLevel: currentLevel });
            await alertStateRef.child('lastStatus').remove();
            return null;
        }

        const lastStatus = alertState.lastStatus || "SAFE";
        const lastAlertTime = alertState.lastAlertTime || 0;
        const currentTime = Date.now();
        const COOLDOWN_MS = 30 * 60 * 1000;

        if (currentStatus === lastStatus && (currentTime - lastAlertTime) <= COOLDOWN_MS) {
            await alertStateRef.update({ lastWaterLevel: currentLevel });
            return null;
        }

        await alertStateRef.update({ lastStatus: currentStatus, lastAlertTime: currentTime, lastWaterLevel: currentLevel });

        const logRef = admin.database().ref('/notification_logs').push();
        await logRef.set({
            stationId: stationId,
            title: title,
            message: message,
            status: currentStatus,
            type: logType,
            timestamp: admin.database.ServerValue.TIMESTAMP,
            isRead: false
        });

        console.log(`Đã tạo log cảnh báo cho trạm ${stationId}`);
        return null;
    });