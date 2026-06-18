package com.menusemana.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.menusemana.data.db.dao.MealDao
import com.menusemana.data.db.dao.PlanDao
import com.menusemana.data.db.dao.PreferencesDao
import com.menusemana.data.db.dao.RecipeCacheDao
import com.menusemana.data.db.entity.IngredientEntity
import com.menusemana.data.db.entity.MealEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        mealDaoProvider: Provider<MealDao>,
    ): MenuSemanaDatabase =
        Room
            .databaseBuilder(context, MenuSemanaDatabase::class.java, "menusemana.db")
            .addMigrations(MenuSemanaDatabase.MIGRATION_1_2, MenuSemanaDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .addCallback(
                object : RoomDatabase.Callback() {
                    // Se ejecuta una sola vez, al crear la base (primera instalación). HU-06.4.
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        val dao = mealDaoProvider.get()
                        CoroutineScope(Dispatchers.IO).launch {
                            SeedData.meals.forEach { meal ->
                                val mealId =
                                    dao.insertMeal(
                                        MealEntity(
                                            name = meal.name,
                                            photoUri = meal.photoUri,
                                            timeMinutes = meal.timeMinutes,
                                            servings = meal.servings,
                                            category = meal.category,
                                            notes = meal.notes,
                                            sourceRecipeId = null,
                                        ),
                                    )
                                dao.insertIngredients(
                                    meal.ingredients.map { ingredient ->
                                        IngredientEntity(
                                            mealId = mealId,
                                            name = ingredient.name,
                                            quantity = ingredient.quantity,
                                            aisle = ingredient.aisle,
                                        )
                                    },
                                )
                            }
                        }
                    }
                },
            ).build()

    @Provides
    fun provideMealDao(db: MenuSemanaDatabase): MealDao = db.mealDao()

    @Provides
    fun providePlanDao(db: MenuSemanaDatabase): PlanDao = db.planDao()

    @Provides
    fun provideRecipeCacheDao(db: MenuSemanaDatabase): RecipeCacheDao = db.recipeCacheDao()

    @Provides
    fun providePreferencesDao(db: MenuSemanaDatabase): PreferencesDao = db.preferencesDao()
}
