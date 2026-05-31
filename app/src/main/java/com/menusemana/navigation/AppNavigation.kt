package com.menusemana.navigation

import kotlinx.serialization.Serializable

@Serializable data object Onboarding
@Serializable data object Plan
@Serializable data object Meals
@Serializable data object Recipes
@Serializable data object Shopping
@Serializable data class MealDetail(val mealId: Long)
@Serializable data object AddMeal
@Serializable data class EditMeal(val mealId: Long)
@Serializable data class RecipeDetail(val mealDbId: String)
