package com.menusemana.feature.recipes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.menusemana.core.designsystem.component.MsPrimaryButton
import com.menusemana.core.designsystem.component.MsTopAppBar
import com.menusemana.core.designsystem.theme.JetBrainsMono
import com.menusemana.core.designsystem.theme.Neutral50
import com.menusemana.core.designsystem.theme.Neutral900
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.core.designsystem.theme.PillShape

@Composable
fun RecipeDetailScreen(
    mealDbId: String,
    onNavigateUp: () -> Unit,
    onImported: (recipeName: String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(mealDbId) { viewModel.load(mealDbId) }
    LaunchedEffect(state.imported) { if (state.imported) onImported(state.recipe?.name.orEmpty()) }

    val recipe = state.recipe

    if (state.isLoading || recipe == null) {
        Scaffold(topBar = { MsTopAppBar(title = "", onNavigateUp = onNavigateUp) }) { pad ->
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Persimmon500)
            }
        }
        return
    }

    Scaffold(
        topBar = { MsTopAppBar(title = recipe.nameEs ?: recipe.name, onNavigateUp = onNavigateUp) },
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                    AsyncImage(
                        model = recipe.thumbUrl,
                        contentDescription = recipe.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier =
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Transparent, Neutral900.copy(alpha = 0.5f))),
                            ),
                    )
                    recipe.category?.let { cat ->
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .clip(PillShape)
                                    .background(Persimmon500)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) { Text(state.translatedCategory ?: cat, style = MaterialTheme.typography.labelSmall, color = Neutral50) }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(recipe.nameEs ?: recipe.name, style = MaterialTheme.typography.headlineSmall)
                    state.translatedArea?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    MsPrimaryButton("Agregar a mis comidas", onClick = { viewModel.importToMyMeals() }, modifier = Modifier.fillMaxWidth())
                }
                HorizontalDivider()
            }

            val resolvedIngredients = state.resolvedIngredients
            if (resolvedIngredients.isNotEmpty()) {
                item { Text("Ingredientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp)) }
                itemsIndexed(resolvedIngredients) { _, (name, measure) ->
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(
                            measure.ifBlank { "c/n" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(0.35f),
                        )
                        Text(name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.65f))
                    }
                }
                item { HorizontalDivider() }
            }

            val showInstructions = !recipe.instructions.isNullOrBlank() || state.isTranslating
            if (showInstructions) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("Preparación", style = MaterialTheme.typography.titleMedium)
                            if (state.isTranslating) {
                                Spacer(Modifier.width(8.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        val body = recipe.instructionsEs ?: recipe.instructions
                        if (!body.isNullOrBlank()) {
                            Text(body, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
