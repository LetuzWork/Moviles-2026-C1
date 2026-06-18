package com.menusemana.repository

import com.menusemana.data.model.Aisle
import com.menusemana.data.model.ShoppingItem
import com.menusemana.data.model.ShoppingSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import kotlin.math.floor

class GenerateShoppingListUseCase
    @Inject
    constructor(
        private val planRepository: PlanRepository,
        private val mealRepository: MealRepository,
    ) {
        operator fun invoke(weekStartEpochDay: Long): Flow<List<ShoppingSection>> =
            combine(
                planRepository.getWeekPlan(weekStartEpochDay),
                mealRepository.getAllMeals(),
            ) { plannedMeals, allMeals ->
                val mealMap = allMeals.associateBy { it.id }
                val allIngredients =
                    plannedMeals
                        .mapNotNull { mealMap[it.meal.id] }
                        .flatMap { it.ingredients }

                val deduped =
                    allIngredients
                        .groupBy { it.name.lowercase().trim() }
                        .map { (_, group) ->
                            val first = group.first()
                            ShoppingItem(
                                name = first.name,
                                quantity = sumQuantities(group.map { it.quantity }),
                                aisle = first.aisle,
                            )
                        }

                val aisleOrder = Aisle.entries.map { it.label }
                deduped
                    .groupBy { it.aisle }
                    .entries
                    .sortedBy { (aisle, _) -> aisleOrder.indexOf(aisle).takeIf { it >= 0 } ?: Int.MAX_VALUE }
                    .map { (aisle, items) -> ShoppingSection(aisle, items) }
            }

        private data class ParsedQty(
            val value: Double,
            val unit: String,
        )

        private fun parseQty(raw: String): ParsedQty? {
            val s = raw.trim()
            if (s.isBlank()) return null
            // Match whole + fraction (e.g. "1 1/2") or fraction (e.g. "1/2") or decimal/int
            val mixedRe = Regex("""^(\d+)\s+(\d+)\s*/\s*(\d+)\s*(.*)$""")
            val fracRe = Regex("""^(\d+)\s*/\s*(\d+)\s*(.*)$""")
            val numRe = Regex("""^(\d+(?:[.,]\d+)?)\s*(.*)$""")
            return when {
                mixedRe.matches(s) -> {
                    val (w, n, d, u) = mixedRe.find(s)!!.destructured
                    ParsedQty(w.toDouble() + n.toDouble() / d.toDouble(), u.trim().lowercase())
                }
                fracRe.matches(s) -> {
                    val (n, d, u) = fracRe.find(s)!!.destructured
                    ParsedQty(n.toDouble() / d.toDouble(), u.trim().lowercase())
                }
                numRe.matches(s) -> {
                    val (n, u) = numRe.find(s)!!.destructured
                    val value = n.replace(',', '.').toDoubleOrNull() ?: return null
                    ParsedQty(value, u.trim().lowercase())
                }
                else -> null
            }
        }

        private fun sumQuantities(quantities: List<String>): String {
            if (quantities.size == 1) return quantities.first()
            val parsed = quantities.map { parseQty(it) }
            if (parsed.all { it != null }) {
                val units = parsed.map { it!!.unit }.distinct()
                if (units.size == 1) {
                    val total = parsed.sumOf { it!!.value }
                    val unit = units.first()
                    val formatted = if (total == floor(total)) total.toLong().toString() else "%.1f".format(total)
                    return if (unit.isBlank()) formatted else "$formatted $unit"
                }
            }
            return quantities.filter { it.isNotBlank() }.joinToString(", ")
        }
    }
