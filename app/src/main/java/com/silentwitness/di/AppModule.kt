package com.silentwitness.di

import android.content.Context
import com.silentwitness.data.encryption.CryptoManager
import com.silentwitness.data.repository.SupabaseAuthRepository
import com.silentwitness.data.repository.SupabaseCheckInRepository
import com.silentwitness.data.repository.SupabaseContactsRepository
import com.silentwitness.data.repository.SupabaseLogEntryRepository
import com.silentwitness.data.repository.SupabaseSafetyPlanRepository
import com.silentwitness.domain.repository.AuthRepository
import com.silentwitness.domain.repository.CheckInRepository
import com.silentwitness.domain.repository.ContactsRepository
import com.silentwitness.domain.repository.LogEntryRepository
import com.silentwitness.domain.repository.SafetyPlanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide the default JSON serializer
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // Provide the AuthRepository
    @Provides
    @Singleton
    fun provideAuthRepository(
        client: SupabaseClient,
        @ApplicationContext context: Context
    ): AuthRepository {
        return SupabaseAuthRepository(client, context)
    }

    // Provide LogEntryRepository
    @Provides
    @Singleton
    fun provideLogEntryRepository(
        client: SupabaseClient,
        authRepository: AuthRepository,
        cryptoManager: CryptoManager,
        json: Json
    ): LogEntryRepository {
        return SupabaseLogEntryRepository(client, authRepository, cryptoManager, json)
    }

    // Provide ContactsRepository
    @Provides
    @Singleton
    fun provideContactsRepository(
        client: SupabaseClient,
        authRepository: AuthRepository,
        cryptoManager: CryptoManager,
        json: Json
    ): ContactsRepository {
        return SupabaseContactsRepository(client, authRepository, cryptoManager, json)
    }

    // Provide SafetyPlanRepository
    @Provides
    @Singleton
    fun provideSafetyPlanRepository(
        client: SupabaseClient,
        authRepository: AuthRepository,
        cryptoManager: CryptoManager,
        json: Json
    ): SafetyPlanRepository {
        return SupabaseSafetyPlanRepository(client, authRepository, cryptoManager, json)
    }

    // Provide CheckInRepository (takes appContext instead of json)
    @Provides
    @Singleton
    fun provideCheckInRepository(
        client: SupabaseClient,
        authRepository: AuthRepository,
        cryptoManager: CryptoManager,
        @ApplicationContext appContext: Context
    ): CheckInRepository {
        return SupabaseCheckInRepository(client, authRepository, cryptoManager, appContext)
    }
}
