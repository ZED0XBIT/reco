package com.example.ui.util

import java.util.regex.Pattern

data class SensitiveDataCheckResult(
    val hasPotentialSensitiveData: Boolean,
    val detectedTypes: List<String>,
    val warningMessageAr: String,
    val warningMessageFr: String,
    val warningMessageEn: String
)

object PrivacyCheckUtils {

    private val PHONE_PATTERN = Pattern.compile("(\\+?216[\\s.-]?)?([2459]\\d{7}|\\b\\d{8}\\b)")
    private val EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val ID_KEYWORDS = listOf("cin", "بطاقة تعريف", "carte d'identité", "b3", "passeport", "جواز سفر")

    fun inspectText(text: String): SensitiveDataCheckResult {
        if (text.isBlank()) {
            return SensitiveDataCheckResult(
                hasPotentialSensitiveData = false,
                detectedTypes = emptyList(),
                warningMessageAr = "",
                warningMessageFr = "",
                warningMessageEn = ""
            )
        }

        val detected = mutableListOf<String>()

        if (PHONE_PATTERN.matcher(text).find()) {
            detected.add("Phone number / رقم هاتف")
        }
        if (EMAIL_PATTERN.matcher(text).find()) {
            detected.add("Email address / بريد إلكتروني")
        }
        val lower = text.lowercase()
        if (ID_KEYWORDS.any { lower.contains(it) }) {
            detected.add("National ID / معطى تعريفي")
        }

        val hasData = detected.isNotEmpty()
        return SensitiveDataCheckResult(
            hasPotentialSensitiveData = hasData,
            detectedTypes = detected,
            warningMessageAr = "تنبيه خصوصية: قد يحتوي النص على معطيات شخصية (${detected.joinToString(", ")}). تأكد من تجنب نشر معطيات خاصة لا ضرورة لها.",
            warningMessageFr = "Avertissement : Le texte peut contenir des données personnelles (${detected.joinToString(", ")}). Évitez la divulgation de données privées superflues.",
            warningMessageEn = "Privacy Notice: The text may contain personal data (${detected.joinToString(", ")}). Ensure you exclude unnecessary personal information."
        )
    }
}
