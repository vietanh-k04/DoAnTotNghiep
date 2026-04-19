const functions = require("firebase-functions/v1");
const axios = require('axios');

const TELEGRAM_BOT_TOKEN = "8797105678:AAEhYhusSuiuEM2-nlivooheYeDA695SxPE";
const TELEGRAM_CHAT_ID = "-1003996335714";

exports.sendTelegramAlert = functions.database.ref('/notification_logs/{logId}')
    .onCreate(async (snapshot, context) => {
        const logData = snapshot.val();

        const textMessage = `*${logData.title}*\n${logData.message}`;
        const url = `https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage`;

        try {
            await axios.post(url, {
                chat_id: TELEGRAM_CHAT_ID,
                text: textMessage,
                parse_mode: "Markdown"
            });
            console.log(`Đã gửi Telegram cho logId: ${context.params.logId}`);
        } catch (error) {
            console.error("Lỗi khi gửi Telegram:", error.response ? error.response.data : error.message);
        }
        return null;
    });