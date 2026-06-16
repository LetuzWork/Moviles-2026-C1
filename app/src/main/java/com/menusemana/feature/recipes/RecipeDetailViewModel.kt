package com.menusemana.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.core.common.AisleClassifier
import com.menusemana.core.common.MealTranslator
import com.menusemana.core.common.Result
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.Recipe
import com.menusemana.domain.repository.MealRepository
import com.menusemana.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecipeDetailUiState(
    val recipe: Recipe? = null,
    val resolvedIngredients: List<Pair<String, String>> = emptyList(),
    val translatedArea: String? = null,
    val translatedCategory: String? = null,
    val isLoading: Boolean = true,
    val isTranslating: Boolean = false,
    val imported: Boolean = false,
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val mealRepository: MealRepository,
    private val translator: MealTranslator,
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeDetailUiState())
    val state: StateFlow<RecipeDetailUiState> = _state

    fun load(mealDbId: String) {
        viewModelScope.launch {
            when (val result = recipeRepository.getById(mealDbId)) {
                is Result.Success -> {
                    val recipe = result.data
                    _state.update { it.copy(
                        recipe = recipe,
                        resolvedIngredients = recipe.resolveIngredients(),
                        translatedArea = recipe.area?.let { translator.translateArea(it) },
                        translatedCategory = recipe.category?.let { translator.translateCategory(it) },
                        isLoading = false,
                    ) }
                    val needsTranslation = recipe.instructionsEs == null || recipe.ingredientsEs == null
                    if (needsTranslation) translateRecipe(recipe)
                }
                is Result.Error -> _state.update { it.copy(isLoading = false) }
                is Result.Loading -> Unit
            }
        }
    }

    private fun translateRecipe(recipe: Recipe) {
        viewModelScope.launch {
            _state.update { it.copy(isTranslating = true) }

            // Compute local ingredient translations up front (pure, no IO)
            val localPairs = if (recipe.ingredientsEs == null && recipe.ingredients.isNotEmpty()) {
                recipe.ingredients.map { (name, measure) -> translator.translatePair(name, measure) }
            } else null

            val unknownIdx = localPairs?.indices
                ?.filter { i -> localPairs[i].first == recipe.ingredients[i].first }
                ?: emptyList()
            val unknownNames = unknownIdx.map { recipe.ingredients[it].first }

            // Fire independent API calls in parallel
            var updatedRecipe = recipe
            coroutineScope {
                val instrDeferred = if (recipe.instructions != null && recipe.instructionsEs == null) {
                    async { translator.translateToSpanish(recipe.instructions) }
                } else null

                val namesDeferred = if (localPairs != null && unknownNames.isNotEmpty()) {
                    async { runCatching { translator.translateBatch(unknownNames) }.getOrDefault(unknownNames) }
                } else null

                instrDeferred?.await()?.let { translated ->
                    recipeRepository.saveTranslation(recipe.mealDbId, translated)
                    updatedRecipe = updatedRecipe.copy(instructionsEs = translated)
                }

                if (localPairs != null) {
                    val apiNames = namesDeferred?.await() ?: unknownNames
                    val finalPairs = if (unknownIdx.isEmpty()) localPairs else {
                        localPairs.toMutableList().also { list ->
                            unknownIdx.zip(apiNames).forEach { (recipeIdx, apiName) ->
                                if (apiName != recipe.ingredients[recipeIdx].first) {
                                    list[recipeIdx] = apiName to list[recipeIdx].second
                                }
                            }
                        }
                    }
                    recipeRepository.saveIngredientTranslations(recipe.mealDbId, finalPairs)
                    updatedRecipe = updatedRecipe.copy(ingredientsEs = finalPairs)
                }
            }

            _state.update { it.copy(
                recipe = updatedRecipe,
                resolvedIngredients = updatedRecipe.resolveIngredients(),
                isTranslating = false,
            ) }
        }
    }

    private fun Recipe.resolveIngredients(): List<Pair<String, String>> =
        ingredientsEs ?: ingredients.map { (name, measure) -> translator.translatePair(name, measure) }

    fun importToMyMeals() {
        val state = _state.value
        val recipe = state.recipe ?: return
        viewModelScope.launch {
            val meal = Meal(
                name = recipe.name,
                photoUri = recipe.thumbUrl,
                category = MealTranslator.mealCategory(recipe.category).label,
                notes = recipe.instructionsEs ?: recipe.instructions,
                sourceRecipeId = recipe.mealDbId,
                ingredients = state.resolvedIngredients.mapIndexed { i, (name, measure) ->
                    Ingredient(
                        name = name,
                        quantity = measure,
                        aisle = AisleClassifier.classify(recipe.ingredients.getOrNull(i)?.first ?: name),
                    )
                },
            )
            mealRepository.saveMeal(meal)
            _state.update { it.copy(imported = true) }
        }
    }
}
