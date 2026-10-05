package com.example.ui.screens.profile

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
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthResult
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.MonoWhite
import com.example.ui.theme.Neutral500
import com.example.ui.theme.RecordRed
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    lang: AppLanguage,
    onOpenLegalReference: () -> Unit
) {
    val currentUser by viewModel.authRepository.currentUser.collectAsState()
    val myRecordings by viewModel.myRecordings.collectAsState()
    val currentThemeMode by viewModel.themeMode.collectAsState()

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showModerationDialog by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = AppStrings.navAccount(lang),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (currentUser == null) {
                // AUTH VIEW: iOS minimal sign-in / registration
                MonochromeAuthSection(
                    viewModel = viewModel,
                    lang = lang
                )
            } else {
                // LOGGED-IN GROUPED SETTINGS (Section 17)
                val user = currentUser!!
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                val memberSince = dateFormat.format(Date(user.createdAt))

                // User Info Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Member since $memberSince · ${myRecordings.size} public recordings",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Group 1: Appearance (Light / Dark / System)
                SectionHeader("APPEARANCE")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            AppThemeMode.LIGHT to "Light",
                            AppThemeMode.DARK to "Dark",
                            AppThemeMode.SYSTEM to "System"
                        ).forEach { (mode, label) ->
                            val isSelected = (currentThemeMode == mode)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 2: Language
                SectionHeader("LANGUAGE")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { l ->
                            val isSelected = (lang == l)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.setLanguage(l) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = l.nativeName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 3: Account Actions
                SectionHeader("ACCOUNT & SECURITY")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    GroupedRow(
                        icon = Icons.Outlined.Key,
                        title = when (lang) {
                            AppLanguage.ARABIC -> "تغيير كلمة المرور"
                            AppLanguage.FRENCH -> "Changer le mot de passe"
                            AppLanguage.ENGLISH -> "Change Password"
                        },
                        onClick = { showChangePasswordDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    val pendingReports by viewModel.pendingReports.collectAsState()
                    GroupedRow(
                        icon = Icons.Outlined.Shield,
                        title = when (lang) {
                            AppLanguage.ARABIC -> "الرقابة وسجل التدقيق (${pendingReports.size})"
                            AppLanguage.FRENCH -> "Modération & Audit (${pendingReports.size})"
                            AppLanguage.ENGLISH -> "Moderation & Audit Logs (${pendingReports.size})"
                        },
                        onClick = { showModerationDialog = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    GroupedRow(
                        icon = Icons.Outlined.Logout,
                        title = when (lang) {
                            AppLanguage.ARABIC -> "تسجيل الخروج"
                            AppLanguage.FRENCH -> "Déconnexion"
                            AppLanguage.ENGLISH -> "Log Out"
                        },
                        onClick = { viewModel.authRepository.logout() }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

                    GroupedRow(
                        icon = Icons.Outlined.DeleteOutline,
                        title = when (lang) {
                            AppLanguage.ARABIC -> "حذف الحساب نهائياً"
                            AppLanguage.FRENCH -> "Supprimer le compte"
                            AppLanguage.ENGLISH -> "Delete Account"
                        },
                        textColor = RecordRed,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 4: Legal & Policy
                SectionHeader("LEGAL & PRIVACY")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                ) {
                    GroupedRow(
                        icon = Icons.Outlined.MenuBook,
                        title = AppStrings.readTunisianLawBtn(lang),
                        onClick = onOpenLegalReference
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 5: About
                SectionHeader("ABOUT")
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Protect Yourself — إحمي روحك",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Citizen evidence & public documentation platform. Version 1.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Change Password Modal
        if (showChangePasswordDialog) {
            ChangePasswordModal(
                lang = lang,
                onDismiss = { showChangePasswordDialog = false },
                onSubmit = { currentPass, newPass, onError, onSuccess ->
                    scope.launch {
                        val res = viewModel.authRepository.changePassword(currentPass, newPass)
                        res.onSuccess {
                            onSuccess()
                            showChangePasswordDialog = false
                        }.onFailure {
                            onError(it.localizedMessage ?: "Failed to update password")
                        }
                    }
                }
            )
        }

        // Delete Account Modal
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "حذف الحساب نهائياً"
                            AppLanguage.FRENCH -> "Supprimer le compte"
                            AppLanguage.ENGLISH -> "Delete Account"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "هل أنت متأكد من حذف الحساب؟ سيتم محو البيانات نهائياً."
                            AppLanguage.FRENCH -> "Voulez-vous supprimer définitivement votre compte ?"
                            AppLanguage.ENGLISH -> "Are you sure you want to permanently delete your account?"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.authRepository.deleteAccount()
                                showDeleteAccountDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.SemiBold, color = MonoWhite)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteAccountDialog = false },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }

        // Moderation Queue & Audit Log Modal (Section 36, 37, 83)
        if (showModerationDialog) {
            ModerationCenterModal(
                viewModel = viewModel,
                lang = lang,
                onDismiss = { showModerationDialog = false }
            )
        }
    }
}

@Composable
fun ModerationCenterModal(
    viewModel: MainViewModel,
    lang: AppLanguage,
    onDismiss: () -> Unit
) {
    var subTab by remember { mutableIntStateOf(0) } // 0 = Reports, 1 = Audit Log, 2 = Sessions
    val pendingReports by viewModel.pendingReports.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "الرقابة وسجل التدقيق"
                            AppLanguage.FRENCH -> "Modération & Audit"
                            AppLanguage.ENGLISH -> "Moderation & Audit"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                // Segmented Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val titles = listOf(
                        "Reports (${pendingReports.size})",
                        "Audit Log",
                        "Sessions"
                    )
                    titles.forEachIndexed { idx, t ->
                        val isSel = subTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { subTab = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (subTab) {
                    0 -> {
                        // Pending Reports Queue
                        if (pendingReports.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No pending reports in moderation queue.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(pendingReports.size) { i ->
                                    val rep = pendingReports[i]
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Evidence: ${rep.recordingId}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Reporter: @${rep.reporterUsername} · Reason: ${rep.reason}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (rep.comment.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "\"${rep.comment}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Neutral500
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.takeModerationAction(rep.reportId, rep.recordingId, "RESTRICT", "Policy compliance violation")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(36.dp)
                                            ) {
                                                Text("Restrict", fontSize = 12.sp, color = MonoWhite)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.takeModerationAction(rep.reportId, rep.recordingId, "DISMISS", "No violation found")
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(36.dp)
                                            ) {
                                                Text("Dismiss", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Audit Logs
                        if (auditLogs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No audit log entries recorded yet.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Neutral500
                                )
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(auditLogs.size) { i ->
                                    val log = auditLogs[i]
                                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                    val dateStr = sdf.format(Date(log.timestamp))

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = log.eventType,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = dateStr,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Neutral500
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Actor: @${log.actorUsername} · Target: ${log.targetId}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Neutral500
                                        )
                                        Text(
                                            text = log.details,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Sessions
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Current Device Session",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Android Local SQLite Session · Active Now",
                                style = MaterialTheme.typography.bodySmall,
                                color = Neutral500
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.authRepository.logout()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sign Out Other Sessions")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Done", color = MaterialTheme.colorScheme.surface)
            }
        }
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = Neutral500,
        modifier = Modifier.padding(start = 6.dp)
    )
}

@Composable
private fun GroupedRow(
    icon: ImageVector,
    title: String,
    textColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    val actualTextColor = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = actualTextColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = actualTextColor
            )
        }
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = Neutral500,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MonochromeAuthSection(
    viewModel: MainViewModel,
    lang: AppLanguage
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(1.dp))
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0; errorMessage = null },
                text = {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "دخول"
                            AppLanguage.FRENCH -> "Connexion"
                            AppLanguage.ENGLISH -> "Sign In"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1; errorMessage = null },
                text = {
                    Text(
                        text = when (lang) {
                            AppLanguage.ARABIC -> "تسجيل جديد"
                            AppLanguage.FRENCH -> "Créer un compte"
                            AppLanguage.ENGLISH -> "Register"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; errorMessage = null },
            placeholder = {
                Text(
                    when (lang) {
                        AppLanguage.ARABIC -> "اسم المستخدم (بدون بيانات شخصية)..."
                        AppLanguage.FRENCH -> "Nom d'utilisateur (anonymisé)..."
                        AppLanguage.ENGLISH -> "Username (no real name required)..."
                    },
                    color = Neutral500
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_username_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            placeholder = {
                Text(
                    when (lang) {
                        AppLanguage.ARABIC -> "كلمة المرور (6 أحرف على الأقل)..."
                        AppLanguage.FRENCH -> "Mot de passe (min 6 car.)..."
                        AppLanguage.ENGLISH -> "Password (at least 6 chars)..."
                    },
                    color = Neutral500
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_password_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        if (selectedTab == 1) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                placeholder = {
                    Text(
                        when (lang) {
                            AppLanguage.ARABIC -> "تأكيد كلمة المرور..."
                            AppLanguage.FRENCH -> "Confirmer le mot de passe..."
                            AppLanguage.ENGLISH -> "Confirm password..."
                        },
                        color = Neutral500
                    )
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_confirm_password_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = RecordRed
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (selectedTab == 0) {
                    isLoading = true
                    scope.launch {
                        val result = viewModel.authRepository.login(username, password)
                        isLoading = false
                        when (result) {
                            is AuthResult.Success -> { errorMessage = null }
                            is AuthResult.Error -> { errorMessage = result.message }
                        }
                    }
                } else {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val result = viewModel.authRepository.register(username, password)
                        isLoading = false
                        when (result) {
                            is AuthResult.Success -> { errorMessage = null }
                            is AuthResult.Error -> { errorMessage = result.message }
                        }
                    }
                }
            },
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("auth_submit_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = if (selectedTab == 0) {
                        when (lang) {
                            AppLanguage.ARABIC -> "دخول"
                            AppLanguage.FRENCH -> "Se connecter"
                            AppLanguage.ENGLISH -> "Sign In"
                        }
                    } else {
                        when (lang) {
                            AppLanguage.ARABIC -> "إنشاء الحساب"
                            AppLanguage.FRENCH -> "Créer un compte"
                            AppLanguage.ENGLISH -> "Create Account"
                        }
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordModal(
    lang: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (current: String, newPass: String, onError: (String) -> Unit, onSuccess: () -> Unit) -> Unit
) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "Change Password",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    placeholder = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    placeholder = { Text("New Password (min 6 chars)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                errorMsg?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, color = RecordRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(currentPass, newPass, { errorMsg = it }, { onDismiss() })
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.surface)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
