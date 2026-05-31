package com.menusemana.feature.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.core.designsystem.component.MsEmptyState
import com.menusemana.core.designsystem.component.MsLargeHeader
import com.menusemana.core.designsystem.theme.Herb500
import com.menusemana.core.designsystem.theme.JetBrainsMono

@Composable
fun ComprasScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ShoppingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            MsLargeHeader(kicker = "Esta semana", title = "Compras")
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (checked) 0.55f else 1f)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { viewModel.toggleItem(key) },
                            colors = CheckboxDefaults.colors(checkedColor = Herb500),
                        )
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium.let {
                                if (checked) it.copy(textDecoration = TextDecoration.LineThrough) else it
                            },
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = item.quantity,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            }
        }
    }
}
