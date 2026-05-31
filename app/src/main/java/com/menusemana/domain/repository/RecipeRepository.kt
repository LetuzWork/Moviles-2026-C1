package com.menusemana.domain.repository

import com.menusemana.core.common.Result
import com.menusemana.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun search(query: String): Result<List<Recipe>>
    suspend fun getById(mealDbId: String): Result<Recipe>
    fun getCachedRecipes(): Flow<List<Recipe>>
}
