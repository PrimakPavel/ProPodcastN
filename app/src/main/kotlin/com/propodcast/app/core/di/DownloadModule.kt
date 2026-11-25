package com.propodcast.app.core.di

import android.content.Context
import androidx.work.WorkManager
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.WorkManagerDownloadsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadModule {
    @Binds
    abstract fun bindDownloadsRepository(impl: WorkManagerDownloadsRepository): DownloadsRepository

    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager = WorkManager.getInstance(context)

        /** Audio hosts can be slow; follows redirects (Listen Notes audio links redirect to the host). */
        @Provides
        @Singleton
        @DownloadClient
        fun provideDownloadClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
