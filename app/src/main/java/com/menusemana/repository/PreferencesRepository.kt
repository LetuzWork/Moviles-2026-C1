package com.menusemana.repository

import com.menusemana.data.model.DietaryPreference
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observePreferences(): Flow<Set<DietaryPreference>>

    suspend fun setPreference(
        preference: DietaryPreference,
        enabled: Boolean,
    )
}
