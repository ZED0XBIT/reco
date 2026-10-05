package com.example.ui.screens.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.legal.LegalArticle
import com.example.legal.TunisianLegalSources
import com.example.ui.theme.Neutral500

enum class LegalTab {
    LEGAL_FRAMEWORK,
    TRANSPARENCY,
    WHAT_CAN_PROVE,
    INCIDENT_GUIDANCE,
    GLOSSARY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalDetailSheet(
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = LegalTab.values()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("legal_detail_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Policy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "مركز الشفافية والإطار القانوني"
                            AppLanguage.FRENCH -> "Transparence & Cadre Juridique"
                            AppLanguage.ENGLISH -> "Transparency & Legal Center"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_legal_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Tab Navigation Chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val title = when (tab) {
                        LegalTab.LEGAL_FRAMEWORK -> when (lang) {
                            AppLanguage.ARABIC -> "النصوص القانونية"
                            AppLanguage.FRENCH -> "Lois tunisiennes"
                            AppLanguage.ENGLISH -> "Tunisian Law"
                        }
                        LegalTab.TRANSPARENCY -> when (lang) {
                            AppLanguage.ARABIC -> "الشفافية والبيانات"
                            AppLanguage.FRENCH -> "Transparence"
                            AppLanguage.ENGLISH -> "Transparency"
                        }
                        LegalTab.WHAT_CAN_PROVE -> when (lang) {
                            AppLanguage.ARABIC -> "ماذا يثبت التطبيق؟"
                            AppLanguage.FRENCH -> "Que prouve l'app ?"
                            AppLanguage.ENGLISH -> "What can this prove?"
                        }
                        LegalTab.INCIDENT_GUIDANCE -> when (lang) {
                            AppLanguage.ARABIC -> "إرشادات السلامة"
                            AppLanguage.FRENCH -> "Sécurité personnelle"
                            AppLanguage.ENGLISH -> "Safety Guidance"
                        }
                        LegalTab.GLOSSARY -> when (lang) {
                            AppLanguage.ARABIC -> "دليل المصطلحات"
                            AppLanguage.FRENCH -> "Glossaire"
                            AppLanguage.ENGLISH -> "Glossary"
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                0.5.dp,
                                if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (tabs[selectedTab]) {
                LegalTab.LEGAL_FRAMEWORK -> LegalFrameworkTab(lang)
                LegalTab.TRANSPARENCY -> TransparencyTab(lang)
                LegalTab.WHAT_CAN_PROVE -> WhatCanProveTab(lang)
                LegalTab.INCIDENT_GUIDANCE -> IncidentGuidanceTab(lang)
                LegalTab.GLOSSARY -> GlossaryTab(lang)
            }
        }
    }
}

@Composable
private fun LegalFrameworkTab(lang: AppLanguage) {
    val articles = TunisianLegalSources.getArticles(lang)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(14.dp)
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "هذه المعلومات قانونية عامة ومستندة إلى النصوص الرسمية التونسية. هي لا تغني عن استشارة محامٍ مرسم لدى الهيئة الوطنية للمحامين بتونس للحصول على استشارة خاصة بحالتك."
                        AppLanguage.FRENCH -> "Ces informations sont générales et tirées des textes officiels tunisiens. Elles ne constituent pas un conseil juridique personnalisé. Consultez un avocat pour toute situation particulière."
                        AppLanguage.ENGLISH -> "This legal information is general and derived from official Tunisian texts. It is not individualized legal counsel. Consult a qualified Tunisian lawyer for individual legal guidance."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(articles) { article ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral500,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (article.keyTakeaways.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    article.keyTakeaways.forEach { takeaway ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "—",
                                color = Neutral500,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = takeaway,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransparencyTab(lang: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TransparencyCard(
                title = when (lang) {
                    AppLanguage.ARABIC -> "تقليل جمع المعطيات (Data Minimization)"
                    AppLanguage.FRENCH -> "Minimisation des données"
                    AppLanguage.ENGLISH -> "Data Minimization Principle"
                },
                content = when (lang) {
                    AppLanguage.ARABIC -> "لا يطلب التطبيق اسمك الحقيقي، ولا رقم بطاقة التعريف الوطنية، ولا عنوانك الشخصي، ولا رقم هاتفك. يتم حفظ التسجيلات محلياً على هاتفك فقط حتى تختار بنفسك نشرها."
                    AppLanguage.FRENCH -> "Nous ne collectons ni nom réel, ni numéro de CIN, ni adresse, ni téléphone. Les enregistrements restent strictement locaux sur l'appareil jusqu'à publication volontaire."
                    AppLanguage.ENGLISH -> "We do not request real names, national ID numbers, home addresses, or phone numbers. Audio recordings stay strictly on your local device until you consciously choose to publish."
                }
            )
        }

        item {
            TransparencyCard(
                title = when (lang) {
                    AppLanguage.ARABIC -> "ما هو علني مقابل ما هو خاص"
                    AppLanguage.FRENCH -> "Données publiques vs privées"
                    AppLanguage.ENGLISH -> "Public vs Private Information"
                },
                content = when (lang) {
                    AppLanguage.ARABIC -> "العلني: الملف الصوتي المنشور، البصمة الرقمية SHA-256، مدة التسجيل، اسم المستخدم، التاريخ، والتصنيف. الخاص: كلمة المرور المشفرة بملح آمن، التسجيلات غير المنشورة، وقائمة الأدلة المحفوظة لديك."
                    AppLanguage.FRENCH -> "Public : audio publié, empreinte SHA-256, durée, pseudonyme, date et catégorie. Privé : mot de passe salé et haché, brouillons locaux, favoris privés."
                    AppLanguage.ENGLISH -> "Public: published audio, SHA-256 hash, duration, username, timestamps, category. Private: securely salted password hash, local drafts, saved evidence bookmarks."
                }
            )
        }

        item {
            TransparencyCard(
                title = when (lang) {
                    AppLanguage.ARABIC -> "كيف يعمل نظام الإبلاغ والرقابة؟"
                    AppLanguage.FRENCH -> "Système de signalement et modération"
                    AppLanguage.ENGLISH -> "Reporting & Moderation System"
                },
                content = when (lang) {
                    AppLanguage.ARABIC -> "يتيح النظام للمستخدمين الإبلاغ عن محتويات تنتهك الخصوصية أو تحتوي على ثلب أو تحريض. تخضع البلاغات لمراجعة موثقة في سجل تدقيق غير قابل للتعديل (Audit Log) دون إطلاق أحكام مسبقة."
                    AppLanguage.FRENCH -> "Les utilisateurs peuvent signaler les infractions à la vie privée, diffamation ou menaces. Chaque décision de modération est enregistrée de façon immuable dans un registre d'audit."
                    AppLanguage.ENGLISH -> "Users can report content violating privacy, containing defamation or doxxing. Every moderation action is recorded in an immutable append-only audit log with documented reasoning."
                }
            )
        }
    }
}

@Composable
private fun WhatCanProveTab(lang: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "ما يمكن للمنصة إثباته تقنياً"
                            AppLanguage.FRENCH -> "Ce que la plateforme peut établir"
                            AppLanguage.ENGLISH -> "What this platform CAN establish"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val canPoints = when (lang) {
                    AppLanguage.ARABIC -> listOf(
                        "أن ملفاً صوتياً محدداً تم تسجيله وحفظه في تاريخ وتوقيت معين.",
                        "أن البصمة الرقمية (SHA-256) للملف الأصلي لم تتغير ولم تخضع للتعديل بعد التسجيل.",
                        "مدة التسجيل وحجمه بدقة البايت.",
                        "أن صاحب الحساب نشر هذا التسجيل بكامل إرادته."
                    )
                    AppLanguage.FRENCH -> listOf(
                        "Qu'un fichier audio spécifique a été créé à un instant horodaté.",
                        "Que son empreinte SHA-256 certifie l'intégrité et l'absence de modification post-sauvegarde.",
                        "La durée et la taille exacte du fichier.",
                        "Que le titulaire du compte a volontairement publié cet enregistrement."
                    )
                    AppLanguage.ENGLISH -> listOf(
                        "That a specific audio file was preserved at an exact recorded timestamp.",
                        "That the cryptographic SHA-256 hash confirms the bitstream was not altered post-upload.",
                        "The precise byte size and playback duration.",
                        "That the account holder voluntarily published this evidence item."
                    )
                }

                canPoints.forEach { pt ->
                    Text(
                        text = "✓ $pt",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "ما لا تستطيع المنصة إثباته آلياً"
                            AppLanguage.FRENCH -> "Ce que la plateforme ne peut PAS établir"
                            AppLanguage.ENGLISH -> "What this platform CANNOT establish"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val cannotPoints = when (lang) {
                    AppLanguage.ARABIC -> listOf(
                        "هوية الأشخاص المتحدثين في التسجيل (تتطلب معاينة قضائية أو اعترافاً).",
                        "صدق أو كذب الادعاءات الواردة في وصف صاحب التسجيل.",
                        "هل تم قص أو اقتطاع أجزاء من الحديث قبل بدء التسجيل.",
                        "هل يشكل الفعل الموثق جريمة قانونية أو إخلالاً مهنياً (هذا اختصاص القضاء فقط)."
                    )
                    AppLanguage.FRENCH -> listOf(
                        "L'identité certaine des interlocuteurs (nécessite une expertise judiciaire).",
                        "La véracité des allégations formulées dans la description de l'auteur.",
                        "L'absence d'éléments de contexte préexistants non enregistrés.",
                        "La culpabilité légale d'une personne ou institution (compétence exclusive des tribunaux)."
                    )
                    AppLanguage.ENGLISH -> listOf(
                        "The verified real-world identity of speaking parties (requires judicial forensics).",
                        "The truth of allegations or assertions in the uploader's narrative description.",
                        "Whether selective recording omitted crucial prior contextual interaction.",
                        "Whether an offense occurred or legal guilt was incurred (solely within court jurisdiction)."
                    )
                }

                cannotPoints.forEach { pt ->
                    Text(
                        text = "✕ $pt",
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IncidentGuidanceTab(lang: AppLanguage) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GuidanceCard(
                phase = when (lang) {
                    AppLanguage.ARABIC -> "1. قبل الواقعة: السلامة أولاً"
                    AppLanguage.FRENCH -> "1. Avant : La sécurité prime"
                    AppLanguage.ENGLISH -> "1. Before: Safety First"
                },
                body = when (lang) {
                    AppLanguage.ARABIC -> "سلامتك الجسدية أهم من أي تسجيل. لا تعرض نفسك للخطر أبداً لجمع دليل. تأكد من أن الهاتف في وضع آمن وسهل الوصول دون لفت انتباه مستفز."
                    AppLanguage.FRENCH -> "Votre intégrité physique est primordiale. Ne vous mettez jamais en danger pour obtenir un enregistrement. Gardez votre calme."
                    AppLanguage.ENGLISH -> "Your personal safety overrides any evidence capture. Never place yourself in danger to obtain a recording. Keep calm and avoid confrontation."
                }
            )
        }

        item {
            GuidanceCard(
                phase = when (lang) {
                    AppLanguage.ARABIC -> "2. أثناء الواقعة: لا تستفز ولا تعطل"
                    AppLanguage.FRENCH -> "2. Pendant : Pas de provocation"
                    AppLanguage.ENGLISH -> "2. During: Do Not Obstruct"
                },
                body = when (lang) {
                    AppLanguage.ARABIC -> "التزم بالهدوء التام والاحترام. لا تعطل سير المرفق العام أو عمل الموظف (الفصل 125 مجلة جزائية). دع التوثيق يتم بهدوء دون الدخول في مشادات كلامية."
                    AppLanguage.FRENCH -> "Conservez une attitude courtoise. Ne perturbez pas le service public (Art. 125 Code Pénal). Laissez le son s'enregistrer sans surenchère verbale."
                    AppLanguage.ENGLISH -> "Maintain civility. Never obstruct public administration or provoke officials (Tunisian Penal Code Art. 125). Allow the audio to preserve interactions without verbal escalation."
                }
            )
        }

        item {
            GuidanceCard(
                phase = when (lang) {
                    AppLanguage.ARABIC -> "3. بعد الواقعة: احفظ وراجع بمسؤولية"
                    AppLanguage.FRENCH -> "3. Après : Préservez et réfléchissez"
                    AppLanguage.ENGLISH -> "3. After: Review Responsibly"
                },
                body = when (lang) {
                    AppLanguage.ARABIC -> "احفظ الملف محلياً وتأكد من سلامة البصمة الرقمية. راجع التسجيل بعقلانية. تجنب نشر المعطيات الخاصة أو توجيه تهم شخصية قد تعرضك لعقوبات الثلب (المرسوم 115)."
                    AppLanguage.FRENCH -> "Vérifiez l'empreinte SHA-256 locale. Évitez toute publication impulsive de données privées d'autrui ou d'accusations diffamatoires."
                    AppLanguage.ENGLISH -> "Preserve the original audio with its SHA-256 hash. Review calmly. Avoid publishing unnecessary private data or defamatory accusations (Decree-Law 2011-115)."
                }
            )
        }
    }
}

@Composable
private fun GlossaryTab(lang: AppLanguage) {
    val items = when (lang) {
        AppLanguage.ARABIC -> listOf(
            "معرف الدليل (Evidence ID)" to "رمز فريد ومستقر (مثل PS-TN-2026-000184) يحدد التسجيل علناً دون كشف بيانات الحساب الشخصية.",
            "البصمة الرقمية (SHA-256)" to "تشفير رياضي فريد لملف الصوت يتيح إثبات عدم تعديل أو تلاعب بأي جزء من الملف بعد حفظه.",
            "البيانات الوصفية (Metadata)" to "المعلومات التقنية المرافقة للتسجيل: الحجم، المدة، الصيغة، والتاريخ.",
            "تصريح صاحب التسجيل (Uploader Statement)" to "الرواية الذاتية والوصف والتصنيف الذي يقدمه المستخدم، ولا يمثل حقيقة مؤكدة من المنصة.",
            "حقيقة المنصة (Platform Fact)" to "الأمور التقنية التي يتحقق منها النظام ذاتياً مثل التاريخ، البصمة الرقمية، وحالة النشر."
        )
        AppLanguage.FRENCH -> listOf(
            "Identifiant de preuve (Evidence ID)" to "Code unique stable (ex. PS-TN-2026-000184) permettant de partager la preuve sans exposer d'identifiants privés.",
            "Empreinte numérique (SHA-256)" to "Algorithme cryptographique garantissant que le flux audio n'a subi aucune altération après son enregistrement.",
            "Métadonnées (Metadata)" to "Données techniques rattachées au fichier : taille, durée, horodatage et encodage.",
            "Déclaration de l'auteur (Uploader Statement)" to "Récit et description fournis par l'utilisateur, n'ayant pas valeur de vérité judiciaire certifiée par l'application.",
            "Fait système (Platform Fact)" to "Élément vérifié directement par l'application : date de téléversement, statut, empreinte."
        )
        AppLanguage.ENGLISH -> listOf(
            "Evidence Identifier (Evidence ID)" to "A stable, unique civic identifier (e.g. PS-TN-2026-000184) for sharing without leaking private database keys.",
            "Cryptographic Hash (SHA-256)" to "A one-way cryptographic digest confirming the audio bitstream matches the originally captured bytes.",
            "Metadata" to "Technical context recorded alongside audio: duration, exact size, file format, timestamps.",
            "Uploader Statement" to "Subjective narrative, claims, and categorization supplied by the uploader; not verified as factual truth by the platform.",
            "Platform Fact" to "Objective elements verified directly by the system: timestamps, bitstream hashes, storage records."
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { (term, definition) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = term,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = definition,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransparencyCard(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GuidanceCard(phase: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = phase,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
