package com.example.ui.screens.legal

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.Neutral500

@Composable
fun LegalGateScreen(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onOpenLegalSheet: () -> Unit,
    onAcceptAndEnter: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) } // Steps 1 to 6
    var isAcknowledgedPublic by remember { mutableStateOf(false) }
    var isAcknowledgedLegal by remember { mutableStateOf(false) }
    var isAcknowledgedSafety by remember { mutableStateOf(false) }

    val allAcknowledged = isAcknowledgedPublic && isAcknowledgedLegal && isAcknowledgedSafety
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Language Selector Pills (Monochrome)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Indicator (e.g. 1/6)
                Text(
                    text = "0$currentStep / 06",
                    style = MaterialTheme.typography.labelMedium,
                    color = Neutral500,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = (currentLanguage == lang)
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
                                .clickable { onLanguageChange(lang) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("lang_chip_${lang.code}")
                        ) {
                            Text(
                                text = lang.nativeName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Pills (Steps 1 to 6)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (step in 1..6) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (step <= currentStep) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.outline
                            )
                            .clickable { currentStep = step }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "onboarding_steps"
            ) { step ->
                when (step) {
                    1 -> Step1Presentation(currentLanguage)
                    2 -> Step2CorePhilosophy(currentLanguage)
                    3 -> Step3WhatAppDoes(currentLanguage)
                    4 -> Step4PublicNotice(currentLanguage)
                    5 -> Step5LegalConsiderations(currentLanguage, onOpenLegalSheet)
                    6 -> Step6Acknowledgement(
                        currentLanguage = currentLanguage,
                        isAckPublic = isAcknowledgedPublic,
                        isAckLegal = isAcknowledgedLegal,
                        isAckSafety = isAcknowledgedSafety,
                        onTogglePublic = { isAcknowledgedPublic = !isAcknowledgedPublic },
                        onToggleLegal = { isAcknowledgedLegal = !isAcknowledgedLegal },
                        onToggleSafety = { isAcknowledgedSafety = !isAcknowledgedSafety },
                        onOpenLegalSheet = onOpenLegalSheet
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ARABIC -> "السابق"
                                AppLanguage.FRENCH -> "Précédent"
                                AppLanguage.ENGLISH -> "Back"
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStep < 6) {
                    Button(
                        onClick = { currentStep++ },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("onboarding_next_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ARABIC -> "التالي"
                                AppLanguage.FRENCH -> "Suivant"
                                AppLanguage.ENGLISH -> "Next"
                            },
                            color = MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = "Next",
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = onAcceptAndEnter,
                        enabled = allAcknowledged,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("enter_application_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ARABIC -> "دخول التطبيق"
                                AppLanguage.FRENCH -> "Accéder à l'application"
                                AppLanguage.ENGLISH -> "Enter Application"
                            },
                            color = if (allAcknowledged) MaterialTheme.colorScheme.surface else Neutral500,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Step1Presentation(lang: AppLanguage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "إحمي روحك",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Protect Yourself",
            style = MaterialTheme.typography.titleLarge,
            color = Neutral500,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "منصة مدنية لتوثيق الأدلة الصوتية، وحماية الحقوق، والشفافية المسؤولة في تونس."
                AppLanguage.FRENCH -> "Plateforme citoyenne de préservation de preuves audio, de compréhension des droits et de partage responsable en Tunisie."
                AppLanguage.ENGLISH -> "A civic audio evidence-preservation platform for understanding legal rights and practicing responsible transparency in Tunisia."
            },
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Step2CorePhilosophy(lang: AppLanguage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "فلسفة المنصة"
                AppLanguage.FRENCH -> "Philosophie fondamentale"
                AppLanguage.ENGLISH -> "Core Philosophy"
            },
            style = MaterialTheme.typography.titleSmall,
            color = Neutral500,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "وثّق.\nاحفظ.\nافهم حقوقك.\nانشر بمسؤولية."
                AppLanguage.FRENCH -> "Enregistrer.\nPréserver.\nComprendre.\nPartager responsablement."
                AppLanguage.ENGLISH -> "Record.\nPreserve.\nUnderstand.\nShare responsibly."
            },
            style = MaterialTheme.typography.displayMedium.copy(lineHeight = 44.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "الهدف هو التوثيق النزيه والموضوعي دون تشهير، وحماية النفس في كنف القانون التونسي."
                AppLanguage.FRENCH -> "L'objectif est une documentation intègre sans diffamation, dans le strict respect de la loi tunisienne."
                AppLanguage.ENGLISH -> "The goal is honest, tamper-evident civic documentation without defamation or reckless endangerment, respecting Tunisian law."
            },
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Step3WhatAppDoes(lang: AppLanguage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "ماذا يقدم التطبيق؟"
                AppLanguage.FRENCH -> "Que fait cette application ?"
                AppLanguage.ENGLISH -> "What this application does"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        val points = when (lang) {
            AppLanguage.ARABIC -> listOf(
                "تسجيل صوتي محلي آمن يعمل بدون إنترنت لحماية المحادثات الهامة.",
                "توليد بصمة رقمية مشفرة (SHA-256) فورية لكل تسجيل لإثبات عدم التلاعب بالملف.",
                "فصل قاطع بين الحقائق التقنية الموثقة وبين تصريحات صاحب التسجيل.",
                "أرشيف عام للأدلة التي يقرر أصحابها نشرها طوعياً لخدمة المصلحة العامة."
            )
            AppLanguage.FRENCH -> listOf(
                "Enregistrement audio local sécurisé fonctionnant sans connexion internet.",
                "Génération immédiate d'une empreinte cryptographique SHA-256 certifiant l'intégrité du fichier.",
                "Séparation stricte entre faits vérifiés par le système et déclarations de l'auteur.",
                "Archive citoyenne publique pour les preuves publiées volontairement pour l'intérêt général."
            )
            AppLanguage.ENGLISH -> listOf(
                "Secure on-device local recording that functions completely offline.",
                "Immediate cryptographic SHA-256 hash generation confirming bitstream integrity post-capture.",
                "Strict distinction between verified platform facts and user-stated allegations.",
                "Public evidence repository for recordings voluntarily published for civic accountability."
            )
        }

        points.forEach { point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = point,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun Step4PublicNotice(lang: AppLanguage) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "النشر العام لا رجعة فيه"
                AppLanguage.FRENCH -> "La publication publique est définitive"
                AppLanguage.ENGLISH -> "Public publication is permanent"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "تسجيلاتك تظل خاصة على جهازك حتى تقرر أنت نشرها. عندما تختار النشر العام، يصبح الدليل متاحاً للعموم مع بصمته الرقمية ولا يمكن التراجع عنه سرياً. تجنب تضمين معطيات شخصية لا علاقة لها بالموضوع."
                AppLanguage.FRENCH -> "Vos enregistrements restent privés sur votre appareil. Si vous choisissez de publier publiquement, la preuve devient accessible à tous avec son empreinte SHA-256. Évitez toute divulgation de données personnelles superflues."
                AppLanguage.ENGLISH -> "Your recordings remain strictly private on your device until you consciously choose to publish. Once published publicly, evidence becomes accessible to anyone alongside its SHA-256 hash. Always exclude irrelevant private personal data."
            },
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Step5LegalConsiderations(lang: AppLanguage, onOpenLegalSheet: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Gavel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "الإطار القانوني التونسي"
                AppLanguage.FRENCH -> "Cadre juridique tunisien"
                AppLanguage.ENGLISH -> "Tunisian Legal Framework"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "الفصل 24 من الدستور يحمي المعطيات الشخصية وحرمة الحياة الخاصة، بينما يضمن القانون حرية التعبير والإعلام (المرسوم 115). التوثيق السلمي لا يعطي الحق في تعطيل المرافق العامة (الفصل 125 مجلة جزائية) أو توجيه تهم دون دليل قاطع."
                AppLanguage.FRENCH -> "L'article 24 de la Constitution protège la vie privée et les données personnelles. La documentation pacifique ne dispense pas du respect des agents publics (Art. 125 CP) et prohibe toute accusation diffamatoire sans jugement définitif."
                AppLanguage.ENGLISH -> "Article 24 of the Tunisian Constitution protects privacy and personal data. Peaceful documentation must not obstruct public servants (Penal Code Art. 125) nor constitute defamatory allegations without judicial determination."
            },
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 23.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedButton(
            onClick = onOpenLegalSheet,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "الاطلاع على النصوص القانونية كاملة"
                    AppLanguage.FRENCH -> "Consulter les textes juridiques complets"
                    AppLanguage.ENGLISH -> "Inspect complete legal sources"
                }
            )
        }
    }
}

@Composable
private fun Step6Acknowledgement(
    currentLanguage: AppLanguage,
    isAckPublic: Boolean,
    isAckLegal: Boolean,
    isAckSafety: Boolean,
    onTogglePublic: () -> Unit,
    onToggleLegal: () -> Unit,
    onToggleSafety: () -> Unit,
    onOpenLegalSheet: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = when (currentLanguage) {
                AppLanguage.ARABIC -> "إقرار الاستخدام المسؤول"
                AppLanguage.FRENCH -> "Engagement d'usage responsable"
                AppLanguage.ENGLISH -> "Responsible Use Acknowledgement"
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = when (currentLanguage) {
                AppLanguage.ARABIC -> "يرجى الموافقة على البنود التالية قبل الدخول:"
                AppLanguage.FRENCH -> "Veuillez cocher les conditions suivantes pour continuer :"
                AppLanguage.ENGLISH -> "Please confirm the following terms before proceeding:"
            },
            style = MaterialTheme.typography.bodySmall,
            color = Neutral500
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Checkbox 1: Public publication
        AckCheckboxRow(
            checked = isAckPublic,
            onToggle = onTogglePublic,
            text = when (currentLanguage) {
                AppLanguage.ARABIC -> "أفهم أن التسجيلات المنشورة علناً تصبح متاحة للجميع ولا يمكن مسحها سرياً."
                AppLanguage.FRENCH -> "Je comprends que les enregistrements publiés deviennent publics et irréversibles."
                AppLanguage.ENGLISH -> "I understand that published recordings become public and cannot be covertly erased."
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Checkbox 2: Legal responsibility
        AckCheckboxRow(
            checked = isAckLegal,
            onToggle = onToggleLegal,
            text = when (currentLanguage) {
                AppLanguage.ARABIC -> "أتحمل المسؤولية القانونية الكاملة عن صحة أوصافي والامتثال للقوانين التونسية لحماية المعطيات والثلب."
                AppLanguage.FRENCH -> "J'assume l'entière responsabilité légale de mes propos et le respect des lois tunisiennes."
                AppLanguage.ENGLISH -> "I assume full legal responsibility for my descriptions and compliance with Tunisian defamation and data laws."
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Checkbox 3: Safety first
        AckCheckboxRow(
            checked = isAckSafety,
            onToggle = onToggleSafety,
            text = when (currentLanguage) {
                AppLanguage.ARABIC -> "أقر بأن سلامتي الشخصية تأتي أولاً، وأن التطبيق لا يحل محل المحاكم أو أجهزة القضاء."
                AppLanguage.FRENCH -> "Je reconnais que ma sécurité personnelle prime et que l'application ne remplace pas les tribunaux."
                AppLanguage.ENGLISH -> "I affirm that personal safety comes first and that this platform does not replace courts or law enforcement."
            }
        )
    }
}

@Composable
private fun AckCheckboxRow(
    checked: Boolean,
    onToggle: () -> Unit,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.onSurface,
                uncheckedColor = Neutral500,
                checkmarkColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
