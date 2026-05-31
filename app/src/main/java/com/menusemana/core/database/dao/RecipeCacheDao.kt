package com.menusemana.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.menusemana.core.database.entity.RecipeCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeCacheDao {

    @Query("SELECT * FROM recipe_cache WHERE mealDbId = :id")
    suspend fun getById(id: String): RecipeCacheEntity?

    @Query("SELECT * FROM recipe_cache ORDER BY cachedAt DESC")
    fun getAllCached(): Flow<List<RecipeCacheEntity>>

    @Query("SELECT * FROM recipe_cache WHERE name LIKE '%' || :query || '%' ORDER BY cachedAt DESC")
    suspend fun searchCached(query: String): List<RecipeCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: RecipeCacheEntity)
}
