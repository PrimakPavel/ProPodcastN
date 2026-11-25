package com.propodcast.app.feature.favorites

import androidx.annotation.StringRes
import com.propodcast.app.R

enum class FavoritesTab(@StringRes val labelRes: Int) {
    PODCASTS(R.string.favorites_tab_podcasts),
    EPISODES(R.string.favorites_tab_episodes),
    DOWNLOADS(R.string.favorites_tab_downloads),
}
