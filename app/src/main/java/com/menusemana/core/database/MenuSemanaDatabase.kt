package com.menusemana.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.menusemana.core.database.dao.MealDao
import com.menusemana.core.database.dao.PlanDao
import com.menusemana.core.database.dao.RecipeCacheDao
import com.menusemana.core.database.entity.IngredientEntity
import com.menusemana.core.database.entity.MealEntity
import com.menusemana.core.database.entity.PlannedMealEntity
import com.menusemana.core.database.entity.RecipeCacheEntity

@Database(
    entities = [
        MealEntity::class,
        IngredientEntity::class,
        PlannedMealEntity::class,
        RecipeCacheEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MenuSemanaDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun planDao(): PlanDao
    abstract fun recipeCacheDao(): RecipeCacheDao
}
