package com.menusemana.domain.usecase

import app.cash.turbine.test
import com.menusemana.domain.model.Aisle
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.MealCategory
import com.menusemana.domain.model.PlannedMeal
import com.menusemana.util.FakeMealRepository
import com.menusemana.util.FakePlanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GenerateShoppingListUseCaseTest {

    private lateinit var planRepo: FakePlanRepository
    private lateinit var mealRepo: FakeMealRepository
    private lateinit var useCase: GenerateShoppingListUseCase

    private val weekStart = LocalDate.now().toEpochDay()

    @Before
    fun setup() {
        planRepo = FakePlanRepository()
        mealRepo = FakeMealRepository()
        useCase = GenerateShoppingListUseCase(planRepo, mealRepo)
    }

    @Test
    fun `empty plan produces empty shopping list`() = runTest {
        planRepo.setPlan(emptyList())
        mealRepo.setMeals(emptyList())

        useCase(weekStart).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `planned meal with ingredients produces correct section`() = runTest {
        val meal = makeMeal(
            id = 1L,
            ingredients = listOf(
                Ingredient(name = "Pollo", quantity = "500 g", aisle = Aisle.CARNICERIA.label),
                Ingredient(name = "Sal", quantity = "1 cdta.", aisle = Aisle.ALMACEN.label),
            ),
        )
        mealRepo.setMeals(listOf(meal))
        planRepo.setPlan(listOf(makePlannedMeal(meal, day = 0, slot = 0)))

        useCase(weekStart).test {
            val sections = awaitItem()
            assertEquals(2, sections.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `same ingredient in two meals has quantities joined`() = runTest {
        val ing = Ingredient(name = "Ajo", quantity = "2 dientes", aisle = Aisle.VERDULERIA.label)
        val meal1 = makeMeal(id = 1L, ingredients = listOf(ing))
        val meal2 = makeMeal(id = 2L, ingredients = listOf(ing.copy(quantity = "3 dientes")))
        mealRepo.setMeals(listOf(meal1, meal2))
        planRepo.setPlan(listOf(
            makePlannedMeal(meal1, day = 0, slot = 0),
            makePlannedMeal(meal2, day = 1, slot = 0),
        ))

        useCase(weekStart).test {
            val sections = awaitItem()
            val verduleria = sections.first { it.aisle == Aisle.VERDULERIA.label }
            val ajoItem = verduleria.items.first { it.name == "Ajo" }
            assertTrue(ajoItem.quantity.contains("2 dientes"))
            assertTrue(ajoItem.quantity.contains("3 dientes"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ingredients are grouped by aisle`() = runTest {
        val meal = makeMeal(
            id = 1L,
            ingredients = listOf(
                Ingredient(name = "Pollo", quantity = "400 g", aisle = Aisle.CARNICERIA.label),
                Ingredient(name = "Tomate", quantity = "2", aisle = Aisle.VERDULERIA.label),
                Ingredient(name = "Leche", quantity = "200 ml", aisle = Aisle.LACTEOS.label),
            ),
        )
        mealRepo.setMeals(listOf(meal))
        planRepo.setPlan(listOf(makePlannedMeal(meal, day = 0, slot = 0)))

        useCase(weekStart).test {
            val sections = awaitItem()
            val aisles = sections.map { it.aisle }
            assertTrue(aisles.contains(Aisle.CARNICERIA.label))
            assertTrue(aisles.contains(Aisle.VERDULERIA.label))
            assertTrue(aisles.contains(Aisle.LACTEOS.label))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `meal not in plan does not appear in shopping list`() = runTest {
        val planned = makeMeal(
            id = 1L,
            ingredients = listOf(Ingredient(name = "Arroz", quantity = "200 g", aisle = Aisle.ALMACEN.label)),
        )
        val notPlanned = makeMeal(
            id = 2L,
            ingredients = listOf(Ingredient(name = "Pasta", quantity = "300 g", aisle = Aisle.ALMACEN.label)),
        )
        mealRepo.setMeals(listOf(planned, notPlanned))
        planRepo.setPlan(listOf(makePlannedMeal(planned, day = 0, slot = 0)))

        useCase(weekStart).test {
            val sections = awaitItem()
            val almacen = sections.first { it.aisle == Aisle.ALMACEN.label }
            val names = almacen.items.map { it.name }
            assertTrue(names.contains("Arroz"))
            assertTrue(!names.contains("Pasta"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `meal with no ingredients produces empty list`() = runTest {
        val meal = makeMeal(id = 1L, ingredients = emptyList())
        mealRepo.setMeals(listOf(meal))
        planRepo.setPlan(listOf(makePlannedMeal(meal, day = 0, slot = 0)))

        useCase(weekStart).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeMeal(id: Long, ingredients: List<Ingredient>) = Meal(
        id = id,
        name = "Comida $id",
        category = MealCategory.COMIDA.label,
        ingredients = ingredients,
    )

    private fun makePlannedMeal(meal: Meal, day: Int, slot: Int) = PlannedMeal(
        id = meal.id,
        weekStartEpochDay = weekStart,
        dayOfWeek = day,
        slot = slot,
        meal = meal,
    )
}
