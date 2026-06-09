package com.menusemana.feature.meals

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.domain.model.Aisle
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.MealCategory
import com.menusemana.domain.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IngredientInput(
    val name: String = "",
    val quantity: String = "",
    val aisle: String = Aisle.ALMACEN.label,
)

data class AddEditMealUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val photoUri: Uri? = null,
    val timeMinutes: String = "",
    val servings: String = "",
    val category: String = MealCategory.COMIDA.label,
    val notes: String = "",
    val ingredients: List<IngredientInput> = listOf(IngredientInput()),
    val nameError: Boolean = false,
    val servingsError: Boolean = false,
    val isSaving: Boolean = false,
)

@HiltViewModel
class AddEditMealViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val mealId: Long? = runCatching {
        savedStateHandle.toRoute<com.menusemana.navigation.EditMeal>().mealId.takeIf { it > 0 }
    }.getOrNull()

    private val _state = MutableStateFlow(AddEditMealUiState())
    val state: StateFlow<AddEditMealUiState> = _state

    init {
        if (mealId != null) {
            viewModelScope.launch {
                val meal = mealRepository.getMealById(mealId) ?: return@launch
                _state.update {
                    it.copy(
                        name = meal.name,
                        photoUri = meal.photoUri?.let(Uri::parse),
                        timeMinutes = meal.timeMinutes.toString(),
                        servings = meal.servings.toString(),
                        category = meal.category,
                        notes = meal.notes ?: "",
                        ingredients = meal.ingredients.map { ing ->
                            IngredientInput(ing.name, ing.quantity, ing.aisle)
                        }.ifEmpty { listOf(IngredientInput()) },
                    )
                }
            }
        }
    }

    fun onNameChange(v: String) = _state.update { it.copy(name = v, nameError = false) }
    fun onPhotoTaken(uri: Uri) = _state.update { it.copy(photoUri = uri) }
    fun onTimeChange(v: String) = _state.update { it.copy(timeMinutes = v) }
    fun onServingsChange(v: String) = _state.update { it.copy(servings = v, servingsError = false) }
    fun onCategoryChange(v: String) = _state.update { it.copy(category = v) }
    fun onNotesChange(v: String) = _state.update { it.copy(notes = v) }

    fun onIngredientNameChange(index: Int, v: String) = updateIngredient(index) { it.copy(name = v) }
    fun onIngredientQuantityChange(index: Int, v: String) = updateIngredient(index) { it.copy(quantity = v) }
    fun onIngredientAisleChange(index: Int, v: String) = updateIngredient(index) { it.copy(aisle = v) }

    fun addIngredient() = _state.update { it.copy(ingredients = it.ingredients + IngredientInput()) }
    fun removeIngredient(index: Int) = _state.update {
        it.copy(ingredients = it.ingredients.toMutableList().also { list -> list.removeAt(index) })
    }

    private fun updateIngredient(index: Int, transform: (IngredientInput) -> IngredientInput) {
        _state.update {
            it.copy(ingredients = it.ingredients.toMutableList().also { list -> list[index] = transform(list[index]) })
        }
    }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        val nameValid = s.name.isNotBlank()
        val servingsValid = s.servings.toIntOrNull()?.let { it >= 1 } ?: false

        if (!nameValid || !servingsValid) {
            _state.update { it.copy(nameError = !nameValid, servingsError = !servingsValid) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val meal = Meal(
                id = mealId ?: 0,
                name = s.name.trim(),
                photoUri = s.photoUri?.toString(),
                timeMinutes = s.timeMinutes.toIntOrNull() ?: 0,
                servings = s.servings.toInt(),
                category = s.category,
                notes = s.notes.takeIf { it.isNotBlank() },
                ingredients = s.ingredients
                    .filter { it.name.isNotBlank() }
                    .map { Ingredient(name = it.name, quantity = it.quantity, aisle = it.aisle) },
            )
            if (mealId == null) mealRepository.saveMeal(meal)
            else mealRepository.updateMeal(meal)
            onSaved()
        }
    }
}
