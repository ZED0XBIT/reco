package com.example.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.ui.text.font.FontFamily
import com.example.audio.AudioIntegrityUtils
import com.example.data.model.RecordingEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedPressed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    lang: AppLanguage,
    recentRecordings: List<RecordingEntity>,
    onNavigateToRecord: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToMyRecordings: () -> Unit,
    onOpenLegalReference: () -> Unit,
    onOpenRecordingDetail: (RecordingEntity) -> Unit,
    onStartIncident: () -> Unit = onNavigateToRecord
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(44.dp))

            // Minimalist Brand Emblem (Protection & Voice mark, pure monochrome)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Large Brand Identity (Section 11)
            Text(
                text = "إحمي روحك",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Protect Yourself",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Short Elegant Statement
            Text(
                text = AppStrings.appSubtitle(lang),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = Neutral500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(44.dp))

            // PRIMARY RECORD ACTION (The ONLY Red Element - Section 10)
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.94f else 1.0f,
                label = "record_btn_scale"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .scale(buttonScale)
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(if (isPressed) RecordRedPressed else RecordRed)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onNavigateToRecord() }
                        .testTag("home_primary_record_cta"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Record",
                        tint = MonoWhite,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "تسجيل وتوثيق واقعة"
                        AppLanguage.FRENCH -> "Enregistrer & Documenter"
                        AppLanguage.ENGLISH -> "Record & Document"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(52.dp))

            // Secondary Quiet Navigation (Section 11)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            ) {
                MinimalNavRow(
                    icon = Icons.Outlined.Shield,
                    title = when (lang) {
                        AppLanguage.ARABIC -> "توثيق واقعة جديدة وإيداع الأدلة"
                        AppLanguage.FRENCH -> "Créer un rapport d'incident"
                        AppLanguage.ENGLISH -> "Document Incident & Evidence"
                    },
                    subtitle = when (lang) {
                        AppLanguage.ARABIC -> "تقرير واقعة، تحديد الولاية، وربط الأدلة والبصمات"
                        AppLanguage.FRENCH -> "Rapport structuré, localisation et pièces à conviction"
                        AppLanguage.ENGLISH -> "Structured report, location, and preserved evidence"
                    },
                    onClick = onStartIncident
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                MinimalNavRow(
                    icon = Icons.Outlined.Public,
                    title = when (lang) {
                        AppLanguage.ARABIC -> "التسجيلات العامة"
                        AppLanguage.FRENCH -> "Enregistrements publics"
                        AppLanguage.ENGLISH -> "Public recordings"
                    },
                    subtitle = when (lang) {
                        AppLanguage.ARABIC -> "استكشاف الأدلة المحفوظة"
                        AppLanguage.FRENCH -> "Explorer les fichiers certifiés"
                        AppLanguage.ENGLISH -> "Discover preserved evidence"
                    },
                    onClick = onNavigateToFeed
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                MinimalNavRow(
                    icon = Icons.Outlined.MenuBook,
                    title = when (lang) {
                        AppLanguage.ARABIC -> "دليل حقوقك القانونية"
                        AppLanguage.FRENCH -> "Vos droits légaux"
                        AppLanguage.ENGLISH -> "Know your rights"
                    },
                    subtitle = when (lang) {
                        AppLanguage.ARABIC -> "التشريع التونسي لحماية الخصوصية"
                        AppLanguage.FRENCH -> "Cadre juridique tunisien"
                        AppLanguage.ENGLISH -> "Tunisian legal protections"
                    },
                    onClick = onOpenLegalReference
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                MinimalNavRow(
                    icon = Icons.Outlined.FolderOpen,
                    title = when (lang) {
                        AppLanguage.ARABIC -> "تسجيلاتي"
                        AppLanguage.FRENCH -> "Mes enregistrements"
                        AppLanguage.ENGLISH -> "My recordings"
                    },
                    subtitle = when (lang) {
                        AppLanguage.ARABIC -> "مكتبتك الخاصة"
                        AppLanguage.FRENCH -> "Votre bibliothèque personnelle"
                        AppLanguage.ENGLISH -> "Your personal library"
                    },
                    onClick = onNavigateToMyRecordings
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // RECENT PRESERVED CIVIC EVIDENCE SECTION
            if (recentRecordings.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "أحدث الأدلة الموثقة"
                            AppLanguage.FRENCH -> "Enregistrements récents"
                            AppLanguage.ENGLISH -> "Recent preserved evidence"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "عرض الكل"
                            AppLanguage.FRENCH -> "Voir tout"
                            AppLanguage.ENGLISH -> "View all"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500,
                        modifier = Modifier.clickable { onNavigateToFeed() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recentRecordings.take(3).forEach { rec ->
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        val dateStr = dateFormat.format(Date(rec.publicationTimestamp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .clickable { onOpenRecordingDetail(rec) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = rec.recordingId,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = Neutral500
                                    )
                                    Text(
                                        text = "·",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Neutral500
                                    )
                                    Text(
                                        text = rec.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = rec.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "@${rec.ownerUsername} · $dateStr · ${AudioIntegrityUtils.formatDuration(rec.durationMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500
                                )
                            }

                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = Neutral500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // TUNISIAN LEGAL HIGHLIGHT SPOTLIGHT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "الفصل 24 من الدستور التونسي"
                                AppLanguage.FRENCH -> "Article 24 - Constitution Tunisienne"
                                AppLanguage.ENGLISH -> "Article 24 - Tunisian Constitution"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "حماية الحياة الخاصة والمعطيات الشخصية"
                                AppLanguage.FRENCH -> "Protection de la vie privée et des données"
                                AppLanguage.ENGLISH -> "Protection of privacy and personal data"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "«تحمي الدولة الحياة الخاصة وحرمة المسكن وسرية المراسلات والاتصالات والمعطيات الشخصية.»"
                        AppLanguage.FRENCH -> "« L'État protège la vie privée, l'inviolabilité du domicile et le secret des correspondances, des communications et des données personnelles. »"
                        AppLanguage.ENGLISH -> "\"The state protects the right to privacy, the inviolability of the home, and the confidentiality of correspondence, communications, and personal data.\""
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .clickable { onOpenLegalReference() }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "الاطلاع على المرجعيات القانونية التونسية كاملة"
                            AppLanguage.FRENCH -> "Consulter le cadre juridique complet"
                            AppLanguage.ENGLISH -> "Explore complete legal framework"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun MinimalNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500
                )
            }
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Neutral500,
            modifier = Modifier.size(18.dp)
        )
    }
}
