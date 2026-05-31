package com.menusemana.feature.meals

import androidx.lifecycle.SavedStateHandle
import com.menusemana.domain.model.MealCategory
import com.menusemana.util.FakeMealRepository
import com.menusemana.util.MainDispatcherRule
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditMealViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeMealRepository
    private lateinit var viewModel: AddEditMealViewModel

    @Before
    fun setup() {
        // relaxed mock — toRoute() throws internally, runCatching catches it → mealId = null (add mode)
        val savedStateHandle = mockk<SavedStateHandle>(relaxed = true)
        repository = FakeMealRepository()
        viewModel = AddEditMealViewModel(repository, savedStateHandle)
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has COMIDA as default category`() {
        assertEquals(MealCategory.COMIDA.label, viewModel.state.value.category)
    }

    @Test
    fun `initial state has one blank ingredient row`() {
        assertEquals(1, viewModel.state.value.ingredients.size)
    }

    @Test
    fun `initial state has no errors`() {
        assertFalse(viewModel.state.value.nameError)
        assertFalse(viewModel.state.value.servingsError)
    }

    // ── validation ────────────────────────────────────────────────────────────

    @Test
    fun `save with blank name sets nameError`() {
        viewModel.onNameChange("")
        viewModel.save {}

        assertTrue(viewModel.state.value.nameError)
    }

    @Test
    fun `save with whitespace-only name sets nameError`() {
        viewModel.onNameChange("   ")
        viewModel.save {}

        assertTrue(viewModel.state.value.nameError)
    }

    @Test
    fun `save with servings zero sets servingsError`() {
        viewModel.onNameChange("Milanesa")
        viewModel.onServingsChange("0")
        viewModel.save {}

        assertTrue(viewModel.state.value.servingsError)
    }

    @Test
    fun `save with non-numeric servings sets servingsError`() {
        viewModel.onNameChange("Milanesa")
        viewModel.onServingsChange("abc")
        viewModel.save {}

        assertTrue(viewModel.state.value.servingsError)
    }

    @Test
    fun `valid save calls repository and triggers onSaved`() = runTest {
        viewModel.onNameChange("Milanesa")
        viewModel.onServingsChange("2")

        var savedCalled = false
        viewModel.save { savedCalled = true }

        assertTrue(savedCalled)
        assertEquals(1, repository.savedMeals.size)
        assertEquals("Milanesa", repository.savedMeals.first().name)
    }

    // ── field updates ─────────────────────────────────────────────────────────

    @Test
    fun `onNameChange updates state and clears error`() {
        viewModel.onNameChange("Pasta")

        assertEquals("Pasta", viewModel.state.value.name)
        assertFalse(viewModel.state.value.nameError)
    }

    @Test
    fun `onCategoryChange updates category`() {
        viewModel.onCategoryChange(MealCategory.COLACION.label)

        assertEquals(MealCategory.COLACION.label, viewModel.state.value.category)
    }

    @Test
    fun `addIngredient adds a new blank row`() {
        viewModel.addIngredient()

        assertEquals(2, viewModel.state.value.ingredients.size)
    }

    @Test
    fun `removeIngredient removes the row at given index`() {
        viewModel.addIngredient()
        viewModel.onIngredientNameChange(0, "Pollo")
        viewModel.onIngredientNameChange(1, "Sal")

        viewModel.removeIngredient(0)

        assertEquals(1, viewModel.state.value.ingredients.size)
        assertEquals("Sal", viewModel.state.value.ingredients[0].name)
    }

    @Test
    fun `saved meal includes notes when filled`() = runTest {
        viewModel.onNameChange("Sopa")
        viewModel.onServingsChange("4")
        viewModel.onNotesChange("Hervir 30 minutos")
        viewModel.save {}

        assertEquals("Hervir 30 minutos", repository.savedMeals.first().notes)
    }

    @Test
    fun `saved meal excludes blank ingredient rows`() = runTest {
        viewModel.onNameChange("Arroz")
        viewModel.onServingsChange("2")
        viewModel.onIngredientNameChange(0, "")
        viewModel.save {}

        assertTrue(repository.savedMeals.first().ingredients.isEmpty())
    }
}
