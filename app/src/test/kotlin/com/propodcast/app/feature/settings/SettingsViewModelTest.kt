package com.propodcast.app.feature.settings

import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakeUserPreferencesRepository()

    @Test
    fun wifiOnly_followsAndUpdatesTheSetting() = runTest {
        val viewModel = SettingsViewModel(preferences)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertFalse(viewModel.uiState.value.downloadOnlyOnWifi)

        viewModel.setDownloadOnlyOnWifi(true)

        assertTrue(preferences.downloadOnlyOnWifi.value)
        assertTrue(viewModel.uiState.value.downloadOnlyOnWifi)
    }
}
