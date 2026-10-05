package com.example.i18n

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val isRtl: Boolean) {
    ARABIC("ar", "Arabic", "العربية", true),
    FRENCH("fr", "French", "Français", false),
    ENGLISH("en", "English", "English", false)
}
