package com.menusemana.domain.usecase

import com.menusemana.domain.model.Aisle
import com.menusemana.domain.model.ShoppingItem
import com.menusemana.domain.model.ShoppingSection
import com.menusemana.domain.repository.MealRepository
import com.menusemana.domain.repository.PlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GenerateShoppingListUseCase @Inject constructor(
    private val planRepository: PlanRepository,
    private val mealRepository: MealRepository,
) {
    operator fun invoke(weekStartEpochDay: Long): Flow<List<ShoppingSection>> =
        combine(
            planRepository.getWeekPlan(weekStartEpochDay),
            mealRepository.getAllMeals(),
        ) { plannedMeals, allMeals ->
            val mealMap = allMeals.associateBy { it.id }
            val allIngredients = plannedMeals
                .mapNotNull { mealMap[it.meal.id] }
                .flatMap { it.ingredients }

            val deduped = allIngredients
                .groupBy { it.name.lowercase().trim() }
                .map { (_, group) ->
                    val first = group.first()
                    ShoppingItem(
                        name = first.name,
                        quantity = group.joinToString(", ") { it.quantity },
                        aisle = first.aisle,
                    )
                }

            val aisleOrder = Aisle.entries.map { it.label }
            deduped
                .groupBy { it.aisle }
                .entries
                .sortedBy { (aisle, _) -> aisleOrder.indexOf(aisle).takeIf { it >= 0 } ?: Int.MAX_VALUE }
                .map { (aisle, items) -> ShoppingSection(aisle, items) }
        }
}
