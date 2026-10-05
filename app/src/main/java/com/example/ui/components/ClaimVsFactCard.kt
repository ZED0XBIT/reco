package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecordingEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.Neutral200
import com.example.ui.theme.Neutral300
import com.example.ui.theme.Neutral400
import com.example.ui.theme.Neutral500
import com.example.ui.theme.Neutral600
import com.example.ui.theme.Neutral800
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClaimVsFactCard(
    recording: RecordingEntity,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val uploadDateStr = dateFormat.format(Date(recording.publicationTimestamp))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Text(
            text = when (lang) {
                AppLanguage.ARABIC -> "التصنيف التوثيقي: الوقائع مقابل الادعاءات"
                AppLanguage.FRENCH -> "Classification Méthodologique : Faits & Allégations"
                AppLanguage.ENGLISH -> "Methodology: Facts & Claims"
            },
            style = MaterialTheme.typography.labelMedium,
            color = Neutral500,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 1. APPLICATION FACT (Monochrome Editorial Badge)
        MonochromeClassificationRow(
            badgeLabel = AppStrings.labelApplicationFact(lang),
            icon = Icons.Outlined.CheckCircle,
            content = when (lang) {
                AppLanguage.ARABIC -> "تم رفع هذا التسجيل وتوليد البصمة الرقمية المشفرة بتاريخ $uploadDateStr من قِبل الحساب (@${recording.ownerUsername}). المعرف المرجعي: ${recording.recordingId}."
                AppLanguage.FRENCH -> "Enregistrement stocké et empreinte certifiée le $uploadDateStr par le compte public @${recording.ownerUsername}. Référence : ${recording.recordingId}."
                AppLanguage.ENGLISH -> "Audio stored and cryptographic footprint generated on $uploadDateStr by public account @${recording.ownerUsername}. Reference: ${recording.recordingId}."
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. USER CLAIM
        MonochromeClassificationRow(
            badgeLabel = AppStrings.labelUserClaim(lang),
            icon = Icons.Outlined.RecordVoiceOver,
            content = when (lang) {
                AppLanguage.ARABIC -> "يصرح الناشر بأن التسجيل يوثق: \"${recording.title}\"${if (recording.locationDescription != null) " بالموقع: \"${recording.locationDescription}\"" else ""}. هذا الوصف صادر حصراً عن المستخدم."
                AppLanguage.FRENCH -> "L'utilisateur déclare que l'audio concerne : \"${recording.title}\"${if (recording.locationDescription != null) " au lieu : \"${recording.locationDescription}\"" else ""}. Ce narratif relève de la seule responsabilité de son auteur."
                AppLanguage.ENGLISH -> "Uploader asserts this recording documents: \"${recording.title}\"${if (recording.locationDescription != null) " at: \"${recording.locationDescription}\"" else ""}. This context is supplied exclusively by the uploader."
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. LEGALLY ESTABLISHED FACT
        MonochromeClassificationRow(
            badgeLabel = AppStrings.labelLegallyEstablishedFact(lang),
            icon = Icons.Outlined.Gavel,
            content = when (lang) {
                AppLanguage.ARABIC -> "لا يوجد حكم قضائي بات منشور هنا. المنصة لا تصدر أحكاماً مسبقة؛ تقدير الحجج والمسؤوليات اختصاص قضائي حصري."
                AppLanguage.FRENCH -> "Aucun jugement judiciaire définitif consigné. La plateforme ne qualifie pas pénalement les faits ; l'administration de la preuve relève de la compétence des tribunaux."
                AppLanguage.ENGLISH -> "No judicial finding recorded. The platform does not adjudicate legal violations; evidence evaluation is the sole jurisdiction of competent judicial courts."
            }
        )
    }
}

@Composable
private fun MonochromeClassificationRow(
    badgeLabel: String,
    icon: ImageVector,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = badgeLabel.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
