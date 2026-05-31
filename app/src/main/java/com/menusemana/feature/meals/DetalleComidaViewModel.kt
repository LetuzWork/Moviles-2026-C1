package com.menusemana.feature.meals

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.core.common.PhotoStorage
import com.menusemana.domain.model.Meal
import com.menusemana.domain.repository.MealRepository
import com.menusemana.domain.usecase.AssignMealToSlotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class DetalleComidaUiState(
    val meal: Meal? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val showPlanPicker: Boolean = false,
    val addedToPlan: Boolean = false,
)

@HiltViewModel
class DetalleComidaViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val assignMeal: AssignMealToSlotUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mealId: Long = checkNotNull(savedStateHandle.toRoute<com.menusemana.navigation.MealDetail>().mealId)

    private val _state = MutableStateFlow(DetalleComidaUiState())
    val state: StateFlow<DetalleComidaUiState> = _state

    init {
        viewModelScope.launch {
            val meal = mealRepository.getMealById(mealId)
            _state.update { it.copy(meal = meal, isLoading = false) }
        }
    }

    fun showDeleteDialog() = _state.update { it.copy(showDeleteDialog = true) }
    fun dismissDeleteDialog() = _state.update { it.copy(showDeleteDialog = false) }

    fun deleteMeal(context: Context, onDeleted: () -> Unit) {
        val meal = _state.value.meal ?: return
        viewModelScope.launch {
            meal.photoUri?.let { uri ->
                if (!uri.startsWith("http")) PhotoStorage.deletePhoto(context, uri)
            }
            mealRepository.deleteMeal(meal)
            onDeleted()
        }
    }

    fun openPlanPicker() = _state.update { it.copy(showPlanPicker = true) }
    fun closePlanPicker() = _state.update { it.copy(showPlanPicker = false) }

    fun assignToPlan(dayOfWeek: Int, slot: Int) {
        val meal = _state.value.meal ?: return
        val weekStart = LocalDate.now()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .toEpochDay()
        viewModelScope.launch {
            assignMeal(weekStart, dayOfWeek, slot, meal.id)
            _state.update { it.copy(showPlanPicker = false, addedToPlan = true) }
        }
    }
}
