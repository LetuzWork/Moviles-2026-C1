package com.menusemana.repository

import com.menusemana.data.model.PlannedMeal
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    fun getWeekPlan(weekStartEpochDay: Long): Flow<List<PlannedMeal>>

    suspend fun assignMeal(
        weekStartEpochDay: Long,
        dayOfWeek: Int,
        slot: Int,
        mealId: Long,
    )

    suspend fun clearSlot(
        weekStartEpochDay: Long,
        dayOfWeek: Int,
        slot: Int,
    )
}
