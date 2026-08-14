package com.silentwitness.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import androidx.hilt.navigation.compose.hiltViewModel
import com.silentwitness.presentation.addedit.AddEditEntryScreen
import com.silentwitness.presentation.addedit.DraftEntryViewModel
import com.silentwitness.presentation.audiorecording.AudioRecordingScreen
import com.silentwitness.presentation.certificate.CertificatePreviewScreen
import com.silentwitness.presentation.checkin.CheckInSettingsScreen
import com.silentwitness.presentation.dashboard.DashboardScreen
import com.silentwitness.presentation.entrydetail.EntryDetailScreen
import com.silentwitness.presentation.escalationtimeline.EscalationTimelineScreen
import com.silentwitness.presentation.export.ExportScreen
import com.silentwitness.presentation.paywall.PaywallScreen
import com.silentwitness.presentation.photocapture.PhotoCaptureScreen
import com.silentwitness.presentation.riskassessment.RiskAssessmentScreen
import com.silentwitness.presentation.safetyplan.SafetyPlanScreen
import com.silentwitness.presentation.screenshotimporter.ScreenshotImporterScreen
import com.silentwitness.presentation.settings.SettingsScreen
import com.silentwitness.presentation.session.SessionViewModel
import com.silentwitness.presentation.trustedcontacts.TrustedContactsScreen

fun NavGraphBuilder.secureGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    onLock: () -> Unit
) {
    navigation<SecureGraph>(startDestination = Dashboard) {
        composable<Dashboard> {
            DashboardScreen(
                onNavigateToAddEntry = { navController.navigate(AddEditEntry) },
                onNavigateToEntryDetail = { id -> navController.navigate(EntryDetail(id)) },
                onNavigateToScreenshotImporter = { navController.navigate(ScreenshotImporter) },
                onNavigateToExport = { navController.navigate(Export) },
                onNavigateToContacts = { navController.navigate(TrustedContacts) },
                onNavigateToSafetyPlan = { navController.navigate(SafetyPlan) },
                onNavigateToTimeline = { navController.navigate(EscalationTimeline) },
                onNavigateToSettings = { navController.navigate(Settings) }
            )
        }
        composable<AddEditEntry> {
            val draftViewModel: DraftEntryViewModel = hiltViewModel(navController.getBackStackEntry(SecureGraph))
            AddEditEntryScreen(
                onSave = { entryId ->
                    navController.navigate(EntryDetail(entryId)) {
                        popUpTo(AddEditEntry) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onNavigateToPhotoCapture = { navController.navigate(PhotoCapture) },
                onNavigateToAudioRecording = { navController.navigate(AudioRecording) },
                draftViewModel = draftViewModel
            )
        }
        composable<EditEntry> { backStackEntry ->
            val id = backStackEntry.toRoute<EditEntry>().id
            val draftViewModel: DraftEntryViewModel = hiltViewModel(navController.getBackStackEntry(SecureGraph))
            AddEditEntryScreen(
                entryId = id,
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                onNavigateToPhotoCapture = { navController.navigate(PhotoCapture) },
                onNavigateToAudioRecording = { navController.navigate(AudioRecording) },
                draftViewModel = draftViewModel
            )
        }
        composable<EntryDetail> { backStackEntry ->
            val id = backStackEntry.toRoute<EntryDetail>().id
            EntryDetailScreen(
                entryId = id,
                onEdit = { navController.navigate(EditEntry(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<PhotoCapture> {
            val draftViewModel: DraftEntryViewModel = hiltViewModel(navController.getBackStackEntry(SecureGraph))
            PhotoCaptureScreen(
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                draftViewModel = draftViewModel
            )
        }
        composable<AudioRecording> {
            val draftViewModel: DraftEntryViewModel = hiltViewModel(navController.getBackStackEntry(SecureGraph))
            AudioRecordingScreen(
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                draftViewModel = draftViewModel
            )
        }
        composable<ScreenshotImporter> {
            ScreenshotImporterScreen(
                onSave = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable<TrustedContacts> {
            TrustedContactsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<SafetyPlan> {
            SafetyPlanScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<CheckInSettings> {
            CheckInSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<RiskAssessment> {
            RiskAssessmentScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<EscalationTimeline> {
            EscalationTimelineScreen(
                onEntryClick = { id -> navController.navigate(EntryDetail(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable<Export> {
            ExportScreen(
                onGenerateCertificate = { selectedIds ->
                    navController.navigate(CertificatePreview(selectedIds))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<CertificatePreview> {
            CertificatePreviewScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable<Settings> {
            SettingsScreen(
                sessionViewModel = sessionViewModel,
                onNavigateToRiskAssessment = { navController.navigate(RiskAssessment) },
                onNavigateToCheckIn = { navController.navigate(CheckInSettings) },
                onNavigateToPaywall = { navController.navigate(Paywall) },
                onLock = onLock,
                onBack = { navController.popBackStack() }
            )
        }
        composable<Paywall> {
            PaywallScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
