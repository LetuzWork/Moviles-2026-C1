package com.menusemana.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.menusemana.core.database.entity.DietaryPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferencesDao {

    @Query("SELECT * FROM dietary_preferences")
    fun getAll(): Flow<List<DietaryPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: DietaryPreferenceEntity)

    @Query("DELETE FROM dietary_preferences WHERE tag = :tag")
    suspend fun deleteByTag(tag: String)
}
