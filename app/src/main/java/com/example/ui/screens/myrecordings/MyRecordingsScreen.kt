package com.example.ui.screens.myrecordings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioIntegrityUtils
import com.example.data.model.IncidentEntity
import com.example.data.model.RecordingEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyRecordingsScreen(
    viewModel: MainViewModel,
    lang: AppLanguage,
    onOpenRecordingDetail: (RecordingEntity) -> Unit
) {
    val currentUser by viewModel.authRepository.currentUser.collectAsState()
    val recordings by viewModel.myRecordings.collectAsState()
    val myIncidents by viewModel.myIncidents.collectAsState()
    val savedRecordings by viewModel.savedRecordings.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = My Incidents, 1 = My Audios, 2 = Saved Evidence

    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsState()
    val currentPlayingId by viewModel.audioPlayerManager.currentRecordingId.collectAsState()

    var recordingToDelete by remember { mutableStateOf<RecordingEntity?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Large Heading (Section 16)
            Text(
                text = AppStrings.navMyRecordings(lang),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "أرشيفك الشخصي للوقائع الموثقة والتسجيلات المحفوظة"
                    AppLanguage.FRENCH -> "Votre bibliothèque de rapports d'incidents et preuves"
                    AppLanguage.ENGLISH -> "Your personal incident archive and preserved evidence"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Sub-tab switcher: [My Incidents (N)] [My Audios (M)] [Saved Evidence (K)]
            if (currentUser != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val incidentsTitle = when (lang) {
                        AppLanguage.ARABIC -> "الوقائع (${myIncidents.size})"
                        AppLanguage.FRENCH -> "Incidents (${myIncidents.size})"
                        AppLanguage.ENGLISH -> "Incidents (${myIncidents.size})"
                    }
                    val myUploadsTitle = when (lang) {
                        AppLanguage.ARABIC -> "التسجيلات (${recordings.size})"
                        AppLanguage.FRENCH -> "Audios (${recordings.size})"
                        AppLanguage.ENGLISH -> "Audios (${recordings.size})"
                    }
                    val savedTitle = when (lang) {
                        AppLanguage.ARABIC -> "المحفوظة (${savedRecordings.size})"
                        AppLanguage.FRENCH -> "Sauvegardés (${savedRecordings.size})"
                        AppLanguage.ENGLISH -> "Saved (${savedRecordings.size})"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedSubTab == 0) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedSubTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = incidentsTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedSubTab == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedSubTab == 1) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedSubTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = myUploadsTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedSubTab == 1) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedSubTab == 2) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedSubTab = 2 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = savedTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedSubTab == 2) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            if (currentUser == null) {
                // Not Logged In State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "تسجيل الدخول مطلوب"
                                AppLanguage.FRENCH -> "Connexion requise"
                                AppLanguage.ENGLISH -> "Sign In Required"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "سجل الدخول بحسابك لاستعراض وإدارة تسجيلاتك."
                                AppLanguage.FRENCH -> "Connectez-vous pour gérer votre bibliothèque."
                                AppLanguage.ENGLISH -> "Sign in to access and manage your evidence library."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral500,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.navigateToTab(NavTab.ACCOUNT) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "تسجيل الدخول / إنشاء حساب"
                                    AppLanguage.FRENCH -> "Se connecter"
                                    AppLanguage.ENGLISH -> "Sign In"
                                },
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            } else {
                if (selectedSubTab == 0) {
                    // 1. MY INCIDENTS SUBTAB
                    if (myIncidents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = Neutral500,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "لا توجد وقائع موثقة بعد"
                                        AppLanguage.FRENCH -> "Aucun incident documenté pour le moment"
                                        AppLanguage.ENGLISH -> "No incidents documented yet"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "الوقائع التي تقوم بتوثيقها وإيداع أدلتها ستظهر هنا مع كامل البيانات والبصمات الرقمية."
                                        AppLanguage.FRENCH -> "Les incidents que vous documentez apparaîtront ici avec leurs preuves et empreintes."
                                        AppLanguage.ENGLISH -> "Incidents you document and submit will appear here with complete evidence and cryptographic hashes."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        viewModel.startNewIncident()
                                        viewModel.navigateToTab(NavTab.RECORD)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(46.dp)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "توثيق واقعة جديدة"
                                            AppLanguage.FRENCH -> "Créer un incident"
                                            AppLanguage.ENGLISH -> "Create Incident Report"
                                        },
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        ) {
                            items(myIncidents) { inc ->
                                val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                                val dateStr = dateFormat.format(Date(inc.submissionTimestamp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openIncidentDetail(inc) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.FolderOpen,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = inc.incidentId,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = inc.governorate,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = inc.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$dateStr · ${inc.category} · ${inc.evidenceCount} ${when(lang) {
                                                    AppLanguage.ARABIC -> "ملفات أدلة"
                                                    AppLanguage.FRENCH -> "pièces"
                                                    AppLanguage.ENGLISH -> "files"
                                                }}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Neutral500
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = "Open",
                                        tint = Neutral500,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    // 2. MY AUDIOS & SAVED EVIDENCE SUBTABS
                    val currentDisplayList = if (selectedSubTab == 1) recordings else savedRecordings

                    if (currentDisplayList.isEmpty()) {
                        // Empty State
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = Neutral500,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (selectedSubTab == 1) {
                                        when (lang) {
                                            AppLanguage.ARABIC -> "لا توجد تسجيلات بعد"
                                            AppLanguage.FRENCH -> "Aucun enregistrement pour le moment"
                                            AppLanguage.ENGLISH -> "No recordings yet"
                                        }
                                    } else {
                                        when (lang) {
                                            AppLanguage.ARABIC -> "لا توجد أدلة محفوظة بعد"
                                            AppLanguage.FRENCH -> "Aucune preuve sauvegardée"
                                            AppLanguage.ENGLISH -> "No saved evidence yet"
                                        }
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (selectedSubTab == 1) {
                                        when (lang) {
                                            AppLanguage.ARABIC -> "ستظهر هنا تسجيلاتك بعد حفظها وتأكيد نشرها."
                                            AppLanguage.FRENCH -> "Vos enregistrements publiés apparaîtront ici."
                                            AppLanguage.ENGLISH -> "Your published recordings will appear here."
                                        }
                                    } else {
                                        when (lang) {
                                            AppLanguage.ARABIC -> "الأدلة التي تضغط على حفظها أثناء التصفح ستظهر هنا في سجلك الخاص."
                                            AppLanguage.FRENCH -> "Les preuves que vous enregistrez dans le flux apparaîtront ici."
                                            AppLanguage.ENGLISH -> "Recordings you bookmark while browsing public evidence will appear here."
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                if (selectedSubTab == 1) {
                                    Button(
                                        onClick = { viewModel.navigateToTab(NavTab.RECORD) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        Text("RECORD AUDIO", fontWeight = FontWeight.SemiBold, color = MonoWhite)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { viewModel.navigateToTab(NavTab.FEED) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(46.dp)
                                    ) {
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.ARABIC -> "تصفح الأرشيف العام"
                                                AppLanguage.FRENCH -> "Explorer les preuves publiques"
                                                AppLanguage.ENGLISH -> "Browse Public Evidence"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Elegant List Rows
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        ) {
                            items(currentDisplayList) { rec ->
                                val isCurrentlyPlaying = (currentPlayingId == rec.recordingId && isPlaying)
                                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                val dateStr = dateFormat.format(Date(rec.publicationTimestamp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenRecordingDetail(rec) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Mini Play/Pause button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            viewModel.audioPlayerManager.loadAndPlay(rec.recordingId, rec.filePath)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = rec.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$dateStr · ${AudioIntegrityUtils.formatDuration(rec.durationMs)} · ${rec.category}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral500
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { recordingToDelete = rec },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Neutral500,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = "Detail",
                                    tint = Neutral500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Modal
    recordingToDelete?.let { rec ->
        AlertDialog(
            onDismissRequest = { recordingToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "تأكيد حذف التسجيل"
                        AppLanguage.FRENCH -> "Confirmation de suppression"
                        AppLanguage.ENGLISH -> "Confirm Deletion"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "هل أنت متأكد من حذف ${rec.recordingId} نهائياً؟"
                        AppLanguage.FRENCH -> "Supprimer définitivement l'enregistrement ${rec.recordingId} ?"
                        AppLanguage.ENGLISH -> "Permanently delete ${rec.recordingId} from system?"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecording(rec.recordingId)
                        recordingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold, color = MonoWhite)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { recordingToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
}
}
