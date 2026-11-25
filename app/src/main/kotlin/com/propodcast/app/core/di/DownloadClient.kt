package com.propodcast.app.core.di

import javax.inject.Qualifier

/** OkHttpClient for episode audio files: no Listen Notes key, longer timeouts. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DownloadClient
