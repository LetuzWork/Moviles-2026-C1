package com.menusemana.repository

import com.menusemana.data.model.Meal
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getAllMeals(): Flow<List<Meal>>

    suspend fun getMealById(id: Long): Meal?

    suspend fun saveMeal(meal: Meal): Long

    suspend fun updateMeal(meal: Meal)

    suspend fun deleteMeal(meal: Meal)
}
