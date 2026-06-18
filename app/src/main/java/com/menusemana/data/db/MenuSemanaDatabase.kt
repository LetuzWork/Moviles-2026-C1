package com.menusemana.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.menusemana.data.db.dao.MealDao
import com.menusemana.data.db.dao.PlanDao
import com.menusemana.data.db.dao.PreferencesDao
import com.menusemana.data.db.dao.RecipeCacheDao
import com.menusemana.data.db.entity.DietaryPreferenceEntity
import com.menusemana.data.db.entity.IngredientEntity
import com.menusemana.data.db.entity.MealEntity
import com.menusemana.data.db.entity.PlannedMealEntity
import com.menusemana.data.db.entity.RecipeCacheEntity

@Database(
    entities = [
        MealEntity::class,
        IngredientEntity::class,
        PlannedMealEntity::class,
        RecipeCacheEntity::class,
        DietaryPreferenceEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class MenuSemanaDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao

    abstract fun planDao(): PlanDao

    abstract fun recipeCacheDao(): RecipeCacheDao

    abstract fun preferencesDao(): PreferencesDao

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE recipe_cache ADD COLUMN instructionsEs TEXT")
                }
            }
        val MIGRATION_2_3 =
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE recipe_cache ADD COLUMN ingredientsEsJson TEXT")
                }
            }
    }
}
