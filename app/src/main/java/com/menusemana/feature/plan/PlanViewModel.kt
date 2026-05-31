package com.menusemana.feature.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.PlannedMeal
import com.menusemana.domain.repository.MealRepository
import com.menusemana.domain.usecase.AssignMealToSlotUseCase
import com.menusemana.domain.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class PlanUiState(
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    val isCurrentWeek: Boolean = true,
    val plannedMeals: List<PlannedMeal> = emptyList(),
    val allMeals: List<Meal> = emptyList(),
    val pickerDay: Int? = null,
    val pickerSlot: Int? = null,
    val showMealPicker: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val mealRepository: MealRepository,
    private val assignMeal: AssignMealToSlotUseCase,
) : ViewModel() {

    private val currentWeekMonday = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    private val _weekStart = MutableStateFlow(currentWeekMonday)
    private val _state = MutableStateFlow(PlanUiState())

    val state: StateFlow<PlanUiState> = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PlanUiState(),
    )

    init {
        viewModelScope.launch {
            _weekStart.flatMapLatest { weekStart ->
                planRepository.getWeekPlan(weekStart.toEpochDay())
                    .map { weekStart to it }
            }.collect { (weekStart, planned) ->
                _state.update {
                    it.copy(
                        weekStart = weekStart,
                        isCurrentWeek = weekStart == currentWeekMonday,
                        plannedMeals = planned,
                    )
                }
            }
        }
        viewModelScope.launch {
            mealRepository.getAllMeals().collect { meals ->
                _state.update { it.copy(allMeals = meals) }
            }
        }
    }

    fun previousWeek() = _weekStart.update { it.minusWeeks(1) }
    fun nextWeek() = _weekStart.update { it.plusWeeks(1) }
    fun goToCurrentWeek() = _weekStart.update { currentWeekMonday }

    fun openMealPicker(day: Int, slot: Int) {
        _state.update { it.copy(pickerDay = day, pickerSlot = slot, showMealPicker = true) }
    }

    fun closeMealPicker() {
        _state.update { it.copy(showMealPicker = false, pickerDay = null, pickerSlot = null) }
    }

    fun assignMealToSlot(mealId: Long) {
        val day = _state.value.pickerDay ?: return
        val slot = _state.value.pickerSlot ?: return
        viewModelScope.launch {
            assignMeal(_weekStart.value.toEpochDay(), day, slot, mealId)
            closeMealPicker()
        }
    }

    fun clearSlot(day: Int, slot: Int) {
        viewModelScope.launch {
            planRepository.clearSlot(_weekStart.value.toEpochDay(), day, slot)
        }
    }
}
