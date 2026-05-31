package com.menusemana.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.menusemana.core.database.entity.IngredientEntity
import com.menusemana.core.database.entity.MealEntity
import kotlinx.coroutines.flow.Flow

data class MealWithIngredients(
    @Embedded val meal: MealEntity,
    @Relation(parentColumn = "id", entityColumn = "mealId")
    val ingredients: List<IngredientEntity>,
)

@Dao
interface MealDao {

    @Transaction
    @Query("SELECT * FROM meals ORDER BY createdAt DESC")
    fun getAllMealsWithIngredients(): Flow<List<MealWithIngredients>>

    @Transaction
    @Query("SELECT * FROM meals WHERE id = :id")
    suspend fun getMealWithIngredientsById(id: Long): MealWithIngredients?

    @Query("SELECT * FROM meals WHERE name LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchMeals(query: String): Flow<List<MealEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE mealId = :mealId")
    suspend fun deleteIngredientsByMealId(mealId: Long)
}
