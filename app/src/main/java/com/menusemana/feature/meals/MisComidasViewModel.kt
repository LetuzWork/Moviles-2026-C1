package com.menusemana.feature.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.domain.model.Meal
import com.menusemana.domain.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

    fun onQueryChange(query: String) { _query.value = query }
    fun onCategorySelected(category: String?) { _category.value = category }

}