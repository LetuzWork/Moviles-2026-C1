package com.menusemana.data.repository

import com.menusemana.core.common.Result
import com.menusemana.core.database.dao.RecipeCacheDao
import com.menusemana.core.database.entity.RecipeCacheEntity
import com.menusemana.core.network.TheMealDbApi
import com.menusemana.core.network.dto.MealDbItemDto
import com.menusemana.core.network.dto.MealDbResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class RecipeRepositoryImplTest {

    private val api = mockk<TheMealDbApi>()
    private val cacheDao = mockk<RecipeCacheDao>(relaxed = true)

    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setup() {
        repository = RecipeRepositoryImpl(api, cacheDao)
    }

    // ── search() ──────────────────────────────────────────────────────────────

    @Test
    fun `search success returns recipes and caches them`() = runTest {
        val dto = makeDto(id = "1", name = "Pasta Carbonara")
        coEvery { api.searchByName("pasta") } returns MealDbResponseDto(listOf(dto))
        coEvery { cacheDao.getAllCached() } returns flowOf(emptyList())

        val result = repository.search("pasta")

        assertTrue(result is Result.Success)
        val recipes = (result as Result.Success).data
        assertEquals(1, recipes.size)
        assertEquals("Pasta Carbonara", recipes.first().name)
        coVerify { cacheDao.insert(any()) }
    }

    @Test
    fun `search with null meals list returns empty list`() = runTest {
        coEvery { api.searchByName("xyz") } returns MealDbResponseDto(null)

        val result = repository.search("xyz")

        assertTrue(result is Result.Success)
        assertTrue((result as Result.Success).data.isEmpty())
    }

    @Test
    fun `search network failure returns cached results`() = runTest {
        val cached = makeCacheEntity(id = "1", name = "Pasta Carbonara")
        coEvery { api.searchByName("pasta") } throws IOException("no internet")
        coEvery { cacheDao.searchCached("pasta") } returns listOf(cached)

        val result = repository.search("pasta")

        assertTrue(result is Result.Success)
        assertEquals("Pasta Carbonara", (result as Result.Success).data.first().name)
    }

    @Test
    fun `search network failure with empty cache returns Error`() = runTest {
        coEvery { api.searchByName("pasta") } throws IOException("no internet")
        coEvery { cacheDao.searchCached("pasta") } returns emptyList()

        val result = repository.search("pasta")

        assertTrue(result is Result.Error)
    }

    // ── getById() ─────────────────────────────────────────────────────────────

    @Test
    fun `getById success returns recipe`() = runTest {
        val dto = makeDto(id = "52772", name = "Teriyaki Chicken")
        coEvery { api.lookupById("52772") } returns MealDbResponseDto(listOf(dto))

        val result = repository.getById("52772")

        assertTrue(result is Result.Success)
        assertEquals("Teriyaki Chicken", (result as Result.Success).data.name)
        assertEquals("52772", result.data.mealDbId)
    }

    @Test
    fun `getById network failure returns cached recipe`() = runTest {
        val cached = makeCacheEntity(id = "52772", name = "Teriyaki Chicken")
        coEvery { api.lookupById("52772") } throws IOException("no internet")
        coEvery { cacheDao.getById("52772") } returns cached

        val result = repository.getById("52772")

        assertTrue(result is Result.Success)
        assertEquals("Teriyaki Chicken", (result as Result.Success).data.name)
    }

    @Test
    fun `getById network failure with no cache returns Error`() = runTest {
        coEvery { api.lookupById("99999") } throws IOException("no internet")
        coEvery { cacheDao.getById("99999") } returns null

        val result = repository.getById("99999")

        assertTrue(result is Result.Error)
    }

    @Test
    fun `getById with empty meals list returns Error`() = runTest {
        coEvery { api.lookupById("99999") } returns MealDbResponseDto(emptyList())

        val result = repository.getById("99999")

        assertTrue(result is Result.Error)
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeDto(id: String, name: String) = MealDbItemDto(
        id = id, name = name, thumbUrl = null, area = "Italian", category = "Pasta",
        instructions = "Cook pasta.", ingredient1 = "Pasta", measure1 = "200g",
        ingredient2 = null, measure2 = null, ingredient3 = null, measure3 = null,
        ingredient4 = null, measure4 = null, ingredient5 = null, measure5 = null,
        ingredient6 = null, measure6 = null, ingredient7 = null, measure7 = null,
        ingredient8 = null, measure8 = null, ingredient9 = null, measure9 = null,
        ingredient10 = null, measure10 = null, ingredient11 = null, measure11 = null,
        ingredient12 = null, measure12 = null, ingredient13 = null, measure13 = null,
        ingredient14 = null, measure14 = null, ingredient15 = null, measure15 = null,
        ingredient16 = null, measure16 = null, ingredient17 = null, measure17 = null,
        ingredient18 = null, measure18 = null, ingredient19 = null, measure19 = null,
        ingredient20 = null, measure20 = null,
    )

    private fun makeCacheEntity(id: String, name: String) = RecipeCacheEntity(
        mealDbId = id,
        name = name,
        thumbUrl = null,
        area = "Italian",
        category = "Pasta",
        instructions = "Cook pasta.",
        ingredientsJson = "Pasta\t200g",
    )
}
