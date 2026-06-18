package com.menusemana.screens.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.menusemana.data.model.ShoppingSection
import com.menusemana.repository.GenerateShoppingListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class ShoppingUiState(
    val sections: List<ShoppingSection> = emptyList(),
    val checkedItems: Set<String> = emptySet(),
    val totalCount: Int = 0,
    val checkedCount: Int = 0,
)

@HiltViewModel
class ShoppingViewModel
    @Inject
    constructor(
        generateShoppingList: GenerateShoppingListUseCase,
    ) : ViewModel() {
        private val weekStart =
            LocalDate
                .now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .toEpochDay()

        private val _checkedItems = MutableStateFlow<Set<String>>(emptySet())

        val state: StateFlow<ShoppingUiState> =
            combine(
                generateShoppingList(weekStart),
                _checkedItems,
            ) { sections, checked ->
                val total = sections.sumOf { it.items.size }
                ShoppingUiState(
                    sections = sections,
                    checkedItems = checked,
                    totalCount = total,
                    checkedCount = checked.size,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

        fun toggleItem(key: String) {
            _checkedItems.update { current ->
                if (key in current) current - key else current + key
            }
        }

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
                    appendLine(section.aisle.uppercase())
                    section.items.forEach { item ->
                        val mark = if (itemKey(section.aisle, item.name) in current.checkedItems) "[x]" else "[ ]"
                        val qty = if (item.quantity.isNotBlank()) " — ${item.quantity}" else ""
                        appendLine("$mark ${item.name}$qty")
                    }
                    appendLine()
                }
            }.trimEnd()
        }
    }
