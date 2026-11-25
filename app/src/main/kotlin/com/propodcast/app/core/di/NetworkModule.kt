package com.propodcast.app.core.di

import com.propodcast.app.BuildConfig
import com.propodcast.app.core.data.repository.InMemoryApiQuotaRepository
import com.propodcast.app.core.network.ApiKeyInterceptor
import com.propodcast.app.core.network.ApiQuotaInterceptor
import com.propodcast.app.core.network.ListenNotesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        config: ApiConfig,
        quotaRepository: InMemoryApiQuotaRepository,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(ApiKeyInterceptor(config.apiKey))
        .addInterceptor(ApiQuotaInterceptor(quotaRepository::update))
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                        redactHeader(ApiKeyInterceptor.HEADER)
                    },
                )
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideListenNotesApi(config: ApiConfig, client: OkHttpClient, json: Json): ListenNotesApi =
        Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ListenNotesApi::class.java)
}
