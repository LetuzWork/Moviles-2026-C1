package com.menusemana.data.repository

import com.menusemana.core.common.ErrorType
import com.menusemana.core.common.Result
import com.menusemana.core.database.dao.RecipeCacheDao
import com.menusemana.core.database.entity.RecipeCacheEntity
import com.menusemana.core.network.TheMealDbApi
import com.menusemana.core.network.dto.MealDbItemDto
import com.menusemana.domain.model.Recipe
import com.menusemana.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val api: TheMealDbApi,
    private val cacheDao: RecipeCacheDao,
) : RecipeRepository {

    override suspend fun search(query: String): Result<List<Recipe>> =
        runCatching { api.searchByName(query) }
            .fold(
                onSuccess = { response ->
                    val recipes = response.meals?.map { it.toRecipe() } ?: emptyList()
                    recipes.forEach { mergeAndCache(it) }
                    Result.Success(recipes)
                },
                onFailure = { throwable ->
                    val cached = cacheDao.searchCached(query).map { it.toDomain() }
                    if (cached.isNotEmpty()) Result.Success(cached)
                    else Result.Error(
                        if (throwable is IOException) ErrorType.Network else ErrorType.Unknown,
                        throwable,
                    )
                },
            )

    override suspend fun searchByCategory(category: String): Result<List<Recipe>> =
        runCatching { api.filterByCategory(category) }
            .fold(
                onSuccess = { response ->
                    val recipes = response.meals?.map { it.toRecipe().copy(category = category) } ?: emptyList()
                    Result.Success(recipes)
                },
                onFailure = { throwable ->
                    Result.Error(
                        if (throwable is IOException) ErrorType.Network else ErrorType.Unknown,
                        throwable,
                    )
                },
            )

    override suspend fun getById(mealDbId: String): Result<Recipe> =
        runCatching { api.lookupById(mealDbId) }
            .fold(
                onSuccess = { response ->
                    val recipe = response.meals?.firstOrNull()?.toRecipe()
                        ?: return Result.Error(ErrorType.NotFound)
                    Result.Success(mergeAndCache(recipe))
                },
                onFailure = { throwable ->
                    val cached = cacheDao.getById(mealDbId)?.toDomain()
                    if (cached != null) Result.Success(cached)
                    else Result.Error(
                        if (throwable is IOException) ErrorType.Network else ErrorType.Unknown,
                        throwable,
                    )
                },
            )

    override fun getCachedRecipes(): Flow<List<Recipe>> =
        cacheDao.getAllCached().map { list -> list.map { it.toDomain() } }

    override suspend fun saveTranslation(mealDbId: String, translatedInstructions: String) {
        cacheDao.saveTranslation(mealDbId, translatedInstructions)
    }

    override suspend fun saveIngredientTranslations(
        mealDbId: String,
        translatedPairs: List<Pair<String, String>>,
    ) {
        val json = translatedPairs.joinToString("\n") { (n, m) -> "$n\t$m" }
        cacheDao.saveIngredientTranslations(mealDbId, json)
    }

    private suspend fun mergeAndCache(recipe: Recipe): Recipe {
        val existing = cacheDao.getById(recipe.mealDbId)
        val merged = if (existing != null) recipe.copy(
            instructionsEs = existing.instructionsEs,
            ingredientsEs = existing.ingredientsEsJson?.let { parseIngredientJson(it) },
        ) else recipe
        cacheDao.insert(merged.toEntity())
        return merged
    }

    private fun MealDbItemDto.toRecipe() = Recipe(
        mealDbId = id,
        name = name,
        thumbUrl = thumbUrl,
        area = area,
        category = category,
        instructions = instructions,
        ingredients = getIngredients(),
    )

    private fun Recipe.toEntity() = RecipeCacheEntity(
        mealDbId = mealDbId,
        name = name,
        thumbUrl = thumbUrl,
        area = area,
        category = category,
        instructions = instructions,
        ingredientsJson = ingredients.joinToString("\n") { (n, m) -> "$n\t$m" },
        instructionsEs = instructionsEs,
        ingredientsEsJson = ingredientsEs
            ?.joinToString("\n") { (n, m) -> "$n\t$m" },
    )

    private fun RecipeCacheEntity.toDomain() = Recipe(
        mealDbId = mealDbId,
        name = name,
        thumbUrl = thumbUrl,
        area = area,
        category = category,
        instructions = instructions,
        instructionsEs = instructionsEs,
        ingredients = parseIngredientJson(ingredientsJson),
        ingredientsEs = ingredientsEsJson?.let { parseIngredientJson(it) },
    )

    private fun parseIngredientJson(json: String): List<Pair<String, String>> =
        json.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split("\t", limit = 2)
                (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
            }
}
