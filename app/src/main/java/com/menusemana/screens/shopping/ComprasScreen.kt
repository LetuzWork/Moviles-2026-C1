package com.menusemana.screens.shopping

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.screens.ui.component.MsEmptyState
import com.menusemana.screens.ui.component.MsLargeHeader
import com.menusemana.screens.ui.theme.Herb500
import com.menusemana.screens.ui.theme.JetBrainsMono
import com.menusemana.screens.ui.theme.Persimmon500
import com.menusemana.screens.ui.theme.Warning500
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

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
fun ComprasScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            MsLargeHeader(
                kicker = weekLabel(state.weekStart),
                title = "Compras",
                actions = {
                    if (!state.isCurrentWeek) {
                        TextButton(onClick = viewModel::goToCurrentWeek) {
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
                    if (state.sections.isNotEmpty()) {
                        IconButton(onClick = {
                            val text = viewModel.buildShareText()
                            if (text.isNotBlank()) {
                                val sendIntent =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Lista de compras — MenúSemana")
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    }
                                context.startActivity(Intent.createChooser(sendIntent, "Compartir lista"))
                            }
                        }) {
                            Icon(imageVector = Icons.Rounded.Share, contentDescription = "Compartir lista")
                        }
                    }
                },
            )
        }

        if (state.sections.isEmpty()) {
            MsEmptyState(
                title = "La lista está vacía",
                body = "Planificá tu semana para generar la lista automáticamente",
                modifier = Modifier.fillMaxSize(),
            )
            return@Column
        }

        if (state.totalCount > 0) {
            Card(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Herb500.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progreso", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${state.checkedCount} de ${state.totalCount}",
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = JetBrainsMono),
                            color = Herb500,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (state.totalCount > 0) state.checkedCount.toFloat() / state.totalCount else 0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Herb500,
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            state.sections.forEach { section ->
                item {
                    Text(
                        section.aisle,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                items(section.items, key = { "${section.aisle}|${it.name}" }) { item ->
                    val key = viewModel.itemKey(section.aisle, item.name)
                    val checked = key in state.checkedItems
                    val isWarning = key in state.warningItems
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleItem(key, item.quantity) }
                                .alpha(if (checked) 0.55f else 1f)
                                .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = Herb500),
                        )
                        Text(
                            text = item.name,
                            style =
                                MaterialTheme.typography.bodyMedium.let {
                                    if (checked) it.copy(textDecoration = TextDecoration.LineThrough) else it
                                },
                            modifier = Modifier.weight(1f),
                        )
                        if (isWarning) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = {
                                    PlainTooltip {
                                        Text("Ya se había comprado, pero la cantidad aumentó")
                                    }
                                },
                                state = rememberTooltipState(),
                                enableUserInput = false,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "Cantidad aumentó",
                                    tint = Warning500,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Text(
                            text = item.quantity,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color = if (isWarning) Warning500 else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = if (isWarning) 4.dp else 0.dp),
                        )
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            }
        }
    }
}
