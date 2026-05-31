package com.menusemana.domain.usecase

import com.menusemana.domain.repository.PlanRepository
import javax.inject.Inject

class AssignMealToSlotUseCase @Inject constructor(
    private val planRepository: PlanRepository,
) {
    suspend operator fun invoke(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int, mealId: Long) {
        planRepository.assignMeal(weekStartEpochDay, dayOfWeek, slot, mealId)
    }
}
