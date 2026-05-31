package com.menusemana.data.repository

import com.menusemana.core.database.dao.MealDao
import com.menusemana.core.database.dao.MealWithIngredients
import com.menusemana.core.database.entity.IngredientEntity
import com.menusemana.core.database.entity.MealEntity
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao,
) : MealRepository {

    override fun getAllMeals(): Flow<List<Meal>> =
        mealDao.getAllMealsWithIngredients().map { list -> list.map { it.toDomain() } }

    override suspend fun getMealById(id: Long): Meal? =
        mealDao.getMealWithIngredientsById(id)?.toDomain()

    override suspend fun saveMeal(meal: Meal): Long {
        val entity = meal.toEntity()
        val id = mealDao.insertMeal(entity)
        val ingredients = meal.ingredients.map { it.toEntity(id) }
        mealDao.insertIngredients(ingredients)
        return id
    }

    override suspend fun updateMeal(meal: Meal) {
        mealDao.updateMeal(meal.toEntity())
        mealDao.deleteIngredientsByMealId(meal.id)
        mealDao.insertIngredients(meal.ingredients.map { it.toEntity(meal.id) })
    }

    override suspend fun deleteMeal(meal: Meal) {
        mealDao.deleteMeal(meal.toEntity())
    }

    private fun MealWithIngredients.toDomain() = Meal(
        id = meal.id,
        name = meal.name,
        photoUri = meal.photoUri,
        timeMinutes = meal.timeMinutes,
        servings = meal.servings,
        category = meal.category,
        notes = meal.notes,
        ingredients = ingredients.map { it.toDomain() },
        sourceRecipeId = meal.sourceRecipeId,
    )

    private fun IngredientEntity.toDomain() = Ingredient(
        id = id,
        name = name,
        quantity = quantity,
        aisle = aisle,
    )

    private fun Meal.toEntity() = MealEntity(
        id = id,
        name = name,
        photoUri = photoUri,
        timeMinutes = timeMinutes,
        servings = servings,
        category = category,
        notes = notes,
        sourceRecipeId = sourceRecipeId,
    )

    private fun Ingredient.toEntity(mealId: Long) = IngredientEntity(
        id = id,
        mealId = mealId,
        name = name,
        quantity = quantity,
        aisle = aisle,
    )
}
