const admin = require('firebase-admin');
admin.initializeApp();

const processDataModule = require('./processData');
const sendFCMModule = require('./sendFCM');
const sendTelegramModule = require('./sendTelegram');

exports.processFloodData = processDataModule.processFloodData;
exports.sendFCMAlert = sendFCMModule.sendFCMAlert;
exports.sendTelegramAlert = sendTelegramModule.sendTelegramAlert;