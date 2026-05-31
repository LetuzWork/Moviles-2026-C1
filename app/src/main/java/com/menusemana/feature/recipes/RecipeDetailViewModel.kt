package com.menusemana.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.core.common.AisleClassifier
import com.menusemana.core.common.IngredientTranslator
import com.menusemana.core.common.MetricConverter
import com.menusemana.core.common.Result
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.MealCategory
import com.menusemana.domain.model.Recipe
import com.menusemana.domain.repository.MealRepository
import com.menusemana.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val isLoading: Boolean = true,
    val imported: Boolean = false,
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val mealRepository: MealRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state

    fun load(mealDbId: String) {
        viewModelScope.launch {
            when (val result = recipeRepository.getById(mealDbId)) {
                is Result.Success -> _state.update { it.copy(recipe = result.data, isLoading = false) }
                is Result.Error -> _state.update { it.copy(isLoading = false) }
                is Result.Loading -> Unit
            }
        }
    }

    fun importToMyMeals() {
        val recipe = _state.value.recipe ?: return
        viewModelScope.launch {
            val meal = Meal(
                name = recipe.name,
                photoUri = recipe.thumbUrl,
                category = MealCategory.COMIDA.label,
                notes = recipe.instructions,
                sourceRecipeId = recipe.mealDbId,
                ingredients = recipe.ingredients.map { (name, measure) ->
                    Ingredient(
                        name = IngredientTranslator.translate(name),
                        quantity = MetricConverter.convert(measure).ifBlank { measure },
                        aisle = AisleClassifier.classify(name),
                    )
                },
            )
            mealRepository.saveMeal(meal)
            _state.update { it.copy(imported = true) }
        }
    }
}
