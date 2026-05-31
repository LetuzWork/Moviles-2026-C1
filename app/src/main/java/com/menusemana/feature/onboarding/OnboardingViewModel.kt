package com.menusemana.feature.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    companion object {
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
    }

    fun markOnboardingSeen() {
        viewModelScope.launch {
            dataStore.edit { it[ONBOARDING_SEEN] = true }
        }
    }
}
