package com.menusemana.repository

import com.menusemana.data.model.Recipe
import com.menusemana.data.util.Result
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun search(query: String): Result<List<Recipe>>

    suspend fun searchByCategory(category: String): Result<List<Recipe>>

    suspend fun getById(mealDbId: String): Result<Recipe>

    fun getCachedRecipes(): Flow<List<Recipe>>

    suspend fun saveTranslation(
        mealDbId: String,
        translatedInstructions: String,
    )

    suspend fun saveIngredientTranslations(
        mealDbId: String,
        translatedPairs: List<Pair<String, String>>,
    )
}
