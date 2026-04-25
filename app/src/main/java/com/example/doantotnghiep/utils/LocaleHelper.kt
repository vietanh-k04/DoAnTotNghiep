package com.example.doantotnghiep.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {
    fun setLocale(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getLocale(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            locales.get(0)?.language ?: "vi"
        } else {
            "vi"
        }
    }
}
