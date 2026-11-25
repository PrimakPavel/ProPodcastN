package com.propodcast.app.core.di

import com.propodcast.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Listen Notes API settings, taken from BuildConfig (debug → mock server, release → production). */
data class ApiConfig(
    val baseUrl: String,
    val apiKey: String,
)

@Module
@InstallIn(SingletonComponent::class)
object ApiConfigModule {
    @Provides
    fun provideApiConfig(): ApiConfig = ApiConfig(
        baseUrl = BuildConfig.LISTEN_NOTES_BASE_URL,
        apiKey = BuildConfig.LISTEN_NOTES_API_KEY,
    )
}
