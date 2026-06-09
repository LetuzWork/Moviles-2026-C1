package com.menusemana.feature.meals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.core.designsystem.component.MsEmptyState
import com.menusemana.core.designsystem.component.MsFab
import com.menusemana.core.designsystem.component.MsFilterChip
import com.menusemana.core.designsystem.component.MsLargeHeader
import com.menusemana.core.designsystem.component.MsSearchBar
import com.menusemana.core.designsystem.component.MealListRow
import com.menusemana.domain.model.MealCategory

private val CATEGORIES = listOf(null) + MealCategory.entries.map { it.label }

@Composable
fun MisComidasScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    viewModel: MisComidasViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val topPx = with(density) { contentPadding.calculateTopPadding().roundToPx() }
    val bottomPx = with(density) { contentPadding.calculateBottomPadding().roundToPx() }

    Scaffold(
        contentWindowInsets = WindowInsets(top = topPx, bottom = bottomPx),
        floatingActionButton = {
            MsFab(
                icon = Icons.Rounded.Add,
                contentDescription = "Sumar comida",
                onClick = onNavigateToAdd,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                MsLargeHeader(kicker = "Tus recetas", title = "Mis comidas")
                MsSearchBar(
                    query = state.query,
                    onQueryChange = viewModel::onQueryChange,
                    placeholder = "Buscar comidas…",
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(CATEGORIES) { category ->
                    MsFilterChip(
                        label = category ?: "Todas",
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            if (state.meals.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    MsEmptyState(
                        title = "Todavía no tenés comidas",
                        body = "Sumá tu primera comida con el botón +",
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f).testTag("lista_comidas")) {
                    items(state.meals, key = { it.id }) { meal ->
                        MealListRow(
                            name = meal.name,
                            timeMinutes = meal.timeMinutes,
                            ingredientCount = meal.ingredients.size,
                            category = meal.category,
                            photoUri = meal.photoUri,
                            onClick = { onNavigateToDetail(meal.id) },
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}
