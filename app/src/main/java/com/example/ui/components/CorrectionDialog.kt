package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.Neutral500

@Composable
fun CorrectionDialog(
    recordingId: String,
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSubmitCorrection: (field: String, suggestion: String) -> Unit
) {
    val correctionTypes = listOf(
        "category" to when (lang) {
            AppLanguage.ARABIC -> "تصنيف غير دقيق"
            AppLanguage.FRENCH -> "Catégorie inexacte"
            AppLanguage.ENGLISH -> "Inaccurate category"
        },
        "metadata" to when (lang) {
            AppLanguage.ARABIC -> "خطأ في البيانات الوصفية (الموقع/التاريخ)"
            AppLanguage.FRENCH -> "Erreur de métadonnées (lieu/date)"
            AppLanguage.ENGLISH -> "Metadata error (location/date)"
        },
        "typo" to when (lang) {
            AppLanguage.ARABIC -> "خطأ لغوي أو مطبعي في العنوان"
            AppLanguage.FRENCH -> "Erreur typographique dans le titre"
            AppLanguage.ENGLISH -> "Typo in title"
        },
        "other" to when (lang) {
            AppLanguage.ARABIC -> "تصحيح توثيقي آخر"
            AppLanguage.FRENCH -> "Autre correction documentaire"
            AppLanguage.ENGLISH -> "Other documentation correction"
        }
    )

    var selectedType by remember { mutableStateOf(correctionTypes.first().first) }
    var suggestionText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "اقتراح تصحيح توثيقي"
                        AppLanguage.FRENCH -> "Suggérer une correction"
                        AppLanguage.ENGLISH -> "Suggest a Correction"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "تُرسل التصحيحات إلى مراجعة التدقيق الإداري دون فتح نقاشات علنية أو مساس بالتسجيل الأصلي."
                        AppLanguage.FRENCH -> "Les suggestions sont transmises à la modération sans altérer l'enregistrement d'origine."
                        AppLanguage.ENGLISH -> "Suggestions are sent to moderation review without opening public arguments or altering the original recording."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp),
                    color = Neutral500
                )

                Spacer(modifier = Modifier.height(12.dp))

                correctionTypes.forEach { (key, label) ->
                    val isSelected = selectedType == key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else androidx.compose.ui.graphics.Color.Transparent
                            )
                            .clickable { selectedType = key }
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedType = key },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = suggestionText,
                    onValueChange = { suggestionText = it },
                    placeholder = {
                        Text(
                            when (lang) {
                                AppLanguage.ARABIC -> "اكتب التعديل المقترح مع السبب..."
                                AppLanguage.FRENCH -> "Détaillez la correction suggérée..."
                                AppLanguage.ENGLISH -> "Describe the suggested correction..."
                            },
                            color = Neutral500,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("correction_suggestion_input"),
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
                onClick = {
                    if (suggestionText.isNotBlank()) {
                        onSubmitCorrection(selectedType, suggestionText)
                    }
                },
                enabled = suggestionText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("submit_correction_button")
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.ARABIC -> "إرسال الاقتراح"
                        AppLanguage.FRENCH -> "Envoyer"
                        AppLanguage.ENGLISH -> "Submit"
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = if (suggestionText.isNotBlank()) MaterialTheme.colorScheme.surface else Neutral500
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
