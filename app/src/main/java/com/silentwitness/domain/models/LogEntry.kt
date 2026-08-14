package com.silentwitness.domain.models

import java.time.LocalDateTime
import java.util.UUID

data class AuditEntry(
    val action: String,
    val timestamp: LocalDateTime
)

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: LocalDateTime,
    val description: String,
    val category: EntryCategory,
    val injuryNotes: String? = null,
    val location: String? = null,
    val photoAttached: Boolean = false,
    val audioAttached: Boolean = false,
    val captureTimeUnknown: Boolean = false,
    val senderInfo: String? = null,
    val platform: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val auditTrail: List<AuditEntry> = listOf(AuditEntry("Created", createdAt))
)
