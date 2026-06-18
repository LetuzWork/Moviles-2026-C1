package com.menusemana.screens.recipes

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.data.model.MealCategory
import com.menusemana.data.util.MealTranslator
import com.menusemana.screens.ui.component.MealPhotoCard
import com.menusemana.screens.ui.component.MsEmptyState
import com.menusemana.screens.ui.component.MsLargeHeader
import com.menusemana.screens.ui.component.MsPrimaryButton
import com.menusemana.screens.ui.component.MsSearchBar
import com.menusemana.screens.ui.theme.Persimmon500
import com.menusemana.screens.ui.theme.Warning500

@Composable
fun RecetasScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (String) -> Unit,
    importedRecipeName: String = "",
    onImportedConsumed: () -> Unit = {},
    viewModel: RecetasViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val gridState = rememberLazyGridState()

    LaunchedEffect(importedRecipeName) {
        if (importedRecipeName.isNotBlank()) {
            snackbarHostState.showSnackbar("\"$importedRecipeName\" se agregó a Mis comidas")
            onImportedConsumed()
        }
    }

    val reachedEnd by remember {
        derivedStateOf {
            val lastVisible =
                gridState.layoutInfo.visibleItemsInfo
                    .lastOrNull()
                    ?.index ?: 0
            val total = gridState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 4
        }
    }
    LaunchedEffect(reachedEnd) {
        if (reachedEnd && state.hasMore && !state.isLoadingMore && !state.isLoading && state.query.isBlank()) {
            viewModel.loadNextPage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                MsLargeHeader(kicker = "TheMealDB", title = "Recetas")
                Spacer(Modifier.height(12.dp))
                MsSearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    placeholder = "Buscar recetas…",
                )
            }

            if (state.isOffline) {
                Text(
                    "Sin conexión. Mostrando recetas guardadas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Warning500,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.mealTypeFilter == null,
                    onClick = { viewModel.setMealTypeFilter(null) },
                    label = { Text("Todos") },
                )
                MealCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = state.mealTypeFilter == cat,
                        onClick = { viewModel.setMealTypeFilter(if (state.mealTypeFilter == cat) null else cat) },
                        label = { Text(cat.label) },
                    )
                }
            }

            if (state.activeDietaryFilters.isNotEmpty()) {
                Text(
                    "Filtrando por tus preferencias: ${state.activeDietaryFilters.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Persimmon500,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            when {
                state.isLoading ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Persimmon500)
                    }
                state.error != null && state.recipes.isEmpty() ->
                    MsEmptyState(
                        title = "Sin resultados",
                        body = "Revisá tu conexión e intentá de nuevo",
                        icon = Icons.Rounded.Search,
                        action = { MsPrimaryButton("Reintentar", onClick = viewModel::retry) },
                        modifier = Modifier.fillMaxSize(),
                    )
                state.recipes.isEmpty() && state.query.isNotBlank() ->
                    MsEmptyState(
                        title = "Sin resultados",
                        body = "Probá con otro término de búsqueda",
                        icon = Icons.Rounded.Search,
                        modifier = Modifier.fillMaxSize(),
                    )
                else ->
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.recipes, key = { it.mealDbId }) { recipe ->
                            MealPhotoCard(
                                name = recipe.nameEs ?: recipe.name,
                                category = MealTranslator.translateCategory(recipe.category ?: ""),
                                area = recipe.area?.let { MealTranslator.translateArea(it) },
                                thumbUrl = recipe.thumbUrl,
                                onClick = { onNavigateToDetail(recipe.mealDbId) },
                            )
                        }
                        if (state.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = Persimmon500)
                                }
                            }
                        }
                    }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(contentPadding),
        )
    }
}
