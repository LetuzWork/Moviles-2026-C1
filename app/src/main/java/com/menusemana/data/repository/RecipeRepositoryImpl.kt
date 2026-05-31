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
                    recipes.forEach { cacheRecipe(it) }
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

    override suspend fun getById(mealDbId: String): Result<Recipe> =
        runCatching { api.lookupById(mealDbId) }
            .fold(
                onSuccess = { response ->
                    val recipe = response.meals?.firstOrNull()?.toRecipe()
                        ?: return Result.Error(ErrorType.NotFound)
                    cacheRecipe(recipe)
                    Result.Success(recipe)
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

    private suspend fun cacheRecipe(recipe: Recipe) {
        cacheDao.insert(recipe.toEntity())
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
    )

    private fun RecipeCacheEntity.toDomain() = Recipe(
        mealDbId = mealDbId,
        name = name,
        thumbUrl = thumbUrl,
        area = area,
        category = category,
        instructions = instructions,
        ingredients = ingredientsJson.lines()
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split("\t", limit = 2)
                (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
            },
    )
}
