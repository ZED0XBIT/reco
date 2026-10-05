package com.example.ui.screens.record

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.audio.AudioIntegrityUtils
import com.example.audio.RecorderState
import com.example.data.model.TunisianGovernorates
import com.example.i18n.AppLanguage
import com.example.ui.components.AudioPlayerWidget
import com.example.ui.components.MapPinPickerSheet
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedPressed
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: MainViewModel,
    lang: AppLanguage
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val draft by viewModel.incidentDraft.collectAsState()

    // Audio Recorder Manager State
    val recorderState by viewModel.audioRecorderManager.recorderState.collectAsState()
    val elapsedTimeMs by viewModel.audioRecorderManager.elapsedTimeMs.collectAsState()
    val currentAmp by viewModel.audioRecorderManager.currentAmplitude.collectAsState()

    // Audio Player for review
    val isPlaying by viewModel.audioPlayerManager.isPlaying.collectAsState()
    val playPositionMs by viewModel.audioPlayerManager.currentPositionMs.collectAsState()
    val playDurationMs by viewModel.audioPlayerManager.durationMs.collectAsState()
    val playbackSpeed by viewModel.audioPlayerManager.playbackSpeed.collectAsState()

    var showMapPicker by remember { mutableStateOf(false) }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startRecording()
        }
    }

    // Attachment Launchers
    fun getFilenameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            result = cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (e: Exception) { }
        }
        return result ?: uri.lastPathSegment ?: "file_${System.currentTimeMillis()}"
    }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.attachFileFromUri(it, "AUDIO", getFilenameFromUri(it))
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.attachFileFromUri(it, "PHOTO", getFilenameFromUri(it))
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.attachFileFromUri(it, "VIDEO", getFilenameFromUri(it))
        }
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.attachFileFromUri(it, "DOCUMENT", getFilenameFromUri(it))
        }
    }

    // Categories list (Section 6)
    val categories = listOf(
        "Police / security incident" to when (lang) {
            AppLanguage.ARABIC -> "واقعة أمنية / شرطة"
            AppLanguage.FRENCH -> "Incident policier / sécurité"
            AppLanguage.ENGLISH -> "Police / security incident"
        },
        "Road / traffic incident" to when (lang) {
            AppLanguage.ARABIC -> "حادث مروري / طريق"
            AppLanguage.FRENCH -> "Incident circulation / route"
            AppLanguage.ENGLISH -> "Road / traffic incident"
        },
        "Harassment" to when (lang) {
            AppLanguage.ARABIC -> "مضايقة / تحرش"
            AppLanguage.FRENCH -> "Harcèlement"
            AppLanguage.ENGLISH -> "Harassment"
        },
        "Threat" to when (lang) {
            AppLanguage.ARABIC -> "تهديد"
            AppLanguage.FRENCH -> "Menace"
            AppLanguage.ENGLISH -> "Threat"
        },
        "Assault" to when (lang) {
            AppLanguage.ARABIC -> "اعتداء بالعنف"
            AppLanguage.FRENCH -> "Agression physique"
            AppLanguage.ENGLISH -> "Assault"
        },
        "Property damage" to when (lang) {
            AppLanguage.ARABIC -> "إضرار بالممتلكات"
            AppLanguage.FRENCH -> "Dégradation de biens"
            AppLanguage.ENGLISH -> "Property damage"
        },
        "Workplace incident" to when (lang) {
            AppLanguage.ARABIC -> "حادثة في مكان العمل"
            AppLanguage.FRENCH -> "Incident au travail"
            AppLanguage.ENGLISH -> "Workplace incident"
        },
        "Public service issue" to when (lang) {
            AppLanguage.ARABIC -> "إشكال مع مرفق عام"
            AppLanguage.FRENCH -> "Problème service public"
            AppLanguage.ENGLISH -> "Public service issue"
        },
        "Other" to when (lang) {
            AppLanguage.ARABIC -> "أخرى"
            AppLanguage.FRENCH -> "Autre"
            AppLanguage.ENGLISH -> "Other"
        }
    )

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Incident Header & Pre-assigned Incident ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "توثيق واقعة جديدة"
                            AppLanguage.FRENCH -> "Créer un rapport d'incident"
                            AppLanguage.ENGLISH -> "Create Incident Report"
                        },
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "حفظ الأدلة والبيانات بشفافية ودقة تشفيرية"
                            AppLanguage.FRENCH -> "Préservation transparente des preuves et informations"
                            AppLanguage.ENGLISH -> "Transparent evidence preservation & cryptographic hashing"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = draft.incidentId,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step Progress Indicator (Steps 0 to 6)
            if (draft.currentStep < 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepLabels = listOf(
                        "1. الشروط",
                        "2. التوقيت",
                        "3. الموقع",
                        "4. الواقعة",
                        "5. الأطراف",
                        "6. الأدلة",
                        "7. المراجعة"
                    )
                    repeat(7) { index ->
                        val isDone = draft.currentStep > index
                        val isCurrent = draft.currentStep == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isCurrent -> MaterialTheme.colorScheme.onSurface
                                        isDone -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            AnimatedContent(
                targetState = draft.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "incident_wizard_step"
            ) { step ->
                when (step) {
                    // ==========================================
                    // STEP 0: REQUIRED TERMS (Section 2)
                    // ==========================================
                    0 -> {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "الموافقة على الشروط والمسؤوليات (إجباري)"
                                    AppLanguage.FRENCH -> "Conditions requises avant soumission"
                                    AppLanguage.ENGLISH -> "Required Terms Before Submission"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "المنصة لا تصرح بأن التسجيل دليل قضائي جاهز تلقائياً، بل تحفظ الملفات بدقة وأمانة تمهيداً لتقديمها للهيئات والجهات المعنية."
                                    AppLanguage.FRENCH -> "L'application ne prétend pas qu'un enregistrement constitue automatiquement une 'preuve légalement valable'. Elle préserve les fichiers avec intégrité pour les autorités compétentes."
                                    AppLanguage.ENGLISH -> "The app does NOT claim a recording is automatically 'legally valid evidence'. It accurately and transparently preserves information and files for competent authorities."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Checkbox 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setIncidentTerms(
                                            !draft.termsRightToSubmitConfirmed,
                                            draft.termsResponsibleConfirmed,
                                            draft.termsStorageUnderstoodConfirmed
                                        )
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = draft.termsRightToSubmitConfirmed,
                                    onCheckedChange = {
                                        viewModel.setIncidentTerms(
                                            it,
                                            draft.termsResponsibleConfirmed,
                                            draft.termsStorageUnderstoodConfirmed
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "أؤكد أن لدي الحق القانوني في إيداع هذا التسجيل أو الأدلة المرفقة."
                                        AppLanguage.FRENCH -> "Je confirme avoir le droit de soumettre cet enregistrement ou cette preuve."
                                        AppLanguage.ENGLISH -> "I confirm that I have the right to submit this recording or evidence."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Checkbox 2
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setIncidentTerms(
                                            draft.termsRightToSubmitConfirmed,
                                            !draft.termsResponsibleConfirmed,
                                            draft.termsStorageUnderstoodConfirmed
                                        )
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = draft.termsResponsibleConfirmed,
                                    onCheckedChange = {
                                        viewModel.setIncidentTerms(
                                            draft.termsRightToSubmitConfirmed,
                                            it,
                                            draft.termsStorageUnderstoodConfirmed
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "أفهم وأقر بأنني المسؤول قانونياً عن صحة ودقة البيانات والمعلومات التي أقدمها."
                                        AppLanguage.FRENCH -> "Je comprends que je suis responsable des informations que je soumets."
                                        AppLanguage.ENGLISH -> "I understand that I am responsible for the information I submit."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Checkbox 3
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setIncidentTerms(
                                            draft.termsRightToSubmitConfirmed,
                                            draft.termsResponsibleConfirmed,
                                            !draft.termsStorageUnderstoodConfirmed
                                        )
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = draft.termsStorageUnderstoodConfirmed,
                                    onCheckedChange = {
                                        viewModel.setIncidentTerms(
                                            draft.termsRightToSubmitConfirmed,
                                            draft.termsResponsibleConfirmed,
                                            it
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.onSurface,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "أفهم كيفية حفظ معلوماتي وأدلتي واستخدامها وفق سياسة الخصوصية وحماية المعطيات الشخصية."
                                        AppLanguage.FRENCH -> "Je comprends comment mes informations et preuves soumises seront stockées et utilisées."
                                        AppLanguage.ENGLISH -> "I understand how my submitted information and evidence will be stored and used."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = { viewModel.setIncidentStep(1) },
                                enabled = draft.areTermsAccepted,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("terms_continue_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    disabledContentColor = Neutral500
                                )
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "متابعة إلى توقيت الواقعة"
                                        AppLanguage.FRENCH -> "Continuer"
                                        AppLanguage.ENGLISH -> "Continue to Timing"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // ==========================================
                    // STEP 1: WHEN DID IT HAPPEN? (Section 5)
                    // ==========================================
                    1 -> {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "متى حدثت هذه الواقعة؟"
                                    AppLanguage.FRENCH -> "Quand cet incident s'est-il produit ?"
                                    AppLanguage.ENGLISH -> "When did this incident happen?"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "يتم تسجيل وقت حدوث الواقعة بشكل منفصل تماماً عن توقيت تقديم التقرير في المنصة."
                                    AppLanguage.FRENCH -> "La date de l'incident est enregistrée séparément de l'heure de soumission."
                                    AppLanguage.ENGLISH -> "Incident date/time is recorded separately from the submission timestamp."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Quick Presets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentDateTime(System.currentTimeMillis()) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "توّاً"
                                            AppLanguage.FRENCH -> "À l'instant"
                                            AppLanguage.ENGLISH -> "Just now"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentDateTime(System.currentTimeMillis() - 3600000L) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "منذ ساعة"
                                            AppLanguage.FRENCH -> "Il y a 1h"
                                            AppLanguage.ENGLISH -> "1h ago"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentDateTime(System.currentTimeMillis() - 86400000L) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "البارحة"
                                            AppLanguage.FRENCH -> "Hier"
                                            AppLanguage.ENGLISH -> "Yesterday"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Date & Time Card
                            val cal = Calendar.getInstance().apply { timeInMillis = draft.incidentDateTime }
                            val sdfDisplay = SimpleDateFormat("EEEE, d MMMM yyyy — HH:mm", Locale.getDefault())

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                                    .padding(18.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "توقيت الواقعة المحدد:"
                                            AppLanguage.FRENCH -> "Date et heure de l'incident :"
                                            AppLanguage.ENGLISH -> "Selected Incident Time:"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = sdfDisplay.format(Date(draft.incidentDateTime)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                                Spacer(modifier = Modifier.height(14.dp))

                                // Adjust hours & days controls
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "تعديل التاريخ / الساعات"
                                            AppLanguage.FRENCH -> "Ajuster la date"
                                            AppLanguage.ENGLISH -> "Adjust date/hour"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral500
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { viewModel.setIncidentDateTime(draft.incidentDateTime - 86400000L) },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("-1 J", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.setIncidentDateTime(draft.incidentDateTime - 3600000L) },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("-1 h", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.setIncidentDateTime((draft.incidentDateTime + 3600000L).coerceAtMost(System.currentTimeMillis())) },
                                            modifier = Modifier.height(34.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("+1 h", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submission timestamp notice (Section 5)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "ملاحظة: سيتم تسجيل توقيت الإيداع رسمياً في المنصة بشكل آلي عند تأكيد الإرسال."
                                        AppLanguage.FRENCH -> "L'heure de soumission sera enregistrée automatiquement lors de la validation."
                                        AppLanguage.ENGLISH -> "Submission date & time will be automatically recorded upon final confirmation."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentStep(0) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "رجوع")
                                }

                                Button(
                                    onClick = { viewModel.setIncidentStep(2) },
                                    modifier = Modifier.weight(1.5f).height(50.dp).testTag("time_continue_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(text = "متابعة إلى الموقع", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 2: WHERE DID IT HAPPEN? (Section 3 & 4)
                    // ==========================================
                    2 -> {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "أين حدثت الواقعة؟"
                                    AppLanguage.FRENCH -> "Où l'incident s'est-il produit ?"
                                    AppLanguage.ENGLISH -> "Where did it happen?"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "اختيار الولاية إجباري (من بين 24 ولاية تونسية). تحديد النقطة على الخريطة اختياري."
                                    AppLanguage.FRENCH -> "Le gouvernorat est obligatoire (24 gouvernorats). Le point sur la carte est optionnel."
                                    AppLanguage.ENGLISH -> "Governorate is REQUIRED (24 Tunisian governorates). Map pin is OPTIONAL."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 1. Required Governorate Dropdown / Selector (Section 3)
                            Text(
                                text = "${when (lang) {
                                    AppLanguage.ARABIC -> "الولاية التونسية"
                                    AppLanguage.FRENCH -> "Gouvernorat"
                                    AppLanguage.ENGLISH -> "Governorate"
                                }} * (${when (lang) {
                                    AppLanguage.ARABIC -> "إجباري"
                                    AppLanguage.FRENCH -> "Requis"
                                    AppLanguage.ENGLISH -> "Required"
                                }})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            var expandedGov by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expandedGov,
                                onExpandedChange = { expandedGov = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = TunisianGovernorates.findByName(draft.governorate)?.localizedName(lang)
                                        ?: draft.governorate.ifBlank { "اختر الولاية (إجباري)..." },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGov) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("governorate_selector"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                        unfocusedBorderColor = if (draft.governorate.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = expandedGov,
                                    onDismissRequest = { expandedGov = false },
                                    modifier = Modifier.height(300.dp)
                                ) {
                                    TunisianGovernorates.ALL.forEach { gov ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${gov.localizedName(lang)} (${gov.nameEn})",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                viewModel.setIncidentGovernorate(gov.nameEn)
                                                expandedGov = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 2. Optional Exact Map Pin Card (Section 4)
                            Text(
                                text = "${when (lang) {
                                    AppLanguage.ARABIC -> "تثبيت الموقع الدقيق على الخريطة"
                                    AppLanguage.FRENCH -> "Pointer l'emplacement exact"
                                    AppLanguage.ENGLISH -> "Pin exact location on map"
                                }} (${when (lang) {
                                    AppLanguage.ARABIC -> "اختياري"
                                    AppLanguage.FRENCH -> "Optionnel"
                                    AppLanguage.ENGLISH -> "Optional"
                                }})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                if (draft.latitude != null && draft.longitude != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "تم تثبيت الإحداثيات بنجاح",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = String.format(Locale.US, "%.5f° N, %.5f° E", draft.latitude, draft.longitude),
                                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                                    color = Neutral500
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(onClick = { showMapPicker = true }) {
                                                Icon(Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(onClick = { viewModel.setIncidentCoordinates(null, null) }) {
                                                Icon(Icons.Outlined.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Neutral500)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.ARABIC -> "مفيد للطرقات، الشوارع العامة، والأماكن الخالية من عناوين بريدية."
                                                AppLanguage.FRENCH -> "Utile pour les routes, autoroutes et zones sans adresse précise."
                                                AppLanguage.ENGLISH -> "Useful for highways, roads, public places, or remote areas."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Neutral500
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = { showMapPicker = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .testTag("open_map_pin_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = when (lang) {
                                                AppLanguage.ARABIC -> "فتح الخريطة وتثبيت النقطة"
                                                AppLanguage.FRENCH -> "Ouvrir la carte et pointer"
                                                AppLanguage.ENGLISH -> "Open map and drop pin"
                                            },
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Optional Location description text
                            OutlinedTextField(
                                value = draft.locationNotes,
                                onValueChange = { viewModel.setIncidentLocationNotes(it) },
                                label = {
                                    Text(
                                        when (lang) {
                                            AppLanguage.ARABIC -> "تفاصيل الموقع أو المعالم القريبة (اختياري)"
                                            AppLanguage.FRENCH -> "Détails ou repères (optionnel)"
                                            AppLanguage.ENGLISH -> "Location details or landmarks (optional)"
                                        }
                                    )
                                },
                                placeholder = {
                                    Text("مثال: مفترق شارع الحبيب بورقيبة، محطة النقل، الكيلومتر 42...")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentStep(1) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "رجوع")
                                }

                                Button(
                                    onClick = { viewModel.setIncidentStep(3) },
                                    enabled = draft.isLocationValid,
                                    modifier = Modifier.weight(1.5f).height(50.dp).testTag("location_continue_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(text = "متابعة إلى الواقعة", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 3: WHAT HAPPENED? (Section 6 & 7)
                    // ==========================================
                    3 -> {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "ماذا حدث؟ (صنف الواقعة والوصف)"
                                    AppLanguage.FRENCH -> "Que s'est-il passé ?"
                                    AppLanguage.ENGLISH -> "What happened?"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "تحديد الصنف إجباري. كتابة الوصف السردي اختياري بحسب رغبتك."
                                    AppLanguage.FRENCH -> "La catégorie est requise. La description est optionnelle."
                                    AppLanguage.ENGLISH -> "Category is required. Narrative description is optional."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Required Category Chips (Section 6)
                            Text(
                                text = "${when (lang) {
                                    AppLanguage.ARABIC -> "صنف الواقعة"
                                    AppLanguage.FRENCH -> "Catégorie de l'incident"
                                    AppLanguage.ENGLISH -> "Incident Category"
                                }} * (${when(lang) {
                                    AppLanguage.ARABIC -> "إجباري"
                                    AppLanguage.FRENCH -> "Requis"
                                    AppLanguage.ENGLISH -> "Required"
                                }})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Category selector chips in a wrapped/grid layout
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categories.chunked(2).forEach { rowPair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowPair.forEach { (catId, catLabel) ->
                                            val isSelected = draft.category == catId
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onSurface
                                                        else MaterialTheme.colorScheme.surface
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) MaterialTheme.colorScheme.onSurface
                                                        else MaterialTheme.colorScheme.outline,
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { viewModel.setIncidentCategory(catId) }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                                    .testTag("cat_chip_$catId"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = catLabel,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                        if (rowPair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Optional Title
                            OutlinedTextField(
                                value = draft.title,
                                onValueChange = { viewModel.setIncidentTitle(it) },
                                label = { Text("عنوان التقرير (اختياري)") },
                                placeholder = { Text("مثال: توثيق تفتيش في محطة المترو") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Optional Large Description (Section 7)
                            Text(
                                text = "${when (lang) {
                                    AppLanguage.ARABIC -> "وصف ما حدث"
                                    AppLanguage.FRENCH -> "Décrivez ce qui s'est passé"
                                    AppLanguage.ENGLISH -> "Describe what happened"
                                }} (${when (lang) {
                                    AppLanguage.ARABIC -> "اختياري"
                                    AppLanguage.FRENCH -> "Optionnel"
                                    AppLanguage.ENGLISH -> "Optional"
                                }})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = draft.description,
                                onValueChange = { viewModel.setIncidentDescription(it) },
                                placeholder = {
                                    Text(
                                        when (lang) {
                                            AppLanguage.ARABIC -> "يمكنك هنا وصف ما حدث، أين حدث بالضبط، من كان متواجداً، وما جرى قبل الواقعة وأثناءها وبعدها، وأي تفاصيل أخرى تراها مفيدة..."
                                            AppLanguage.FRENCH -> "Décrivez ce qui s'est passé, les personnes impliquées, ce qui s'est produit avant/pendant/après, et tout détail pertinent..."
                                            AppLanguage.ENGLISH -> "Describe what happened, who was involved, what happened before/during/after the incident, and any other relevant details..."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral500
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("incident_description_input"),
                                shape = RoundedCornerShape(14.dp),
                                maxLines = 6
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentStep(2) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "رجوع")
                                }

                                Button(
                                    onClick = { viewModel.setIncidentStep(4) },
                                    enabled = draft.isCategoryValid,
                                    modifier = Modifier.weight(1.5f).height(50.dp).testTag("narrative_continue_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(text = "متابعة إلى الأطراف", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 4: WHO WAS INVOLVED? (OPTIONAL - Section 8)
                    // ==========================================
                    4 -> {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (lang) {
                                        AppLanguage.ARABIC -> "بيانات الشخص أو العون المعني"
                                        AppLanguage.FRENCH -> "Personne ou agent impliqué"
                                        AppLanguage.ENGLISH -> "Officer / Person Information"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
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
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "اختياري بالكامل"
                                            AppLanguage.FRENCH -> "Optionnel"
                                            AppLanguage.ENGLISH -> "Optional"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Neutral500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "هذه الحقول اختيارية تماماً. لا تدخل أي معلومة لست متأكداً منها. يمكنك تخطي هذه الخطوة مباشرة."
                                    AppLanguage.FRENCH -> "Ces champs sont strictement optionnels. Vous pouvez passer cette étape."
                                    AppLanguage.ENGLISH -> "None of these fields are mandatory. You can skip this step if unknown."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = draft.officerName,
                                onValueChange = { viewModel.setIncidentOfficerInfo(it, draft.officerBadge, draft.officerDepartment, draft.vehicleRegistration, draft.otherIdentifyingInfo) },
                                label = { Text("الاسم / الصفة (اختياري)") },
                                placeholder = { Text("الاسم إن كان معلوماً") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = draft.officerBadge,
                                onValueChange = { viewModel.setIncidentOfficerInfo(draft.officerName, it, draft.officerDepartment, draft.vehicleRegistration, draft.otherIdentifyingInfo) },
                                label = { Text("الرقم المهني أو الشارة (اختياري)") },
                                placeholder = { Text("Badge / ID number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = draft.officerDepartment,
                                onValueChange = { viewModel.setIncidentOfficerInfo(draft.officerName, draft.officerBadge, it, draft.vehicleRegistration, draft.otherIdentifyingInfo) },
                                label = { Text("المصلحة أو الإدارة أو السلك (اختياري)") },
                                placeholder = { Text("شرطة، حرس وطني، بلدية، ديوانة...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = draft.vehicleRegistration,
                                onValueChange = { viewModel.setIncidentOfficerInfo(draft.officerName, draft.officerBadge, draft.officerDepartment, it, draft.otherIdentifyingInfo) },
                                label = { Text("رقم تسجيل العربة أو السيارة (اختياري)") },
                                placeholder = { Text("Matricule / رقم اللوحة المنجمية") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = draft.otherIdentifyingInfo,
                                onValueChange = { viewModel.setIncidentOfficerInfo(draft.officerName, draft.officerBadge, draft.officerDepartment, draft.vehicleRegistration, it) },
                                label = { Text("أي معلومات أو أوصاف تمييزية أخرى (اختياري)") },
                                placeholder = { Text("أوصاف، ألوان، زي مميز...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentStep(3) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "رجوع")
                                }

                                Button(
                                    onClick = { viewModel.setIncidentStep(5) },
                                    modifier = Modifier.weight(1.5f).height(50.dp).testTag("officer_continue_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(text = "متابعة إلى إرفاق الأدلة", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 5: ADD EVIDENCE (Section 9 & 10)
                    // ==========================================
                    5 -> {
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "إرفاق الأدلة والملفات الأصلية"
                                    AppLanguage.FRENCH -> "Attacher les preuves (Audio, Photo, Vidéo, Doc)"
                                    AppLanguage.ENGLISH -> "Attach Evidence (Audio, Photo, Video, Doc)"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "تسجيل مباشر أو استيراد ملفات. يتم حفظ الملف الأصلي بدون تعديل وحساب بصمته الرقمية SHA-256 تلقائياً."
                                    AppLanguage.FRENCH -> "Enregistrement en direct ou import. Les fichiers originaux sont préservés intacts avec calcul SHA-256."
                                    AppLanguage.ENGLISH -> "Live audio or import. Original files are preserved untouched and cryptographic SHA-256 hashes computed."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // 1. IN-APP AUDIO RECORDER (Original Core Feature preserved!)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "التسجيل الصوتي المباشر (الملف الأصلي)"
                                            AppLanguage.FRENCH -> "Enregistrement audio direct"
                                            AppLanguage.ENGLISH -> "Live Audio Recording (Original File)"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                when (recorderState) {
                                    RecorderState.RECORDING, RecorderState.PAUSED -> {
                                        // Active Recording State
                                        val infiniteTransition = rememberInfiniteTransition(label = "recording_pulse")
                                        val pulseScale by infiniteTransition.animateFloat(
                                            initialValue = 1f,
                                            targetValue = 1.25f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(700),
                                                repeatMode = RepeatMode.Reverse
                                            ),
                                            label = "pulse"
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .scale(if (recorderState == RecorderState.RECORDING) pulseScale else 1f)
                                                    .clip(CircleShape)
                                                    .background(RecordRed)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (recorderState == RecorderState.RECORDING) "RECORDING" else "PAUSED",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp,
                                                color = if (recorderState == RecorderState.RECORDING) RecordRed else Neutral500
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = AudioIntegrityUtils.formatDuration(elapsedTimeMs),
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 36.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Stop, Pause, Cancel buttons
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Cancel
                                            OutlinedButton(
                                                onClick = { viewModel.cancelRecording() },
                                                shape = CircleShape,
                                                modifier = Modifier.size(46.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                            ) {
                                                Icon(Icons.Outlined.Close, contentDescription = "Cancel", modifier = Modifier.size(20.dp))
                                            }

                                            // Stop & Save to Incident
                                            Box(
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                                    .clickable {
                                                        val result = viewModel.audioRecorderManager.stopRecording()
                                                        if (result != null) {
                                                            viewModel.attachRecordedAudioToIncident(result)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(28.dp))
                                            }

                                            // Pause / Resume
                                            OutlinedButton(
                                                onClick = {
                                                    if (recorderState == RecorderState.RECORDING) viewModel.pauseRecording()
                                                    else viewModel.resumeRecording()
                                                },
                                                shape = CircleShape,
                                                modifier = Modifier.size(46.dp),
                                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                            ) {
                                                Icon(
                                                    if (recorderState == RecorderState.RECORDING) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Toggle",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        // Idle State: Start Record Button (The ONLY Red Element)
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(RecordRed)
                                                .clickable {
                                                    if (hasMicPermission) {
                                                        viewModel.startRecording()
                                                    } else {
                                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                    }
                                                }
                                                .testTag("incident_record_audio_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Mic,
                                                contentDescription = "Start Audio Recording",
                                                tint = MonoWhite,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = when (lang) {
                                                AppLanguage.ARABIC -> "اضغط للتسجيل الصوتي الفوري"
                                                AppLanguage.FRENCH -> "Appuyez pour enregistrer l'audio"
                                                AppLanguage.ENGLISH -> "Tap to record audio"
                                            },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 2. ATTACH OTHER MEDIA TYPES (Photos, Videos, Documents, Audio files)
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "أو استيراد ملفات من جهازك:"
                                    AppLanguage.FRENCH -> "Ou importer des fichiers :"
                                    AppLanguage.ENGLISH -> "Or attach existing files:"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Audio File
                                OutlinedButton(
                                    onClick = { audioFilePicker.launch("audio/*") },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Outlined.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "صوت", fontSize = 11.sp)
                                }

                                // Photo
                                OutlinedButton(
                                    onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "صور", fontSize = 11.sp)
                                }

                                // Video
                                OutlinedButton(
                                    onClick = { videoPicker.launch("video/*") },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "فيديو", fontSize = 11.sp)
                                }

                                // Document
                                OutlinedButton(
                                    onClick = { documentPicker.launch("*/*") },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "وثيقة", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 3. ATTACHED EVIDENCE LIST
                            Text(
                                text = "${when (lang) {
                                    AppLanguage.ARABIC -> "الأدلة المرفقة حالياً"
                                    AppLanguage.FRENCH -> "Preuves attachées"
                                    AppLanguage.ENGLISH -> "Attached Evidence"
                                }} (${draft.attachments.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (draft.attachments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (lang) {
                                            AppLanguage.ARABIC -> "لم يتم إرفاق أي دليل بعد. قم بالتسجيل الصوتي أعلاه أو اختر ملفات."
                                            AppLanguage.FRENCH -> "Aucune preuve attachée. Enregistrez ou sélectionnez des fichiers."
                                            AppLanguage.ENGLISH -> "No evidence attached yet. Record audio above or pick files."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral500,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            } else {
                                draft.attachments.forEach { att ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val icon = when (att.fileType) {
                                            "AUDIO" -> Icons.Outlined.Mic
                                            "PHOTO" -> Icons.Outlined.Image
                                            "VIDEO" -> Icons.Outlined.Videocam
                                            else -> Icons.Outlined.InsertDriveFile
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = att.originalFilename,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${att.fileType} • ${AudioIntegrityUtils.formatFileSize(att.fileSizeBytes)} • SHA-256: ${att.sha256Hash.take(10)}...",
                                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                                color = Neutral500
                                            )
                                        }

                                        IconButton(onClick = { viewModel.removeIncidentAttachment(att.evidenceId) }) {
                                            Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = Neutral500, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setIncidentStep(4) },
                                    modifier = Modifier.weight(1f).height(50.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "رجوع")
                                }

                                Button(
                                    onClick = { viewModel.setIncidentStep(6) },
                                    enabled = draft.hasEvidenceAttached,
                                    modifier = Modifier.weight(1.5f).height(50.dp).testTag("evidence_continue_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(text = "مراجعة كامل التقرير", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ==========================================
                    // STEP 6: COMPLETE REVIEW SCREEN (Section 12)
                    // ==========================================
                    6 -> {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                        Column {
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "مراجعة شاملة قبل الإرسال النهائي"
                                    AppLanguage.FRENCH -> "Vérification complète avant soumission"
                                    AppLanguage.ENGLISH -> "Complete Review Before Submission"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "تأكد من دقة المعلومات ومرفقات الأدلة. يمكنك العودة لتعديل أي بند قبل الإرسال."
                                    AppLanguage.FRENCH -> "Vérifiez vos données et pièces jointes. Vous pouvez modifier chaque section."
                                    AppLanguage.ENGLISH -> "Review your incident report and attached evidence. You can edit any section."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // 1. Identity & Category Review
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المعرف والصنف",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Neutral500
                                    )
                                    Text(
                                        text = "تعديل",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { viewModel.setIncidentStep(3) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ID: ${draft.incidentId}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الصنف: ${draft.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (draft.title.isNotBlank()) {
                                    Text(
                                        text = "العنوان: ${draft.title}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 2. Timing Review
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "التوقيت",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Neutral500
                                    )
                                    Text(
                                        text = "تعديل",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { viewModel.setIncidentStep(1) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "تاريخ حدوث الواقعة: ${sdf.format(Date(draft.incidentDateTime))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "توقيت الإيداع: الآن (آلي)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 3. Location Review
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الموقع",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Neutral500
                                    )
                                    Text(
                                        text = "تعديل",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { viewModel.setIncidentStep(2) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "الولاية: ${draft.governorate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (draft.latitude != null && draft.longitude != null) {
                                    Text(
                                        text = "الإحداثيات: ${String.format(Locale.US, "%.5f° N, %.5f° E", draft.latitude, draft.longitude)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (draft.locationNotes.isNotBlank()) {
                                    Text(
                                        text = "معالم: ${draft.locationNotes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Neutral500
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 4. Evidence Attachments & Integrity Table
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الأدلة والبصمات الرقمية (${draft.attachments.size})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Neutral500
                                    )
                                    Text(
                                        text = "تعديل",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { viewModel.setIncidentStep(5) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                draft.attachments.forEachIndexed { i, att ->
                                    Text(
                                        text = "${i + 1}. [${att.fileType}] ${att.originalFilename} (${AudioIntegrityUtils.formatFileSize(att.fileSizeBytes)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "   SHA-256: ${att.sha256Hash}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(28.dp))

                            Button(
                                onClick = { viewModel.submitIncident() },
                                enabled = !draft.isSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("final_submit_incident_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (draft.isSubmitting) "جاري إيداع التقرير والأدلة..."
                                    else when (lang) {
                                        AppLanguage.ARABIC -> "تأكيد إيداع التقرير والأدلة"
                                        AppLanguage.FRENCH -> "Confirmer et soumettre l'incident"
                                        AppLanguage.ENGLISH -> "Confirm & Submit Incident"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = { viewModel.setIncidentStep(5) },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(text = "العودة لتعديل الأدلة")
                            }
                        }
                    }

                    // ==========================================
                    // STEP 7: SUBMISSION CONFIRMATION (Section 13)
                    // ==========================================
                    7 -> {
                        val sub = draft.submittedIncident
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(22.dp))
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = when (lang) {
                                    AppLanguage.ARABIC -> "تم توثيق الواقعة وإيداع الأدلة بنجاح"
                                    AppLanguage.FRENCH -> "Incident soumis avec succès"
                                    AppLanguage.ENGLISH -> "Incident Submitted"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Incident ID Badge with copy
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        sub?.let {
                                            clipboardManager.setText(AnnotatedString(it.incidentId))
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sub?.incidentId ?: draft.incidentId,
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Outlined.Check, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "الولاية: ${sub?.governorate ?: draft.governorate} • الصنف: ${sub?.category ?: draft.category}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "عدد الملفات والأدلة المحفوظة: ${draft.attachments.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Actions
                            Button(
                                onClick = {
                                    sub?.let { viewModel.openIncidentDetail(it) }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("open_submitted_incident_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(text = "فتح ملف الواقعة الكامل والأدلة", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    sub?.let { viewModel.exportIncidentEvidencePackage(it, context) }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_submitted_incident_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "تصدير حزمة الأدلة والتقرير الشامل")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.startNewIncident()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(text = "توثيق واقعة أخرى")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Interactive Map Pin Sheet (Section 4)
    if (showMapPicker) {
        val govInfo = TunisianGovernorates.findByName(draft.governorate)
        MapPinPickerSheet(
            initialLat = draft.latitude,
            initialLng = draft.longitude,
            governorate = govInfo,
            lang = lang,
            onConfirmLocation = { lat, lng ->
                viewModel.setIncidentCoordinates(lat, lng)
            },
            onClearLocation = {
                viewModel.setIncidentCoordinates(null, null)
            },
            onDismiss = { showMapPicker = false }
        )
    }
}
