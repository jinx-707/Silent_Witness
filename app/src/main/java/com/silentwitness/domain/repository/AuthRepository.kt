package com.silentwitness.domain.repository

interface AuthRepository {
    /**
     * Guarantees a signed-in Supabase anonymous session and returns the current user id.
     * Returns "" in dev mode (no backend configured).
     */
    suspend fun ensureSignedIn(): String

    /** Returns the cached user id, or null if not signed in yet. */
    suspend fun getCurrentUserId(): String?
}
