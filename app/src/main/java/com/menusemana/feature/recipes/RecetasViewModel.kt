package com.menusemana.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.core.common.ErrorType
import com.menusemana.core.common.MealTranslator
import com.menusemana.core.common.Result
import com.menusemana.domain.model.DietaryPreference
import com.menusemana.domain.model.MealCategory
import com.menusemana.domain.model.Recipe
import com.menusemana.domain.repository.PreferencesRepository
import com.menusemana.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class RecetasUiState(
    val recipes: List<Recipe> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val isOffline: Boolean = false,
    val error: ErrorType? = null,
    val activeDietaryFilters: List<String> = emptyList(),
    val mealTypeFilter: MealCategory? = null,
)

@HiltViewModel
class RecetasViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val translator: MealTranslator,
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecetasUiState())
    val state: StateFlow<RecetasUiState> = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RecetasUiState(),
    )

    private var browseRecipes: List<Recipe> = emptyList()
    private var categoryIndex = 0
    private var activePreferences: Set<DietaryPreference> = emptySet()
    private var mealTypeFilter: MealCategory? = null

    init {
        viewModelScope.launch {
            preferencesRepository.observePreferences().collect { prefs ->
                activePreferences = prefs
                val source = if (_state.value.query.isBlank()) browseRecipes else _state.value.recipes
                _state.update { it.copy(
                    activeDietaryFilters = prefs.map { p -> p.label },
                    recipes = applyFilters(source),
                ) }
            }
        }
        // Load first 3 categories in parallel so the initial view is varied
        repeat(3) { loadNextCategory() }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        if (query.isBlank()) {
            _state.update { it.copy(
                recipes = applyFilters(browseRecipes),
                error = null,
                isLoading = false,
            ) }
        } else {
            search(query)
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = recipeRepository.search(query)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        recipes = applyFilters(result.data),
                        isLoading = false,
                        isOffline = false,
                    ) }
                    launchNameTranslation(result.data, updateBrowse = false)
                }
                is Result.Error -> _state.update { it.copy(
                    isLoading = false,
                    error = result.type,
                    isOffline = result.type == ErrorType.Network,
                ) }
                is Result.Loading -> Unit
            }
        }
    }

    fun loadNextCategory() {
        while (categoryIndex < BROWSE_CATEGORIES.size && !isCategoryCompatible(BROWSE_CATEGORIES[categoryIndex])) {
            categoryIndex++
        }
        if (categoryIndex >= BROWSE_CATEGORIES.size) {
            _state.update { it.copy(hasMore = false, isLoadingMore = false) }
            return
        }
        val category = BROWSE_CATEGORIES[categoryIndex++]
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = browseRecipes.isEmpty(),
                isLoadingMore = browseRecipes.isNotEmpty(),
            ) }
            when (val result = recipeRepository.searchByCategory(category)) {
                is Result.Success -> {
                    // Show recipes immediately — don't block on translation
                    browseRecipes = browseRecipes + result.data
                    if (_state.value.query.isBlank()) {
                        _state.update { it.copy(
                            recipes = applyFilters(browseRecipes),
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = categoryIndex < BROWSE_CATEGORIES.size,
                            isOffline = false,
                            error = null,
                        ) }
                    } else {
                        _state.update { it.copy(isLoading = false, isLoadingMore = false) }
                    }
                    launchNameTranslation(result.data, updateBrowse = true)
                }
                is Result.Error -> _state.update { it.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = if (browseRecipes.isEmpty()) result.type else null,
                    isOffline = browseRecipes.isEmpty() && result.type == ErrorType.Network,
                ) }
                is Result.Loading -> Unit
            }
        }
    }

    fun setMealTypeFilter(type: MealCategory?) {
        mealTypeFilter = type
        val source = if (_state.value.query.isBlank()) browseRecipes else _state.value.recipes
        _state.update { it.copy(
            mealTypeFilter = type,
            recipes = applyFilters(source),
        ) }
    }

    fun retry() {
        if (_state.value.query.isBlank()) {
            categoryIndex = 0
            browseRecipes = emptyList()
            _state.update { it.copy(hasMore = true, error = null) }
            loadNextCategory()
        } else {
            search(_state.value.query)
        }
    }

    // Translates names in background with a 5s timeout — never blocks loading state.
    private fun launchNameTranslation(recipes: List<Recipe>, updateBrowse: Boolean) {
        viewModelScope.launch {
            val names = recipes.map { it.name }
            val translatedNames = withTimeoutOrNull(5_000) {
                runCatching { translator.translateBatch(names) }.getOrDefault(names)
            } ?: return@launch

            val idToName = recipes.mapIndexedNotNull { i, r ->
                val es = translatedNames.getOrNull(i)?.takeIf { it != r.name } ?: return@mapIndexedNotNull null
                r.mealDbId to es
            }.toMap()

            if (idToName.isEmpty()) return@launch

            if (updateBrowse) {
                browseRecipes = browseRecipes.map { r -> idToName[r.mealDbId]?.let { r.copy(nameEs = it) } ?: r }
                if (_state.value.query.isBlank()) {
                    _state.update { it.copy(recipes = applyFilters(browseRecipes)) }
                }
            } else {
                _state.update { st ->
                    if (st.query.isBlank()) st
                    else st.copy(recipes = applyFilters(st.recipes.map { r ->
                        idToName[r.mealDbId]?.let { r.copy(nameEs = it) } ?: r
                    }))
                }
            }
        }
    }

    private fun isCategoryCompatible(category: String): Boolean {
        val isVeg = activePreferences.any {
            it == DietaryPreference.VEGETARIANO || it == DietaryPreference.VEGANO
        }
        return !isVeg || category.lowercase() !in MEAT_CATEGORIES
    }

    private fun applyFilters(recipes: List<Recipe>): List<Recipe> {
        var result = recipes
        mealTypeFilter?.let { filter ->
            result = result.filter { MealTranslator.mealCategory(it.category) == filter }
        }
        if (activePreferences.isNotEmpty()) {
            result = result.filter { recipe ->
                recipe.ingredients.isEmpty() || activePreferences.all { MealTranslator.satisfies(recipe, it) }
            }
        }
        return result.sortedBy { (it.nameEs ?: it.name).lowercase() }
    }

    companion object {
        private val BROWSE_CATEGORIES = listOf(
            "Chicken", "Dessert",       // comida + colación desde el primer scroll
            "Pasta", "Breakfast",
            "Beef", "Starter",
            "Seafood", "Side",
            "Lamb", "Pork",
            "Vegetarian", "Vegan",
            "Goat", "Miscellaneous",
        )
        private val MEAT_CATEGORIES = setOf(
            "beef", "chicken", "lamb", "pork", "seafood", "goat",
        )
    }
}
