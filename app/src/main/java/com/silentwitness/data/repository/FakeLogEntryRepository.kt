package com.silentwitness.data.repository

import com.silentwitness.domain.models.AuditEntry
import com.silentwitness.domain.models.LogEntry
import com.silentwitness.domain.repository.LogEntryRepository
import com.silentwitness.domain.repository.PartialLogEntryUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class FakeLogEntryRepository : LogEntryRepository {
    private val _entries = MutableStateFlow<List<LogEntry>>(initialEntries())
    override fun getAllEntries(): Flow<List<LogEntry>> = _entries.asStateFlow()

    override suspend fun getEntryById(id: String): LogEntry? =
        _entries.value.find { it.id == id }

    override suspend fun addEntry(entry: LogEntry): LogEntry {
        val newEntry = entry.copy(
            id = java.util.UUID.randomUUID().toString(),
            createdAt = LocalDateTime.now(),
            auditTrail = listOf(AuditEntry("Created", LocalDateTime.now()))
        )
        _entries.value = _entries.value + newEntry
        return newEntry
    }

    override suspend fun updateEntry(id: String, updates: PartialLogEntryUpdate) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) {
                entry.copy(
                    date = updates.date ?: entry.date,
                    description = updates.description ?: entry.description,
                    category = updates.category ?: entry.category,
                    injuryNotes = updates.injuryNotes ?: entry.injuryNotes,
                    location = updates.location ?: entry.location,
                    photoAttached = updates.photoAttached ?: entry.photoAttached,
                    audioAttached = updates.audioAttached ?: entry.audioAttached,
                    auditTrail = entry.auditTrail + AuditEntry("Edited", LocalDateTime.now())
                )
            } else entry
        }
    }

    override suspend fun deleteEntry(id: String) {
        _entries.value = _entries.value.filter { it.id != id }
    }

    private fun initialEntries(): List<LogEntry> = listOf(
        LogEntry(
            id = "1",
            date = LocalDateTime.of(2026, 8, 5, 14, 30),
            description = "Raised voice during dinner, threatened to take the children to his parents' house and not return.",
            category = "verbal",
            createdAt = LocalDateTime.of(2026, 8, 5, 22, 15),
            auditTrail = listOf(AuditEntry("Created", LocalDateTime.of(2026, 8, 5, 22, 15)))
        ),
        LogEntry(
            id = "2",
            date = LocalDateTime.of(2026, 8, 3, 9, 0),
            description = "Blocked access to the joint bank account by changing the online banking password. Could not pay for groceries.",
            category = "financial",
            createdAt = LocalDateTime.of(2026, 8, 3, 20, 0),
            auditTrail = listOf(AuditEntry("Created", LocalDateTime.of(2026, 8, 3, 20, 0)))
        ),
        LogEntry(
            id = "3",
            date = LocalDateTime.of(2026, 7, 28, 19, 0),
            description = "Grabbed left arm while I was trying to leave the kitchen. Left a bruise.",
            category = "physical",
            injuryNotes = "Bruising on left forearm, approximately 4cm diameter, visible for 5 days.",
            createdAt = LocalDateTime.of(2026, 7, 28, 21, 30),
            photoAttached = true,
            auditTrail = listOf(AuditEntry("Created", LocalDateTime.of(2026, 7, 28, 21, 30)))
        ),
        LogEntry(
            id = "4",
            date = LocalDateTime.of(2026, 7, 20, 22, 0),
            description = "Checked my phone without permission, deleted several contacts.",
            category = "digital",
            createdAt = LocalDateTime.of(2026, 7, 20, 22, 45),
            auditTrail = listOf(AuditEntry("Created", LocalDateTime.of(2026, 7, 20, 22, 45)))
        )
    )
}
