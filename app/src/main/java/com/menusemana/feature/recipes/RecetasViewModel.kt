package com.menusemana.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.core.common.ErrorType
import com.menusemana.core.common.Result
import com.menusemana.domain.model.Recipe
import com.menusemana.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecetasUiState(
    val recipes: List<Recipe> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val error: ErrorType? = null,
)

@HiltViewModel
class RecetasViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecetasUiState())
    val state: StateFlow<RecetasUiState> = _state.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RecetasUiState(),
    )

    init {
        search("")
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        search(query)
    }

    fun search(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = recipeRepository.search(query)) {
                is Result.Success -> _state.update {
                    it.copy(recipes = result.data, isLoading = false, isOffline = false)
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.type, isOffline = result.type == ErrorType.Network)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun retry() = search(_state.value.query)
}
