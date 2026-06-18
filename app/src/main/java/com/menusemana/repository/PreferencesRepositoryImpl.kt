package com.menusemana.repository

import com.menusemana.data.db.dao.PreferencesDao
import com.menusemana.data.db.entity.DietaryPreferenceEntity
import com.menusemana.data.model.DietaryPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl
    @Inject
    constructor(
        private val preferencesDao: PreferencesDao,
    ) : PreferencesRepository {
        override fun observePreferences(): Flow<Set<DietaryPreference>> =
            preferencesDao.getAll().map { rows ->
                rows
                    .mapNotNull { row ->
                        runCatching { DietaryPreference.valueOf(row.tag) }.getOrNull()
                    }.toSet()
            }

        override suspend fun setPreference(
            preference: DietaryPreference,
            enabled: Boolean,
        ) {
            if (enabled) {
                preferencesDao.insert(DietaryPreferenceEntity(tag = preference.name))
            } else {
                preferencesDao.deleteByTag(preference.name)
            }
        }
    }
