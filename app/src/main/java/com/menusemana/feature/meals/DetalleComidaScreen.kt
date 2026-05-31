package com.menusemana.feature.meals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.menusemana.core.designsystem.component.MsChoiceChip
import com.menusemana.core.designsystem.component.MsDangerButton
import com.menusemana.core.designsystem.component.MsPrimaryButton
import com.menusemana.core.designsystem.component.MsTopAppBar
import com.menusemana.core.designsystem.theme.JetBrainsMono
import com.menusemana.core.designsystem.theme.Neutral900
import com.menusemana.core.designsystem.theme.Neutral50
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.core.designsystem.theme.PillShape
import com.menusemana.core.designsystem.theme.SheetShape
import com.menusemana.domain.model.MealSlot

private val DAY_NAMES = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleComidaScreen(
    mealId: Long,
    onNavigateUp: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onDeleted: () -> Unit,
    onNavigateToPlan: () -> Unit,
    viewModel: DetalleComidaViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.addedToPlan) { if (state.addedToPlan) onNavigateToPlan() }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Persimmon500)
        }
        return
    }

    val meal = state.meal ?: run {
        onNavigateUp()
        return
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("¿Borrar '${meal.name}'?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                MsDangerButton("Eliminar", onClick = {
                    viewModel.deleteMeal(context, onDeleted)
                })
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) { Text("Cancelar") }
            },
        )
    }

    if (state.showPlanPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var pickerDay by remember { mutableIntStateOf(0) }
        var pickerSlot by remember { mutableIntStateOf(0) }

        ModalBottomSheet(
            onDismissRequest = viewModel::closePlanPicker,
            sheetState = sheetState,
            shape = SheetShape,
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
                Text(
                    "Elegí el día",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DAY_NAMES.forEachIndexed { index, name ->
                        MsChoiceChip(
                            label = name,
                            selected = pickerDay == index,
                            onClick = { pickerDay = index },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Elegí el turno",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealSlot.entries.forEach { slot ->
                        MsChoiceChip(
                            label = slot.label,
                            selected = pickerSlot == slot.index,
                            onClick = { pickerSlot = slot.index },
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                MsPrimaryButton(
                    text = "Sumar al plan",
                    onClick = { viewModel.assignToPlan(pickerDay, pickerSlot) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    Scaffold(
        topBar = {
            MsTopAppBar(
                title = meal.name,
                onNavigateUp = onNavigateUp,
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = viewModel::showDeleteDialog) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Eliminar", tint = Persimmon500)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                ) {
                    if (meal.photoUri != null) {
                        AsyncImage(
                            model = meal.photoUri,
                            contentDescription = meal.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Restaurant, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Neutral900.copy(alpha = 0.3f), Color.Transparent, Neutral900.copy(alpha = 0.5f)))
                        )
                    )
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                            .clip(PillShape).background(Persimmon500).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(meal.category, style = MaterialTheme.typography.labelSmall, color = Neutral50)
                    }
                    Text(
                        meal.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Neutral50,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MsPrimaryButton("Sumar al plan", onClick = viewModel::openPlanPicker, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("${meal.timeMinutes} min", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono))
                        Text("${meal.servings} porciones", style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono))
                    }
                }
                HorizontalDivider()
            }

            if (meal.ingredients.isNotEmpty()) {
                item {
                    Text("Ingredientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
                }
                itemsIndexed(meal.ingredients) { _, ingredient ->
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(ingredient.quantity, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono), color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(0.35f))
                        Text(ingredient.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.65f))
                    }
                }
                item { HorizontalDivider() }
            }

            if (!meal.notes.isNullOrBlank()) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Preparación", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(meal.notes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
