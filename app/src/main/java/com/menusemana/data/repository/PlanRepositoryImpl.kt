package com.menusemana.data.repository

import com.menusemana.core.database.dao.MealDao
import com.menusemana.core.database.dao.PlanDao
import com.menusemana.core.database.entity.PlannedMealEntity
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.PlannedMeal
import com.menusemana.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class PlanRepositoryImpl @Inject constructor(
    private val planDao: PlanDao,
    private val mealDao: MealDao,
) : PlanRepository {

    override fun getWeekPlan(weekStartEpochDay: Long): Flow<List<PlannedMeal>> =
        combine(
            planDao.getPlannedMealsForWeek(weekStartEpochDay),
            mealDao.getAllMealsWithIngredients(),
        ) { planned, meals ->
            val mealMap = meals.associate { mw ->
                mw.meal.id to Meal(
                    id = mw.meal.id,
                    name = mw.meal.name,
                    photoUri = mw.meal.photoUri,
                    timeMinutes = mw.meal.timeMinutes,
                    servings = mw.meal.servings,
                    category = mw.meal.category,
                    notes = mw.meal.notes,
                    ingredients = mw.ingredients.map {
                        Ingredient(it.id, it.name, it.quantity, it.aisle)
                    },
                )
            }
            planned.mapNotNull { p ->
                mealMap[p.mealId]?.let { meal ->
                    PlannedMeal(
                        id = p.id,
                        weekStartEpochDay = p.weekStartEpochDay,
                        dayOfWeek = p.dayOfWeek,
                        slot = p.slot,
                        meal = meal,
                    )
                }
            }
        }

    override suspend fun assignMeal(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int, mealId: Long) {
        planDao.insertOrReplace(
            PlannedMealEntity(
                weekStartEpochDay = weekStartEpochDay,
                dayOfWeek = dayOfWeek,
                slot = slot,
                mealId = mealId,
            )
        )
    }

    override suspend fun clearSlot(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int) {
        planDao.clearSlot(weekStartEpochDay, dayOfWeek, slot)
    }
}
