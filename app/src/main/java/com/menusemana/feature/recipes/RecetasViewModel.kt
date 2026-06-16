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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    // All fetched recipes per category, in BROWSE_CATEGORIES order
    private val categoryRecipes = LinkedHashMap<String, List<Recipe>>()
    private var perPage = INITIAL_PER_CATEGORY
    private var activePreferences: Set<DietaryPreference> = emptySet()
    private var mealTypeFilter: MealCategory? = null

    init {
        viewModelScope.launch {
            preferencesRepository.observePreferences().collect { prefs ->
                activePreferences = prefs
                _state.update { it.copy(activeDietaryFilters = prefs.map { p -> p.label }) }
                if (_state.value.query.isBlank()) updateVisible()
            }
        }
        loadAllCategories()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        if (query.isBlank()) {
            _state.update { it.copy(error = null, isLoading = false) }
            updateVisible()
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

    // Called when user scrolls to the bottom — reveals more recipes per category
    fun loadNextPage() {
        if (!_state.value.hasMore) return
        perPage += MORE_PER_CATEGORY
        updateVisible()
    }

    fun setMealTypeFilter(type: MealCategory?) {
        mealTypeFilter = type
        _state.update { it.copy(mealTypeFilter = type) }
        if (_state.value.query.isBlank()) updateVisible()
        else _state.update { it.copy(recipes = applyFilters(it.recipes)) }
    }

    fun retry() {
        if (_state.value.query.isBlank()) {
            categoryRecipes.clear()
            perPage = INITIAL_PER_CATEGORY
            _state.update { it.copy(hasMore = true, error = null) }
            loadAllCategories()
        } else {
            search(_state.value.query)
        }
    }

    private fun loadAllCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val compatible = BROWSE_CATEGORIES.filter { isCategoryCompatible(it) }
            val allLoaded = mutableListOf<Recipe>()

            try {
                coroutineScope {
                    compatible.map { category ->
                        async {
                            when (val r = recipeRepository.searchByCategory(category)) {
                                is Result.Success -> category to r.data
                                else -> null
                            }
                        }
                    }.awaitAll()
                }.filterNotNull().forEach { (cat, recipes) ->
                    categoryRecipes[cat] = recipes
                    allLoaded += recipes
                }
            } catch (_: Exception) { }

            val hasAny = categoryRecipes.isNotEmpty()
            _state.update { it.copy(
                isLoading = false,
                isOffline = !hasAny,
                error = if (!hasAny) ErrorType.Network else null,
            ) }
            updateVisible()
            if (allLoaded.isNotEmpty()) launchNameTranslation(allLoaded, updateBrowse = true)
        }
    }

    // Slices perPage recipes from each category, preserving category insertion order
    private fun buildVisible(): List<Recipe> =
        BROWSE_CATEGORIES.flatMap { cat -> categoryRecipes[cat]?.take(perPage) ?: emptyList() }

    private fun updateVisible() {
        val visible = buildVisible()
        val hasMore = categoryRecipes.any { (_, list) -> list.size > perPage }
        _state.update { it.copy(
            recipes = applyFilters(visible),
            hasMore = hasMore,
            isLoadingMore = false,
        ) }
    }

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
                val updated = categoryRecipes.mapValues { (_, list) ->
                    list.map { r -> idToName[r.mealDbId]?.let { r.copy(nameEs = it) } ?: r }
                }
                categoryRecipes.clear()
                categoryRecipes.putAll(updated)
                if (_state.value.query.isBlank()) updateVisible()
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
        return result // insertion order preserved — no sort
    }

    companion object {
        private const val INITIAL_PER_CATEGORY = 4
        private const val MORE_PER_CATEGORY = 3
        private val BROWSE_CATEGORIES = listOf(
            "Chicken", "Dessert", "Pasta", "Breakfast", "Beef", "Starter",
            "Seafood", "Side", "Lamb", "Pork", "Vegetarian", "Vegan", "Goat", "Miscellaneous",
        )
        private val MEAT_CATEGORIES = setOf("beef", "chicken", "lamb", "pork", "seafood", "goat")
    }
}
