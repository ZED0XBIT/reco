package com.example.ui.screens.detail

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioIntegrityUtils
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentEvidenceEntity
import com.example.i18n.AppLanguage
import com.example.ui.components.AudioPlayerWidget
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreen(
    incident: IncidentEntity,
    viewModel: MainViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val evidenceList by viewModel.selectedIncidentEvidence.collectAsState()
    val scrollState = rememberScrollState()

    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsState()
    val playPositionMs by viewModel.audioPlayerManager.currentPositionMs.collectAsState()
    val playDurationMs by viewModel.audioPlayerManager.durationMs.collectAsState()
    val playbackSpeed by viewModel.audioPlayerManager.playbackSpeed.collectAsState()

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
    val incidentDateStr = sdf.format(Date(incident.incidentDateTime))
    val submissionDateStr = sdf.format(Date(incident.submissionTimestamp))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = incident.incidentId,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = incident.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral500
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("incident_detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.exportIncidentEvidencePackage(incident, context) },
                        modifier = Modifier.testTag("incident_export_action")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export Evidence Package",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
            ) {
                // Status Header Badge Card (Monochrome)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = incident.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${incident.governorate} • ${evidenceList.size} ${when(lang) {
                                    AppLanguage.ARABIC -> "ملفات أدلة"
                                    AppLanguage.FRENCH -> "fichiers de preuve"
                                    AppLanguage.ENGLISH -> "evidence file(s)"
                                }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = incident.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Time Separation Card (Section 5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "تاريخ وتوقيت الواقعة والإيداع"
                            AppLanguage.FRENCH -> "Horodatage de l'incident et dépôt"
                            AppLanguage.ENGLISH -> "Incident Timing & Submission"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Neutral500
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "وقت حدوث الواقعة"
                                    AppLanguage.FRENCH -> "Survenu le"
                                    AppLanguage.ENGLISH -> "Incident Occurred"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )
                            Text(
                                text = incidentDateStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "توقيت الإيداع بالمنصة"
                                    AppLanguage.FRENCH -> "Déposé le"
                                    AppLanguage.ENGLISH -> "Submitted to Platform"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )
                            Text(
                                text = submissionDateStr,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Details Card (Section 3 & 4)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "الموقع الجغرافي"
                                AppLanguage.FRENCH -> "Localisation géographique"
                                AppLanguage.ENGLISH -> "Geographic Location"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${when (lang) {
                            AppLanguage.ARABIC -> "الولاية (إجباري)"
                            AppLanguage.FRENCH -> "Gouvernorat (requis)"
                            AppLanguage.ENGLISH -> "Governorate (required)"
                        }}: ${incident.governorate}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (incident.latitude != null && incident.longitude != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${when (lang) {
                                AppLanguage.ARABIC -> "الإحداثيات الدقيقة (مثبتة)"
                                AppLanguage.FRENCH -> "Coordonnées GPS exactes"
                                AppLanguage.ENGLISH -> "Exact GPS Coordinates"
                            }}: ${String.format(Locale.US, "%.5f° N, %.5f° E", incident.latitude, incident.longitude)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!incident.locationNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = incident.locationNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral500
                        )
                    }
                }

                // Officer / Person Involved Card (Optional - Section 8)
                val hasOfficerInfo = !incident.officerName.isNullOrBlank() ||
                        !incident.officerBadge.isNullOrBlank() ||
                        !incident.officerDepartment.isNullOrBlank() ||
                        !incident.vehicleRegistration.isNullOrBlank() ||
                        !incident.otherIdentifyingInfo.isNullOrBlank()

                if (hasOfficerInfo) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "بيانات الشخص أو العون المعني (تصريح المبلغ)"
                                    AppLanguage.FRENCH -> "Personne ou agent impliqué (déclaratif)"
                                    AppLanguage.ENGLISH -> "Person or Officer Involved (User Statement)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        if (!incident.officerName.isNullOrBlank()) {
                            Text(text = "Nom/الاسم: ${incident.officerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (!incident.officerBadge.isNullOrBlank()) {
                            Text(text = "Matricule/الرقم المهني: ${incident.officerBadge}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (!incident.officerDepartment.isNullOrBlank()) {
                            Text(text = "Service/المصلحة: ${incident.officerDepartment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (!incident.vehicleRegistration.isNullOrBlank()) {
                            Text(text = "Véhicule/رقم العربة: ${incident.vehicleRegistration}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        if (!incident.otherIdentifyingInfo.isNullOrBlank()) {
                            Text(text = "Autres/معلومات أخرى: ${incident.otherIdentifyingInfo}", style = MaterialTheme.typography.bodySmall, color = Neutral500)
                        }
                    }
                }

                // Narrative Description (Section 7)
                if (incident.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "رواية الوقائع (تصريح المبلغ)"
                                AppLanguage.FRENCH -> "Récit des faits (déclaratif)"
                                AppLanguage.ENGLISH -> "Incident Narrative (Uploader Statement)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Neutral500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = incident.description,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Attached Evidence Section (Section 9 & 10)
                Text(
                    text = "${when (lang) {
                        AppLanguage.ARABIC -> "الأدلة والملفات المرفقة"
                        AppLanguage.FRENCH -> "Pièces et preuves attachées"
                        AppLanguage.ENGLISH -> "Attached Evidence & Files"
                    }} (${evidenceList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                evidenceList.forEachIndexed { index, ev ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (ev.fileType) {
                                    "AUDIO" -> Icons.Outlined.Mic
                                    "PHOTO" -> Icons.Outlined.Image
                                    "VIDEO" -> Icons.Outlined.Videocam
                                    else -> Icons.Outlined.InsertDriveFile
                                }
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.originalFilename,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${ev.fileType} • ${AudioIntegrityUtils.formatFileSize(ev.fileSizeBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500
                                )
                            }
                        }

                        // Audio Player for audio attachment
                        if (ev.fileType == "AUDIO") {
                            Spacer(modifier = Modifier.height(12.dp))
                            AudioPlayerWidget(
                                isPlaying = isPlaying,
                                currentPositionMs = playPositionMs,
                                durationMs = if (playDurationMs > 0) playDurationMs else ev.durationMs,
                                playbackSpeed = playbackSpeed,
                                onPlayPauseToggle = {
                                    viewModel.audioPlayerManager.loadAndPlay(ev.evidenceId, ev.filePath)
                                },
                                onSeek = { viewModel.audioPlayerManager.seekTo(it) },
                                onSpeedChange = { viewModel.audioPlayerManager.setPlaybackSpeed(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Cryptographic Hash Display (Section 10)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHA-256 INTEGRITY HASH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ev.sha256Hash,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Incident Timeline (Section 11)
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "الخط الزمني الموثق للواقعة"
                                AppLanguage.FRENCH -> "Chronologie de l'incident"
                                AppLanguage.ENGLISH -> "Documented Incident Timeline"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = incident.timelineJson.ifBlank { "$incidentDateStr — Incident occurred\n$submissionDateStr — Submitted" },
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Export Evidence Package Button (Section 14)
                Button(
                    onClick = { viewModel.exportIncidentEvidencePackage(incident, context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("export_evidence_package_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "تصدير حزمة الأدلة والتقرير الشامل"
                            AppLanguage.FRENCH -> "Exporter le dossier de preuves"
                            AppLanguage.ENGLISH -> "Export Evidence Package"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legal Disclaimer (Section 17)
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "إشعار قانوني: تقوم هذه المنصة بتوثيق الأدلة وحفظ البصمات الرقمية بدقة وشفافية. المنصة لا تؤكد أو تنفي الصلاحية القضائية لأي تسجيل، ويترك تقدير القيمة الثبوتية حصريا للسلطات والهيئات القضائية المختصة."
                        AppLanguage.FRENCH -> "Notice : Cette plateforme préserve les éléments de preuve et leurs empreintes avec exactitude. Elle ne certifie pas la validité judiciaire automatique des fichiers, qui relève de la seule compétence des autorités judiciaires."
                        AppLanguage.ENGLISH -> "Legal Notice: This platform preserves information and cryptographic integrity transparently. It does NOT claim that a recording is automatically 'legally valid evidence'. Admissibility is evaluated exclusively by competent judicial authorities."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = Neutral500,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
