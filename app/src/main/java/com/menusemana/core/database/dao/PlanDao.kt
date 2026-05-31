package com.menusemana.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.menusemana.core.database.entity.PlannedMealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    @Query("SELECT * FROM planned_meals WHERE weekStartEpochDay = :weekStart")
    fun getPlannedMealsForWeek(weekStart: Long): Flow<List<PlannedMealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(plannedMeal: PlannedMealEntity)

    @Delete
    suspend fun delete(plannedMeal: PlannedMealEntity)

    @Query("DELETE FROM planned_meals WHERE weekStartEpochDay = :weekStart AND dayOfWeek = :day AND slot = :slot")
    suspend fun clearSlot(weekStart: Long, day: Int, slot: Int)
}
