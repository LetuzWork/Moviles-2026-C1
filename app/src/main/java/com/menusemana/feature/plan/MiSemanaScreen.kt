package com.menusemana.feature.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.core.designsystem.component.MsEmptyState
import com.menusemana.core.designsystem.component.MsLargeHeader
import com.menusemana.core.designsystem.theme.Neutral300
import com.menusemana.core.designsystem.theme.Neutral50
import com.menusemana.core.designsystem.theme.Persimmon100
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.core.designsystem.theme.SheetShape
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.MealSlot
import com.menusemana.domain.model.PlannedMeal
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private val DAY_NAMES = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
private val SLOTS = MealSlot.entries

private fun weekLabel(weekStart: LocalDate): String {
    val weekEnd = weekStart.plusDays(6)
    val locale = Locale("es")
    val startMonth = weekStart.month.getDisplayName(TextStyle.SHORT, locale)
    val endMonth = weekEnd.month.getDisplayName(TextStyle.SHORT, locale)
    return if (weekStart.month == weekEnd.month) {
        "${weekStart.dayOfMonth}–${weekEnd.dayOfMonth} $startMonth ${weekStart.year}"
    } else {
        "${weekStart.dayOfMonth} $startMonth – ${weekEnd.dayOfMonth} $endMonth ${weekStart.year}"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiSemanaScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToMealDetail: (Long) -> Unit,
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    var selectedDay by remember { mutableIntStateOf(today.dayOfWeek.value - 1) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            MsLargeHeader(
                kicker = weekLabel(state.weekStart),
                title = "Mi semana",
                actions = {
                    if (!state.isCurrentWeek) {
                        TextButton(
                            onClick = {
                                viewModel.goToCurrentWeek()
                                selectedDay = today.dayOfWeek.value - 1
                            },
                        ) {
                            Text(
                                "Hoy",
                                style = MaterialTheme.typography.labelMedium,
                                color = Persimmon500,
                            )
                        }
                    }
                    IconButton(onClick = viewModel::previousWeek) {
                        Icon(
                            Icons.Rounded.ChevronLeft,
                            contentDescription = "Semana anterior",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(onClick = viewModel::nextWeek) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = "Semana siguiente",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(7) { index ->
                    val date = state.weekStart.plusDays(index.toLong())
                    val isSelected = index == selectedDay
                    val isToday = date == today
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> Persimmon500
                                    isToday -> Persimmon100
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { selectedDay = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                DAY_NAMES[index],
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Neutral50 else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) Neutral50 else MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        val dayMeals = state.plannedMeals.filter { it.dayOfWeek == selectedDay }

        if (state.allMeals.isEmpty() && dayMeals.isEmpty()) {
            MsEmptyState(
                title = "La semana está vacía",
                body = "Primero sumá comidas en \"Mis comidas\" y luego asignales un turno",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(SLOTS) { slot ->
                    val planned = dayMeals.find { it.slot == slot.index }
                    SlotRow(
                        slotLabel = slot.label,
                        plannedMeal = planned,
                        onTapEmpty = { viewModel.openMealPicker(selectedDay, slot.index) },
                        onTapFilled = { planned?.meal?.id?.let(onNavigateToMealDetail) },
                    )
                }
            }
        }
    }

    if (state.showMealPicker) {
        ModalBottomSheet(
            onDismissRequest = viewModel::closeMealPicker,
            sheetState = sheetState,
            shape = SheetShape,
        ) {
            Text(
                "Elegí una comida",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                items(state.allMeals, key = { it.id }) { meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.assignMealToSlot(meal.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = Persimmon500)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meal.name, style = MaterialTheme.typography.titleSmall)
                            Text(meal.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotRow(
    slotLabel: String,
    plannedMeal: PlannedMeal?,
    onTapEmpty: () -> Unit,
    onTapFilled: () -> Unit,
) {
    Column {
        Text(
            slotLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        if (plannedMeal != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onTapFilled)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = Persimmon500, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(plannedMeal.meal.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${plannedMeal.meal.timeMinutes} min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(BorderStroke(1.5.dp, Neutral300), MaterialTheme.shapes.medium)
                    .clickable(onClick = onTapEmpty),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Neutral300)
                    Spacer(Modifier.width(4.dp))
                    Text("Sumar comida", style = MaterialTheme.typography.labelMedium, color = Neutral300)
                }
            }
        }
    }
}
