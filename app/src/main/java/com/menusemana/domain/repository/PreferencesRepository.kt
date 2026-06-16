package com.menusemana.domain.repository

import com.menusemana.domain.model.DietaryPreference
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(): Flow<Set<DietaryPreference>>

    suspend fun setPreference(
        preference: DietaryPreference,
        enabled: Boolean,
    )
}
