package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioIntegrityUtils
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.ui.util.PrivacyCheckUtils
import com.example.viewmodel.PublishDraftState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicUploadWarningSheet(
    lang: AppLanguage,
    uploaderUsername: String,
    draft: PublishDraftState,
    isPublishing: Boolean,
    onConfirmPublish: () -> Unit,
    onCancel: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scrollState = rememberScrollState()

    // 6 Mandatory Checkpoints from Specification (Section 24)
    var ackPublic by remember { mutableStateOf(false) }
    var ackReviewedRecording by remember { mutableStateOf(false) }
    var ackReviewedDescription by remember { mutableStateOf(false) }
    var ackLegalConsequences by remember { mutableStateOf(false) }
    var ackAvoidedPrivateInfo by remember { mutableStateOf(false) }
    var ackUnverifiedStatements by remember { mutableStateOf(false) }

    val allChecked = ackPublic &&
            ackReviewedRecording &&
            ackReviewedDescription &&
            ackLegalConsequences &&
            ackAvoidedPrivateInfo &&
            ackUnverifiedStatements

    // Sensitive Data Detection
    val combinedText = "${draft.title} ${draft.description} ${draft.locationDescription}"
    val privacyCheck = PrivacyCheckUtils.inspectText(combinedText)

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = AppStrings.publicUploadTitle(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "المعاينة العلنية وإقرار المسؤولية"
                            AppLanguage.FRENCH -> "Aperçu public & Engagement de responsabilité"
                            AppLanguage.ENGLISH -> "Public Preview & Responsibility Checklist"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: PUBLIC PREVIEW CARD (Section 23)
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "المعاينة: هكذا سيظهر التسجيل للعموم"
                    AppLanguage.FRENCH -> "APERÇU : Ce qui sera visible publiquement"
                    AppLanguage.ENGLISH -> "PREVIEW: What will become public"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Neutral500
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "@$uploaderUsername",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "PS-TN-2026-PENDING",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = Neutral500
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = draft.title.ifBlank { "Untitled Evidence" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${draft.category} · ${AudioIntegrityUtils.formatDuration(draft.pendingDurationMs)} · ${AudioIntegrityUtils.formatFileSize(draft.pendingFileSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500
                )

                if (draft.locationDescription.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Location: ${draft.locationDescription}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (draft.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = draft.description,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "SHA-256: ${draft.pendingSha256.take(24)}...",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    color = Neutral500
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: CONTEXT COMPLETENESS INDICATOR (Section 21)
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "اكتمال السياق التوثيقي"
                    AppLanguage.FRENCH -> "Complétude du contexte"
                    AppLanguage.ENGLISH -> "Context Completeness"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Neutral500
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CompletenessChip(label = "Title", isComplete = draft.title.isNotBlank())
                CompletenessChip(label = "Category", isComplete = draft.category.isNotBlank())
                CompletenessChip(label = "Description", isComplete = draft.description.isNotBlank())
                CompletenessChip(label = "Location", isComplete = draft.locationDescription.isNotBlank())
            }

            // SECTION 3: SENSITIVE DATA WARNING (Section 38)
            if (privacyCheck.hasPotentialSensitiveData) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> privacyCheck.warningMessageAr
                            AppLanguage.FRENCH -> privacyCheck.warningMessageFr
                            AppLanguage.ENGLISH -> privacyCheck.warningMessageEn
                        },
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: 6-POINT MANDATORY PUBLICATION CHECKLIST (Section 24)
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "إقرار النشر الإلزامي (يرجى التأكيد على جميع البنود):"
                    AppLanguage.FRENCH -> "Engagements obligatoires avant publication :"
                    AppLanguage.ENGLISH -> "Mandatory Publication Checklist:"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            ChecklistRow(
                checked = ackPublic,
                onCheckedChange = { ackPublic = it },
                text = when (lang) {
                    AppLanguage.ARABIC -> "أفهم أن هذا التسجيل سيصبح علنياً ومتاحاً للجميع."
                    AppLanguage.FRENCH -> "Je comprends que cet enregistrement sera public."
                    AppLanguage.ENGLISH -> "I understand this recording will be public."
                }
            )

            ChecklistRow(
                checked = ackReviewedRecording,
                onCheckedChange = { ackReviewedRecording = it },
                text = when (lang) {
                    AppLanguage.ARABIC -> "راجعت التسجيل الصوتي وتأكدت من محتواه."
                    AppLanguage.FRENCH -> "J'ai écouté et vérifié l'enregistrement audio."
                    AppLanguage.ENGLISH -> "I reviewed the audio recording."
                }
            )

            ChecklistRow(
                checked = ackReviewedDescription,
                onCheckedChange = { ackReviewedDescription = it },
                text = when (lang) {
                    AppLanguage.ARABIC -> "راجعت الوصف وأتحمل صحة ما ذكرته فيه."
                    AppLanguage.FRENCH -> "J'ai relu la description et assume mes propos."
                    AppLanguage.ENGLISH -> "I reviewed the narrative description."
                }
            )

            ChecklistRow(
                checked = ackLegalConsequences,
                onCheckedChange = { ackLegalConsequences = it },
                text = when (lang) {
                    AppLanguage.ARABIC -> "أفهم أن النشر قد تترتب عليه مسؤوليات وتبعات قانونية."
                    AppLanguage.FRENCH -> "Je comprends que la publication peut avoir des conséquences légales."
                    AppLanguage.ENGLISH -> "I understand publication may have legal consequences."
                }
            )

            ChecklistRow(
                checked = ackAvoidedPrivateInfo,
                onCheckedChange = { ackAvoidedPrivateInfo = it },
                text = when (lang) {
                    AppLanguage.ARABIC -> "تجنبت نشر معطيات شخصية لا ضرورة لها احتراما للقانون."
                    AppLanguage.FRENCH -> "J'ai évité toute divulgation superflue de données privées."
                    AppLanguage.ENGLISH -> "I avoided unnecessary private personal information."
                }
            )

            ChecklistRow(
                checked = ackUnverifiedStatements,
                onCheckedChange = { ackUnverifiedStatements = it },
                text = when (lang) {
                    AppLanguage.ARABIC -> "أعلم أن المنصة لا تصادق آلياً على صحة الادعاءات أو الإدانات."
                    AppLanguage.FRENCH -> "Je reconnais que la plateforme ne certifie pas la culpabilité ou la véracité des faits."
                    AppLanguage.ENGLISH -> "I understand that uploader statements are not verified by the platform."
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("cancel_publish_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = AppStrings.cancelBtn(lang),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onConfirmPublish,
                    enabled = allChecked && !isPublishing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(50.dp)
                        .testTag("confirm_publish_publicly_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (isPublishing) "..." else AppStrings.publishPubliclyBtn(lang),
                        fontWeight = FontWeight.SemiBold,
                        color = if (allChecked) MaterialTheme.colorScheme.surface else Neutral500
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletenessChip(label: String, isComplete: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isComplete) "✓" else "○",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isComplete) MaterialTheme.colorScheme.onSurface else Neutral500
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChecklistRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.onSurface,
                uncheckedColor = Neutral500,
                checkmarkColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
