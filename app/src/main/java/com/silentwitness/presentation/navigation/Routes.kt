package com.silentwitness.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

@Serializable
object CalculatorRoute

@Serializable
object SecureGraph // parent route for the secure area

@Serializable
object Dashboard
@Serializable
object AddEditEntry
@Serializable
data class EditEntry(val id: String)
@Serializable
data class EntryDetail(val id: String)
@Serializable
object PhotoCapture
@Serializable
object AudioRecording
@Serializable
object ScreenshotImporter
@Serializable
object TrustedContacts
@Serializable
object SafetyPlan
@Serializable
object CheckInSettings
@Serializable
object RiskAssessment
@Serializable
object EscalationTimeline
@Serializable
object Export
@Serializable
data class CertificatePreview(val selectedIds: List<String> = emptyList())
@Serializable
object Settings
@Serializable
object Paywall
