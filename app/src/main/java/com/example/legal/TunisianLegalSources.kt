package com.example.legal

import com.example.i18n.AppLanguage

data class LegalArticle(
    val title: String,
    val source: String,
    val summary: String,
    val keyTakeaways: List<String>
)

object TunisianLegalSources {

    fun getArticles(lang: AppLanguage): List<LegalArticle> = when (lang) {
        AppLanguage.ARABIC -> listOf(
            LegalArticle(
                title = "حماية الحياة الخاصة والمعطيات الشخصية",
                source = "الدستور التونسي (الفصل 24 و41)",
                summary = "تحمي الدولة التونسية الحياة الخاصة وحرمة المسكن وسرية المراسلات والاتصالات والمعطيات الشخصية. لكل مواطن الحق في اختيار مقر إقامته وفي التنقل بحرية داخل الوطن وله الحق في مغادرته.",
                keyTakeaways = listOf(
                    "الحياة الخاصة محمية دستورياً ولا يجوز انتهاكها دون إذن قضائي.",
                    "تسجيل المحادثات في الفضاءات الخاصة دون علم الطرف الآخر غير مسموح قانوناً."
                )
            ),
            LegalArticle(
                title = "قانون حماية المعطيات الشخصية",
                source = "القانون الأساسي عدد 63 لسنة 2004 المؤرخ في 27 جويلية 2004",
                summary = "يضبط قواعد معالجة وتسجيل وتخزين ونشر المعطيات ذات الصبغة الشخصية، بما في ذلك التسجيلات الصوتية والصور التي تمكن من التعرف على هوية الأشخاص.",
                keyTakeaways = listOf(
                    "الصوت الذي يمكن من تحديد هوية الشخص يُعد معطى شخصياً محمياً.",
                    "نشر تسجيل صوتي لشخص دون إذنه قد يعرّض صاحبه للتتبع أمام الهيئة الوطنية لحماية المعطيات الشخصية (INPDP) أو القضاء.",
                    "المصلحة العامة والأدلة القضائية تخضع لتقدير المحاكم المختصة."
                )
            ),
            LegalArticle(
                title = "جريمة الثلب والشتم عبر وسائل النشر",
                source = "المرسوم عدد 115 لسنة 2011 المتعلق بحرية الصحافة والطباعة والنشر",
                summary = "يُعرّف الثلب بأنه نسبة أمر غير صحيح أو غير ثابت علناً إلى شخص أو هيئة يمس من شرفه أو اعتباره. ويُعرّف الشتم بكل عبارة تحقير أو سب.",
                keyTakeaways = listOf(
                    "توجيه اتهامات بالفساد أو الرشوة دون إثبات قضائي قاطع يُعرض الناشر لعقوبات الثلب الجزائية.",
                    "صفة الموظف العمومي لا تجرد الشخص من حقه في حماية شرفه وكرامته."
                )
            ),
            LegalArticle(
                title = "هضم جانب موظف عمومي أثناء أداء مهامه",
                source = "المجلة الجزائية التونسية (الفصل 125)",
                summary = "يُعاقب بالسجن كل من يهضم جانب موظف عمومي بالقول أو الإشارة أو التهديد أثناء مباشرته لوظيفته أو بمناسبة مباشرتها.",
                keyTakeaways = listOf(
                    "التوثيق الصوتي السلمي لا يجب أن يتحول إلى تعطيل للمرفق العام أو استفزاز للموظف.",
                    "التزام الهدوء والاحترام المتبادل شرط أساسي لحماية النفس قانونياً."
                )
            ),
            LegalArticle(
                title = "حجية التسجيلات الصوتية أمام القضاء",
                source = "مجلة الإجراءات الجزائية (الفصل 287)",
                summary = "تُترك مسألة تقدير قيمة الأدلة المقدمة في المادة الجزائية للاقتناع الوجداني للقاضي، مع إمكانية الطعن فيها بالتدليس أو الإخلال بشروط النزاهة.",
                keyTakeaways = listOf(
                    "التسجيل الصوتي هو قرينة أو بداية حجة تخضع للمعاينة الفنية من طرف خبراء معتمدين.",
                    "البصمة الرقمية المشفرة (Hash) تساعد في إثبات سلامة الملف بعد حفظه."
                )
            )
        )
        AppLanguage.FRENCH -> listOf(
            LegalArticle(
                title = "Protection de la vie privée et des données",
                source = "Constitution tunisienne (Articles 24 & 41)",
                summary = "L'État protège la vie privée, l'inviolabilité du domicile, le secret des correspondances et la protection des données personnelles.",
                keyTakeaways = listOf(
                    "La vie privée bénéficie d'une garantie constitutionnelle.",
                    "L'enregistrement dans la sphère privée sans consentement est illicite."
                )
            ),
            LegalArticle(
                title = "Protection des données à caractère personnel",
                source = "Loi organique n° 2004-63 du 27 juillet 2004",
                summary = "Encadre le traitement, le stockage et la diffusion de données identifiantes, incluant la voix et l'image d'individus identifiables.",
                keyTakeaways = listOf(
                    "Une voix identifiable constitue une donnée personnelle protégée.",
                    "La diffusion publique non autorisée peut être sanctionnée par l'INPDP ou les juridictions pénales.",
                    "La préservation de preuves pour la justice obéit à des règles strictes."
                )
            ),
            LegalArticle(
                title = "Diffamation et injure publique",
                source = "Décret-loi n° 2011-115 du 2 novembre 2011",
                summary = "Définit la diffamation comme toute allégation d'un fait précis portant atteinte à l'honneur ou à la considération d'une personne physique ou morale.",
                keyTakeaways = listOf(
                    "Accuser une personne d'infraction sans décision judiciaire expose à des poursuites pénales.",
                    "La critique citoyenne doit rester exempte d'injures ou d'allégations diffamatoires."
                )
            ),
            LegalArticle(
                title = "Outrage à fonctionnaire public",
                source = "Code pénal tunisien (Article 125)",
                summary = "Sanctionne d'emprisonnement quiconque outrage un fonctionnaire public dans l'exercice ou à l'occasion de l'exercice de ses fonctions.",
                keyTakeaways = listOf(
                    "La documentation ne doit ni entraver le service public ni constituer une provocation.",
                    "Conservez une attitude calme et respectueuse en toutes circonstances."
                )
            ),
            LegalArticle(
                title = "Valeur probante des enregistrements",
                source = "Code de procédure pénale (Article 287)",
                summary = "L'appréciation des preuves en matière pénale est laissée à l'intime conviction des magistrats sous réserve d'expertises d'intégrité.",
                keyTakeaways = listOf(
                    "L'enregistrement audio est soumis à l'évaluation judiciaire et technique.",
                    "L'empreinte cryptographique (Hash SHA-256) atteste de la non-altération du fichier après sauvegarde."
                )
            )
        )
        AppLanguage.ENGLISH -> listOf(
            LegalArticle(
                title = "Constitutional Privacy Protections",
                source = "Constitution of Tunisia (Articles 24 & 41)",
                summary = "Guarantees the protection of personal privacy, home inviolability, confidentiality of communications, and personal data.",
                keyTakeaways = listOf(
                    "Privacy is a constitutionally protected right in Tunisia.",
                    "Surreptitious recording in private spaces without consent violates applicable law."
                )
            ),
            LegalArticle(
                title = "Personal Data Protection Framework",
                source = "Organic Law No. 2004-63 dated 27 July 2004",
                summary = "Regulates processing, recording, and dissemination of identifying personal data, including recognizable voice recordings.",
                keyTakeaways = listOf(
                    "An identifiable human voice is recognized as personal data.",
                    "Public dissemination without legitimate legal justification may trigger regulatory or penal liability.",
                    "Submitting evidence to courts follows distinct procedural safeguards."
                )
            ),
            LegalArticle(
                title = "Defamation and Public Accusations",
                source = "Decree-Law No. 2011-115 on Freedom of Press & Publishing",
                summary = "Defines defamation as attributing specific unproven factual offenses that harm the honor or reputation of an individual or institution.",
                keyTakeaways = listOf(
                    "Publicly accusing officials of crimes without a judicial finding can constitute criminal defamation.",
                    "Responsible documentation must distinguish factual recordings from personal accusations."
                )
            ),
            LegalArticle(
                title = "Interference with Public Servants",
                source = "Tunisian Penal Code (Article 125)",
                summary = "Penalizes contempt or obstruction of public officials in or on the occasion of the performance of their official duties.",
                keyTakeaways = listOf(
                    "Preserving evidence must never disrupt public administration or provoke officials.",
                    "Maintain civility and avoid confrontation during any interaction."
                )
            ),
            LegalArticle(
                title = "Admissibility of Audio Evidence",
                source = "Code of Criminal Procedure (Article 287)",
                summary = "Admissibility and weight of evidence in penal matters is subject to judicial appraisal and judicial discretion.",
                keyTakeaways = listOf(
                    "Judges assess recordings with independent technical scrutiny and forensic verification.",
                    "Cryptographic hashing (SHA-256) helps confirm the stored file was not altered post-upload."
                )
            )
        )
    }
}
