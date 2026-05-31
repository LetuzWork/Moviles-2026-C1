package com.menusemana.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val photoUri: String?,
    val timeMinutes: Int,
    val servings: Int,
    val category: String,
    val notes: String?,
    val sourceRecipeId: String?,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [ForeignKey(
        entity = MealEntity::class,
        parentColumns = ["id"],
        childColumns = ["mealId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("mealId")],
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val name: String,
    val quantity: String,
    val aisle: String,
)

@Entity(
    tableName = "planned_meals",
    indices = [Index(value = ["weekStartEpochDay", "dayOfWeek", "slot"], unique = true)],
)
data class PlannedMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartEpochDay: Long,
    val dayOfWeek: Int,
    val slot: Int,
    val mealId: Long,
)

@Entity(tableName = "recipe_cache")
data class RecipeCacheEntity(
    @PrimaryKey val mealDbId: String,
    val name: String,
    val thumbUrl: String?,
    val area: String?,
    val category: String?,
    val instructions: String?,
    val ingredientsJson: String,
    val cachedAt: Long = System.currentTimeMillis(),
)
