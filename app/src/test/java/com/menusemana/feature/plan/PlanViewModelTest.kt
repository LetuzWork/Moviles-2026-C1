package com.menusemana.feature.plan

import app.cash.turbine.test
import com.menusemana.domain.usecase.AssignMealToSlotUseCase
import com.menusemana.util.FakeMealRepository
import com.menusemana.util.FakePlanRepository
import com.menusemana.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var planRepo: FakePlanRepository
    private lateinit var mealRepo: FakeMealRepository
    private lateinit var viewModel: PlanViewModel

    private val currentMonday = LocalDate.now()
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    @Before
    fun setup() {
        planRepo = FakePlanRepository()
        mealRepo = FakeMealRepository()
        viewModel = PlanViewModel(planRepo, mealRepo, AssignMealToSlotUseCase(planRepo))
    }

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial weekStart is current Monday`() {
        assertEquals(currentMonday, viewModel.state.value.weekStart)
    }

    @Test
    fun `isCurrentWeek is true on start`() {
        assertTrue(viewModel.state.value.isCurrentWeek)
    }

    // ── week navigation ───────────────────────────────────────────────────────

    @Test
    fun `previousWeek decrements weekStart by 7 days`() = runTest {
        viewModel.previousWeek()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(currentMonday.minusWeeks(1), state.weekStart)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextWeek increments weekStart by 7 days`() = runTest {
        viewModel.nextWeek()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(currentMonday.plusWeeks(1), state.weekStart)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `goToCurrentWeek resets to current Monday`() = runTest {
        viewModel.previousWeek()
        viewModel.previousWeek()
        viewModel.goToCurrentWeek()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(currentMonday, state.weekStart)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isCurrentWeek is false after previousWeek`() = runTest {
        viewModel.previousWeek()

        viewModel.state.test {
            assertFalse(awaitItem().isCurrentWeek)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isCurrentWeek is false after nextWeek`() = runTest {
        viewModel.nextWeek()

        viewModel.state.test {
            assertFalse(awaitItem().isCurrentWeek)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isCurrentWeek becomes true again after goToCurrentWeek`() = runTest {
        viewModel.nextWeek()
        viewModel.goToCurrentWeek()

        viewModel.state.test {
            assertTrue(awaitItem().isCurrentWeek)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── meal picker ───────────────────────────────────────────────────────────

    @Test
    fun `openMealPicker sets showMealPicker to true with correct day and slot`() = runTest {
        viewModel.state.test {
            awaitItem() // consume initial emission to activate the stateIn collection
            viewModel.openMealPicker(day = 2, slot = 1)
            val state = awaitItem()
            assertTrue(state.showMealPicker)
            assertEquals(2, state.pickerDay)
            assertEquals(1, state.pickerSlot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `closeMealPicker clears showMealPicker and selection`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.openMealPicker(day = 2, slot = 1)
            awaitItem()
            viewModel.closeMealPicker()
            val state = awaitItem()
            assertFalse(state.showMealPicker)
            assertNull(state.pickerDay)
            assertNull(state.pickerSlot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `assignMealToSlot delegates to use case and closes picker`() = runTest {
        viewModel.state.test {
            awaitItem()
            viewModel.openMealPicker(day = 3, slot = 2)
            awaitItem()
            viewModel.assignMealToSlot(mealId = 42L)
            val state = awaitItem()

            assertFalse(state.showMealPicker)
            assertEquals(1, planRepo.assignedMeals.size)
            val (mealId, day, slot) = planRepo.assignedMeals.first()
            assertEquals(42L, mealId)
            assertEquals(3, day)
            assertEquals(2, slot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `assignMealToSlot does nothing if picker was not opened`() = runTest {
        viewModel.assignMealToSlot(mealId = 1L)

        assertTrue(planRepo.assignedMeals.isEmpty())
    }

    @Test
    fun `clearSlot delegates to repository`() = runTest {
        viewModel.clearSlot(day = 1, slot = 0)

        assertEquals(1, planRepo.clearedSlots.size)
        assertEquals(Pair(1, 0), planRepo.clearedSlots.first())
    }
}
