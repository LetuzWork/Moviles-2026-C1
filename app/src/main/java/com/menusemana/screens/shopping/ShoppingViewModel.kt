package com.menusemana.screens.shopping

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.data.model.ShoppingSection
import com.menusemana.repository.GenerateShoppingListUseCase
import com.menusemana.repository.WeekStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ShoppingUiState(
    val sections: List<ShoppingSection> = emptyList(),
    val checkedItems: Set<String> = emptySet(),
    val warningItems: Set<String> = emptySet(),
    val weekStart: LocalDate = LocalDate.now(),
    val isCurrentWeek: Boolean = true,
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShoppingViewModel
    @Inject
    constructor(
        private val generateShoppingList: GenerateShoppingListUseCase,
        private val weekStateHolder: WeekStateHolder,
        private val dataStore: DataStore<Preferences>,
    ) : ViewModel() {
        private val _checkedQuantities = MutableStateFlow<Map<String, String>>(emptyMap())

        private val _weekData =
            weekStateHolder.weekOffset.flatMapLatest { offset ->
                val epochDay = weekStateHolder.weekStartAt(offset).toEpochDay()
                generateShoppingList(epochDay).map { offset to it }
            }

        val state: StateFlow<ShoppingUiState> =
            combine(_weekData, _checkedQuantities) { (offset, sections), checkedQtys ->
                val weekStart = weekStateHolder.weekStartAt(offset)
                val checkedItems = mutableSetOf<String>()
                val warningItems = mutableSetOf<String>()

                for ((key, qtyWhenChecked) in checkedQtys) {
                    val parts = key.split("|", limit = 2)
                    if (parts.size < 2) continue
                    val (aisle, name) = parts
                    val currentQty =
                        sections
                            .find { it.aisle == aisle }
                            ?.items
                            ?.find { it.name == name }
                            ?.quantity
                    when {
                        currentQty == null || currentQty == qtyWhenChecked -> checkedItems.add(key)
                        else -> warningItems.add(key)
                    }
                }

                val total = sections.sumOf { it.items.size }
                ShoppingUiState(
                    sections = sections,
                    checkedItems = checkedItems,
                    warningItems = warningItems,
                    weekStart = weekStart,
                    isCurrentWeek = weekStart == weekStateHolder.currentWeekMonday,
                    totalCount = total,
                    checkedCount = checkedItems.size,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

        init {
            viewModelScope.launch {
                weekStateHolder.weekOffset.collect { offset ->
                    val epochDay = weekStateHolder.weekStartAt(offset).toEpochDay()
                    val prefs = dataStore.data.firstOrNull()
                    _checkedQuantities.value = deserialize(prefs?.get(weekKey(epochDay)) ?: "")
                }
            }
        }

        fun toggleItem(
            key: String,
            quantity: String,
        ) {
            _checkedQuantities.update { current ->
                if (key in current) current - key else current + (key to quantity)
            }
            val epochDay = weekStateHolder.weekStartAt(weekStateHolder.weekOffset.value).toEpochDay()
            viewModelScope.launch {
                dataStore.edit { prefs ->
                    prefs[weekKey(epochDay)] = serialize(_checkedQuantities.value)
                }
            }
        }

        fun previousWeek() = weekStateHolder.previousWeek()

        fun nextWeek() = weekStateHolder.nextWeek()

        fun goToCurrentWeek() = weekStateHolder.goToCurrentWeek()

        fun itemKey(
            aisle: String,
            name: String,
        ) = "$aisle|$name"

        fun buildShareText(): String {
            val current = state.value
            if (current.sections.isEmpty()) return ""
            return buildString {
                appendLine("🛒 Lista de compras — MenúSemana")
                appendLine()
                current.sections.forEach { section ->
                    val pendingItems =
                        section.items.filter { item ->
                            itemKey(section.aisle, item.name) !in current.checkedItems
                        }
                    if (pendingItems.isEmpty()) return@forEach
                    appendLine(section.aisle.uppercase())
                    pendingItems.forEach { item ->
                        val key = itemKey(section.aisle, item.name)
                        val qty = if (item.quantity.isNotBlank()) " — ${item.quantity}" else ""
                        val warn = if (key in current.warningItems) " ⚠" else ""
                        appendLine("[ ] ${item.name}$qty$warn")
                    }
                    appendLine()
                }
            }.trimEnd()
        }

        private fun weekKey(epochDay: Long) = stringPreferencesKey("checked_$epochDay")

        private fun serialize(map: Map<String, String>): String = map.entries.joinToString("\n") { (k, v) -> "$k=$v" }

        private fun deserialize(raw: String): Map<String, String> =
            if (raw.isBlank()) {
                emptyMap()
            } else {
                raw
                    .lines()
                    .filter { "=" in it }
                    .associate { line ->
                        val idx = line.indexOf("=")
                        line.substring(0, idx) to line.substring(idx + 1)
                    }
            }
    }
