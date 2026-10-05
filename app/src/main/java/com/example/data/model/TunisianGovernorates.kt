package com.example.data.model

import com.example.i18n.AppLanguage

data class GovernorateInfo(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val nameFr: String,
    val centerLat: Double,
    val centerLng: Double
) {
    fun localizedName(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.ARABIC -> nameAr
            AppLanguage.FRENCH -> nameFr
            AppLanguage.ENGLISH -> nameEn
        }
    }
}

object TunisianGovernorates {
    val all: List<GovernorateInfo> get() = ALL
    val ALL: List<GovernorateInfo> = listOf(
        GovernorateInfo("tunis", "Tunis", "تونس", "Tunis", 36.8065, 10.1815),
        GovernorateInfo("ariana", "Ariana", "أريانة", "Ariana", 36.8665, 10.1647),
        GovernorateInfo("ben_arous", "Ben Arous", "بن عروس", "Ben Arous", 36.7533, 10.2222),
        GovernorateInfo("manouba", "Manouba", "منوبة", "Manouba", 36.8081, 10.0972),
        GovernorateInfo("nabeul", "Nabeul", "نابل", "Nabeul", 36.4513, 10.7357),
        GovernorateInfo("zaghouan", "Zaghouan", "زغوان", "Zaghouan", 36.4029, 10.1429),
        GovernorateInfo("bizerte", "Bizerte", "بنزرت", "Bizerte", 37.2744, 9.8739),
        GovernorateInfo("beja", "Béja", "باجة", "Béja", 36.7256, 9.1817),
        GovernorateInfo("jendouba", "Jendouba", "جندوبة", "Jendouba", 36.5011, 8.7802),
        GovernorateInfo("kef", "Kef", "الكاف", "Le Kef", 36.1826, 8.7149),
        GovernorateInfo("siliana", "Siliana", "سليانة", "Siliana", 36.0844, 9.3708),
        GovernorateInfo("kairouan", "Kairouan", "القيروان", "Kairouan", 35.6781, 10.0963),
        GovernorateInfo("kasserine", "Kasserine", "القصرين", "Kasserine", 35.1676, 8.8365),
        GovernorateInfo("sidi_bouzid", "Sidi Bouzid", "سيدي بوزيد", "Sidi Bouzid", 35.0382, 9.4849),
        GovernorateInfo("sousse", "Sousse", "سوسة", "Sousse", 35.8256, 10.6369),
        GovernorateInfo("monastir", "Monastir", "المنستير", "Monastir", 35.7780, 10.8262),
        GovernorateInfo("mahdia", "Mahdia", "المهدية", "Mahdia", 35.5047, 11.0622),
        GovernorateInfo("sfax", "Sfax", "صفاقس", "Sfax", 34.7406, 10.7603),
        GovernorateInfo("gabes", "Gabès", "قابس", "Gabès", 33.8815, 10.0982),
        GovernorateInfo("medenine", "Medenine", "مدنين", "Médenine", 33.3549, 10.5055),
        GovernorateInfo("tataouine", "Tataouine", "تطاوين", "Tataouine", 32.9297, 10.4518),
        GovernorateInfo("gafsa", "Gafsa", "قفصة", "Gafsa", 34.4250, 8.7842),
        GovernorateInfo("tozeur", "Tozeur", "توزر", "Tozeur", 33.9197, 8.1335),
        GovernorateInfo("kebili", "Kebili", "قبلي", "Kébili", 33.7044, 8.9690)
    )

    fun findById(id: String): GovernorateInfo? = ALL.firstOrNull { it.id.equals(id, ignoreCase = true) }
    fun findByName(name: String): GovernorateInfo? = ALL.firstOrNull {
        it.nameEn.equals(name, ignoreCase = true) ||
        it.nameAr.equals(name, ignoreCase = true) ||
        it.nameFr.equals(name, ignoreCase = true) ||
        it.id.equals(name, ignoreCase = true)
    }
}
