package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500

data class ReportReasonItem(val id: String, val labelAr: String, val labelFr: String, val labelEn: String)

val reportReasonsList = listOf(
    ReportReasonItem("harassment", "مضايقة أو سب شخصي", "Harcèlement ou injure", "Harassment"),
    ReportReasonItem("threats", "تهديدات أو تحريض على العنف", "Menaces ou incitation à la violence", "Threats or violence"),
    ReportReasonItem("private_info", "إفشاء معطيات شخصية حساسة", "Divulgation de données privées sensibles", "Private sensitive data"),
    ReportReasonItem("doxxing", "نشر معطيات تعريفية (Doxxing)", "Divulgation d'identité malveillante", "Malicious identification (Doxxing)"),
    ReportReasonItem("fabricated", "محتوى مفبرك أو مقتطع", "Contenu manipulé ou tronqué", "Fabricated or manipulated content"),
    ReportReasonItem("illegal", "محتوى مخالف للقانون بشكل صريح", "Contenu illicite flagrant", "Illegal content"),
    ReportReasonItem("impersonation", "انتحال صفة أو هوية", "Usurpation d'identité", "Impersonation"),
    ReportReasonItem("other", "سبب آخر", "Autre motif", "Other")
)

@Composable
fun ReportDialog(
    recordingId: String,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String, comment: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(reportReasonsList.first().id) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = when (lang) {
                    AppLanguage.ARABIC -> "إبلاغ عن تسجيل"
                    AppLanguage.FRENCH -> "Signaler un enregistrement"
                    AppLanguage.ENGLISH -> "Report Recording"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "حدد السبب بدقة. المحتوى لا يُحذف لمجرد انتقاد الموظف؛ نلتزم بالقانون وبالمعايير التوثيقية."
                        AppLanguage.FRENCH -> "Indiquez le motif. Un contenu n'est pas retiré du seul fait qu'il critique un fonctionnaire."
                        AppLanguage.ENGLISH -> "Select a reason. Content is not removed solely for criticizing officials."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                    color = Neutral500
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.height(170.dp)) {
                    items(reportReasonsList) { item ->
                        val isSelected = (selectedReason == item.id)
                        val label = when (lang) {
                            AppLanguage.ARABIC -> item.labelAr
                            AppLanguage.FRENCH -> item.labelFr
                            AppLanguage.ENGLISH -> item.labelEn
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .clickable { selectedReason = item.id }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedReason = item.id },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedColor = Neutral500
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = {
                        Text(
                            when (lang) {
                                AppLanguage.ARABIC -> "ملاحظات إضافية (اختياري)..."
                                AppLanguage.FRENCH -> "Détails supplémentaires (facultatif)..."
                                AppLanguage.ENGLISH -> "Additional context (optional)..."
                            },
                            color = Neutral500,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_comment_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitReport(selectedReason, comment) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "إرسال"
                        AppLanguage.FRENCH -> "Envoyer"
                        AppLanguage.ENGLISH -> "Submit"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "إلغاء"
                        AppLanguage.FRENCH -> "Annuler"
                        AppLanguage.ENGLISH -> "Cancel"
                    },
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
