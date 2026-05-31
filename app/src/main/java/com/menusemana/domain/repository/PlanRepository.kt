package com.menusemana.domain.repository

import com.menusemana.domain.model.PlannedMeal
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    fun getWeekPlan(weekStartEpochDay: Long): Flow<List<PlannedMeal>>
    suspend fun assignMeal(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int, mealId: Long)
    suspend fun clearSlot(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int)
}
