const functions = require("firebase-functions/v1");
const axios = require('axios');

const TELEGRAM_BOT_TOKEN = "8797105678:AAEhYhusSuiuEM2-nlivooheYeDA695SxPE";
const TELEGRAM_CHAT_ID = "-1003996335714";

exports.sendTelegramAlert = functions.database.ref('/notification_logs/{logId}')
    .onCreate(async (snapshot, context) => {
        const logData = snapshot.val();

        // Ưu tiên message_long (tin dài từ AI server, format HTML đầy đủ)
        // Fallback sang message ngắn nếu không có (log từ processData.js)
        const text = logData.message_long || `<b>${logData.title}</b>\n${logData.message}`;

        const url = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`;

        try {
            await axios.post(url, {
                chat_id: TELEGRAM_CHAT_ID,
                text: text,
                parse_mode: "HTML",  // Dùng HTML vì AI server dùng thẻ <b>
            });
            console.log(`Đã gửi Telegram cho logId: ${context.params.logId}`);
        } catch (error) {
            console.error("Lỗi khi gửi Telegram:", error.response ? error.response.data : error.message);
        }
        return null;
    });