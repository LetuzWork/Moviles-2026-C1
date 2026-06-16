package com.menusemana.feature.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.core.common.MealTranslator
import com.menusemana.domain.model.Meal
import com.menusemana.domain.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MisComidasUiState(
    val meals: List<Meal> = emptyList(),
    val query: String = "",
    val selectedCategory: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class MisComidasViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    private val translator: MealTranslator,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _category = MutableStateFlow<String?>(null)

    val state: StateFlow<MisComidasUiState> = combine(
        mealRepository.getAllMeals(),
        _query,
        _category,
    ) { meals, query, category ->
        val filtered = meals
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) }
            .filter { if (category == null) true else it.category == category }
        MisComidasUiState(meals = filtered, query = query, selectedCategory = category, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MisComidasUiState())

    init {
        translateEnglishMealNames()
    }

    fun onQueryChange(query: String) { _query.value = query }
    fun onCategorySelected(category: String?) { _category.value = category }

    // One-time background pass: translate names of TheMealDB-sourced meals that are still in English
    private fun translateEnglishMealNames() {
        viewModelScope.launch {
            val meals = mealRepository.getAllMeals().first()
            val toTranslate = meals.filter { meal ->
                meal.sourceRecipeId != null && meal.name.isLikelyEnglish()
            }
            if (toTranslate.isEmpty()) return@launch
            val translated = runCatching {
                translator.translateBatch(toTranslate.map { it.name })
            }.getOrNull() ?: return@launch
            toTranslate.forEachIndexed { i, meal ->
                val es = translated.getOrNull(i)?.takeIf { it != meal.name } ?: return@forEachIndexed
                mealRepository.updateMeal(meal.copy(name = es))
            }
        }
    }

    // All-ASCII name → likely still in English (TheMealDB names are ASCII)
    private fun String.isLikelyEnglish() = all { c -> c.code < 128 }
}
