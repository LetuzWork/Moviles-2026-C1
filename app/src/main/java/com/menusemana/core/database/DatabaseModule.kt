package com.menusemana.core.database

import android.content.Context
import androidx.room.Room
import com.menusemana.core.database.dao.MealDao
import com.menusemana.core.database.dao.PlanDao
import com.menusemana.core.database.dao.RecipeCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MenuSemanaDatabase =
        Room.databaseBuilder(context, MenuSemanaDatabase::class.java, "menusemana.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMealDao(db: MenuSemanaDatabase): MealDao = db.mealDao()

    @Provides
    fun providePlanDao(db: MenuSemanaDatabase): PlanDao = db.planDao()

    @Provides
    fun provideRecipeCacheDao(db: MenuSemanaDatabase): RecipeCacheDao = db.recipeCacheDao()
}
