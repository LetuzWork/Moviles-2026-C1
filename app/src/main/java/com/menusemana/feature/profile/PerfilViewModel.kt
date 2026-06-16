package com.menusemana.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.domain.model.DietaryPreference
import com.menusemana.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val selected: StateFlow<Set<DietaryPreference>> =
        preferencesRepository.observePreferences()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggle(preference: DietaryPreference, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPreference(preference, enabled)
        }
    }
}
