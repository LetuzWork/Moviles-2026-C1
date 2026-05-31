package com.menusemana.feature.recipes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.core.common.IngredientTranslator
import com.menusemana.core.designsystem.component.MsEmptyState
import com.menusemana.core.designsystem.component.MsLargeHeader
import com.menusemana.core.designsystem.component.MsPrimaryButton
import com.menusemana.core.designsystem.component.MsSearchBar
import com.menusemana.core.designsystem.component.MealPhotoCard
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.core.designsystem.theme.Warning500

@Composable
fun RecetasScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (String) -> Unit,
    viewModel: RecetasViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

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

        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Persimmon500)
            }
            state.error != null && state.recipes.isEmpty() -> MsEmptyState(
                title = "Sin resultados",
                body = "Revisá tu conexión e intentá de nuevo",
                icon = Icons.Rounded.Search,
                action = { MsPrimaryButton("Reintentar", onClick = viewModel::retry) },
                modifier = Modifier.fillMaxSize(),
            )
            state.recipes.isEmpty() -> MsEmptyState(
                title = "Sin resultados",
                body = "Probá con otro término de búsqueda",
                icon = Icons.Rounded.Search,
                modifier = Modifier.fillMaxSize(),
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.recipes, key = { it.mealDbId }) { recipe ->
                    MealPhotoCard(
                        name = recipe.name,
                        category = IngredientTranslator.translateCategory(recipe.category ?: ""),
                        thumbUrl = recipe.thumbUrl,
                        onClick = { onNavigateToDetail(recipe.mealDbId) },
                    )
                }
            }
        }
    }
}
