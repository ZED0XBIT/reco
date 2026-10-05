package com.example.ui.screens.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.model.RecordingEntity
import com.example.i18n.AppLanguage
import com.example.ui.components.AudioPlayerWidget
import com.example.ui.components.ClaimVsFactCard
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingDetailScreen(
    recording: RecordingEntity,
    viewModel: MainViewModel,
    lang: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.authRepository.currentUser.collectAsState()
    val isOwner = currentUser?.userId == recording.ownerId

    val savedList by viewModel.savedRecordings.collectAsState()
    val isSaved = savedList.any { it.recordingId == recording.recordingId }

    val integrityResult by viewModel.integrityVerificationState.collectAsState()
    val isVerifyingIntegrity by viewModel.isVerifyingIntegrity.collectAsState()

    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsState()
    val currentPositionMs by viewModel.audioPlayerManager.currentPositionMs.collectAsState()
    val durationMs by viewModel.audioPlayerManager.durationMs.collectAsState()
    val playbackSpeed by viewModel.audioPlayerManager.playbackSpeed.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var hashCopied by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    val scrollState = rememberScrollState()
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(recording.publicationTimestamp))

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
            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Save to Private Evidence
                    IconButton(
                        onClick = { viewModel.toggleSaveRecording(recording.recordingId) },
                        modifier = Modifier.testTag("detail_save_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save Evidence",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // QR Evidence Card
                    IconButton(
                        onClick = { viewModel.openQrEvidenceCard(recording) },
                        modifier = Modifier.testTag("detail_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCode2,
                            contentDescription = "QR Evidence Card",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Export Evidence Package
                    IconButton(
                        onClick = { viewModel.exportEvidencePackage(recording, context) },
                        modifier = Modifier.testTag("detail_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export Evidence Package",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Evidence: ${recording.recordingId}")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Protect Yourself — إحمي روحك\nEvidence ID: ${recording.recordingId}\nTitle: ${recording.title}\nCategory: ${recording.category}\nUploader: @${recording.ownerUsername}\nSHA-256: ${recording.sha256Hash}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Public Evidence"))
                        },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Report
                    IconButton(
                        onClick = { viewModel.startReporting(recording.recordingId) },
                        modifier = Modifier.testTag("detail_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Report",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isOwner) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("detail_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete",
                                tint = RecordRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category & Evidence ID Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = recording.category.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral500,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = recording.recordingId,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large Title (Section 15)
            Text(
                text = recording.title,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Uploader, Date, Duration Line
            Text(
                text = "@${recording.ownerUsername} · $dateStr · ${AudioIntegrityUtils.formatDuration(recording.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Audio Player
            AudioPlayerWidget(
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = if (durationMs > 0) durationMs else recording.durationMs,
                playbackSpeed = playbackSpeed,
                onPlayPauseToggle = {
                    viewModel.audioPlayerManager.loadAndPlay(recording.recordingId, recording.filePath)
                },
                onSeek = { viewModel.audioPlayerManager.seekTo(it) },
                onSpeedChange = { viewModel.audioPlayerManager.setPlaybackSpeed(it) }
            )

            if (recording.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(24.dp))

                // Description Box (Uploader's Narrative)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "سياق الواقعة (تصريح صاحب التسجيل)"
                            AppLanguage.FRENCH -> "Contexte & Description (Déclaration de l'auteur)"
                            AppLanguage.ENGLISH -> "Context & Description (Uploader's Narrative)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Neutral500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recording.description,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mandatory Claim vs Fact Card (Section 11)
            ClaimVsFactCard(recording = recording, lang = lang)

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive Cryptographic Integrity Card (Section 18 & 31)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CRYPTOGRAPHIC INTEGRITY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Evidence Hash", recording.sha256Hash)
                            clipboard.setPrimaryClip(clip)
                            hashCopied = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Hash",
                            tint = if (hashCopied) MaterialTheme.colorScheme.onSurface else Neutral500,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "SHA-256 STORED HASH",
                            style = MaterialTheme.typography.labelSmall,
                            color = Neutral500,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = recording.sha256Hash,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Size: ${AudioIntegrityUtils.formatFileSize(recording.fileSizeBytes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500
                    )
                    Text(
                        text = "Status: ${recording.moderationStatus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INTERACTIVE INTEGRITY CHECK BUTTON
                OutlinedButton(
                    onClick = { viewModel.verifyIntegrity(recording.recordingId) },
                    enabled = !isVerifyingIntegrity,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isVerifyingIntegrity) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying local bitstream...")
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (lang) {
                                AppLanguage.ARABIC -> "التحقق من سلامة الملف محلياً"
                                AppLanguage.FRENCH -> "Vérifier l'intégrité du fichier"
                                AppLanguage.ENGLISH -> "Verify File Integrity (SHA-256)"
                            }
                        )
                    }
                }

                // INTEGRITY RESULT BANNER
                integrityResult?.let { res ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (res.isIntact) MaterialTheme.colorScheme.surfaceVariant
                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                            .border(
                                1.dp,
                                if (res.isIntact) MaterialTheme.colorScheme.outline
                                else MaterialTheme.colorScheme.error,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (res.isIntact) Icons.Outlined.Check else Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = if (res.isIntact) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (res.isIntact) "Integrity check passed" else "Integrity check warning",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = res.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The cryptographic hash detects whether the stored file corresponds to the recorded hash. It does NOT prove speaker identity or verify truth of allegations.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                    color = Neutral500
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "حذف التسجيل نهائياً"
                            AppLanguage.FRENCH -> "Supprimer l'enregistrement"
                            AppLanguage.ENGLISH -> "Delete Recording"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "سيتم حذف الملف الصوتي وسجل الأدلة نهائياً من النظام."
                            AppLanguage.FRENCH -> "Le fichier audio et son empreinte seront définitivement supprimés."
                            AppLanguage.ENGLISH -> "The audio file and evidence record will be permanently deleted."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            viewModel.deleteRecording(recording.recordingId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.SemiBold, color = MonoWhite)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = false },
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
