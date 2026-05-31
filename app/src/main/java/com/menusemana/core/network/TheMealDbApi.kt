package com.menusemana.core.network

import com.menusemana.core.network.dto.CategoriesResponseDto
import com.menusemana.core.network.dto.MealDbResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TheMealDbApi {
    @GET("search.php")
    suspend fun searchByName(@Query("s") name: String): MealDbResponseDto

    @GET("lookup.php")
    suspend fun lookupById(@Query("i") id: String): MealDbResponseDto

    @GET("categories.php")
    suspend fun categories(): CategoriesResponseDto

    @GET("filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealDbResponseDto
}
