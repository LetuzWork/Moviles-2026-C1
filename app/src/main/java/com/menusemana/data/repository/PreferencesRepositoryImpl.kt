package com.menusemana.data.repository

import com.menusemana.core.database.dao.PreferencesDao
import com.menusemana.core.database.entity.DietaryPreferenceEntity
import com.menusemana.domain.model.DietaryPreference
import com.menusemana.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesDao: PreferencesDao,
) : PreferencesRepository {

    override fun observePreferences(): Flow<Set<DietaryPreference>> =
        preferencesDao.getAll().map { rows ->
            rows.mapNotNull { row ->
                runCatching { DietaryPreference.valueOf(row.tag) }.getOrNull()
            }.toSet()
        }

    override suspend fun setPreference(preference: DietaryPreference, enabled: Boolean) {
        if (enabled) {
            preferencesDao.insert(DietaryPreferenceEntity(tag = preference.name))
        } else {
            preferencesDao.deleteByTag(preference.name)
        }
    }
}
