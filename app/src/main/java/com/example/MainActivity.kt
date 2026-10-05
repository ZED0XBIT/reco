package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.PublicUploadWarningSheet
import com.example.ui.components.ReportDialog
import com.example.ui.navigation.AppBottomNav
import com.example.ui.screens.detail.RecordingDetailScreen
import com.example.ui.screens.feed.PublicFeedScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.legal.LegalDetailSheet
import com.example.ui.screens.legal.LegalGateScreen
import com.example.ui.screens.myrecordings.MyRecordingsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.record.RecordScreen
import com.example.ui.theme.ProtectYourselfTheme
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.NavTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()

            ProtectYourselfTheme(themeMode = themeMode) {
                ProtectYourselfApp(viewModel)
            }
        }
    }
}

@Composable
fun ProtectYourselfApp(
    viewModel: MainViewModel
) {
    val legalGateAccepted by viewModel.legalGateAccepted.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedRecording by viewModel.selectedRecording.collectAsState()
    val selectedIncident by viewModel.selectedIncident.collectAsState()
    val recentRecordings by viewModel.recordingRepository.allPublicRecordings.collectAsState(initial = emptyList())

    val showLegalSheet by viewModel.showLegalSheet.collectAsState()
    val showPublicWarningSheet by viewModel.showPublicWarningSheet.collectAsState()
    val reportingRecordingId by viewModel.reportingRecordingId.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Dynamic RTL support according to language (Section 23)
    val layoutDirection = if (currentLang.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        if (!legalGateAccepted) {
            // First Access Legal Information Gate (Section 3)
            LegalGateScreen(
                currentLanguage = currentLang,
                onLanguageChange = { viewModel.setLanguage(it) },
                onOpenLegalSheet = { viewModel.openLegalSheet() },
                onAcceptAndEnter = { viewModel.acceptLegalGate() }
            )
        } else {
            // Main Application Architecture
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (selectedRecording == null && selectedIncident == null) {
                        AppBottomNav(
                            currentTab = currentTab,
                            lang = currentLang,
                            onTabSelected = { viewModel.navigateToTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (selectedIncident != null) {
                        com.example.ui.screens.detail.IncidentDetailScreen(
                            incident = selectedIncident!!,
                            viewModel = viewModel,
                            lang = currentLang,
                            onBack = { viewModel.closeIncidentDetail() }
                        )
                    } else if (selectedRecording != null) {
                        RecordingDetailScreen(
                            recording = selectedRecording!!,
                            viewModel = viewModel,
                            lang = currentLang,
                            onBack = { viewModel.closeRecordingDetail() }
                        )
                    } else {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_transition"
                        ) { tab ->
                            when (tab) {
                                NavTab.HOME -> HomeScreen(
                                    lang = currentLang,
                                    recentRecordings = recentRecordings,
                                    onNavigateToRecord = { viewModel.navigateToTab(NavTab.RECORD) },
                                    onNavigateToFeed = { viewModel.navigateToTab(NavTab.FEED) },
                                    onNavigateToMyRecordings = { viewModel.navigateToTab(NavTab.MY_AUDIOS) },
                                    onOpenLegalReference = { viewModel.openLegalSheet() },
                                    onOpenRecordingDetail = { viewModel.openRecordingDetail(it) },
                                    onStartIncident = {
                                        viewModel.startNewIncident()
                                        viewModel.navigateToTab(NavTab.RECORD)
                                    }
                                )
                                NavTab.FEED -> PublicFeedScreen(
                                    viewModel = viewModel,
                                    lang = currentLang,
                                    onOpenRecordingDetail = { viewModel.openRecordingDetail(it) },
                                    onOpenIncidentDetail = { viewModel.openIncidentDetail(it) }
                                )
                                NavTab.RECORD -> RecordScreen(
                                    viewModel = viewModel,
                                    lang = currentLang
                                )
                                NavTab.MY_AUDIOS -> MyRecordingsScreen(
                                    viewModel = viewModel,
                                    lang = currentLang,
                                    onOpenRecordingDetail = { viewModel.openRecordingDetail(it) }
                                )
                                NavTab.ACCOUNT -> ProfileScreen(
                                    viewModel = viewModel,
                                    lang = currentLang,
                                    onOpenLegalReference = { viewModel.openLegalSheet() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dedicated Tunisian Legal Sources Modal Sheet
        if (showLegalSheet) {
            LegalDetailSheet(
                lang = currentLang,
                onDismiss = { viewModel.closeLegalSheet() }
            )
        }

        // Mandatory Public Upload Warning Sheet (Section 5)
        if (showPublicWarningSheet) {
            val draft by viewModel.publishDraft.collectAsState()
            val currentUser by viewModel.authRepository.currentUser.collectAsState()
            PublicUploadWarningSheet(
                lang = currentLang,
                uploaderUsername = currentUser?.username ?: "citizen_user",
                draft = draft,
                isPublishing = isPublishing,
                onConfirmPublish = { viewModel.confirmAndPublishPublicly() },
                onCancel = { viewModel.dismissPublicWarning() }
            )
        }

        // Moderation / Abuse Report Modal (Section 14)
        reportingRecordingId?.let { recId ->
            ReportDialog(
                recordingId = recId,
                lang = currentLang,
                onDismiss = { viewModel.dismissReporting() },
                onSubmitReport = { reason, comment ->
                    viewModel.submitReport(reason, comment)
                }
            )
        }

        // QR Evidence Verification Card Sheet (Section 29)
        val showQrEvidenceCard by viewModel.showQrEvidenceCard.collectAsState()
        showQrEvidenceCard?.let { rec ->
            com.example.ui.components.QrEvidenceCardSheet(
                recording = rec,
                currentLanguage = currentLang,
                onDismiss = { viewModel.closeQrEvidenceCard() },
                onShowSnackbar = { viewModel.clearSnackbar() }
            )
        }
    }
}
