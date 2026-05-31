package com.menusemana.util

import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.PlannedMeal
import com.menusemana.domain.repository.MealRepository
import com.menusemana.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeMealRepository : MealRepository {
    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val savedMeals = mutableListOf<Meal>()
    val updatedMeals = mutableListOf<Meal>()
    val deletedMeals = mutableListOf<Meal>()

    fun setMeals(meals: List<Meal>) { _meals.value = meals }

    override fun getAllMeals(): Flow<List<Meal>> = _meals
    override suspend fun getMealById(id: Long): Meal? = _meals.value.find { it.id == id }
    override suspend fun saveMeal(meal: Meal): Long {
        savedMeals += meal
        val withId = meal.copy(id = savedMeals.size.toLong())
        _meals.value = _meals.value + withId
        return withId.id
    }
    override suspend fun updateMeal(meal: Meal) { updatedMeals += meal }
    override suspend fun deleteMeal(meal: Meal) { deletedMeals += meal }
}

class FakePlanRepository : PlanRepository {
    private val _plan = MutableStateFlow<List<PlannedMeal>>(emptyList())
    val assignedMeals = mutableListOf<Triple<Long, Int, Int>>()
    val clearedSlots = mutableListOf<Pair<Int, Int>>()

    fun setPlan(meals: List<PlannedMeal>) { _plan.value = meals }

    override fun getWeekPlan(weekStartEpochDay: Long): Flow<List<PlannedMeal>> = _plan
    override suspend fun assignMeal(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int, mealId: Long) {
        assignedMeals += Triple(mealId, dayOfWeek, slot)
    }
    override suspend fun clearSlot(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int) {
        clearedSlots += Pair(dayOfWeek, slot)
    }
}
