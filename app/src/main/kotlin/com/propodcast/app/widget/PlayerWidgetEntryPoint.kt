package com.propodcast.app.widget

import android.content.Context
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.player.PlayerController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Hilt access for Glance classes, which the system creates without injection. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PlayerWidgetEntryPoint {
    fun playerController(): PlayerController
    fun userPreferences(): UserPreferencesRepository
}

internal fun Context.playerWidgetEntryPoint(): PlayerWidgetEntryPoint =
    EntryPointAccessors.fromApplication(applicationContext, PlayerWidgetEntryPoint::class.java)
