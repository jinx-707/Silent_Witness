package com.silentwitness.domain.repository

import com.silentwitness.domain.models.EntryCategory
import com.silentwitness.domain.models.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogEntryRepository {
    fun getAllEntries(): Flow<List<LogEntry>>
    suspend fun getEntryById(id: String): LogEntry?
    suspend fun addEntry(entry: LogEntry): LogEntry
    suspend fun updateEntry(id: String, updates: PartialLogEntryUpdate)
    suspend fun deleteEntry(id: String)
}

data class PartialLogEntryUpdate(
    val date: java.time.LocalDateTime? = null,
    val description: String? = null,
    val category: EntryCategory? = null,
    val injuryNotes: String? = null,
    val location: String? = null,
    val photoAttached: Boolean? = null,
    val audioAttached: Boolean? = null
)
