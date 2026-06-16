package com.menusemana.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.menusemana.core.designsystem.component.MsChoiceChip
import com.menusemana.core.designsystem.component.MsTopAppBar
import com.menusemana.domain.model.DietaryPreference

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerfilScreen(
    onNavigateUp: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { MsTopAppBar(title = "Perfil", onNavigateUp = onNavigateUp) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            Text("Preferencias dietarias", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Elegí tus preferencias para personalizar la búsqueda de recetas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DietaryPreference.entries.forEach { preference ->
                    val isSelected = preference in selected
                    MsChoiceChip(
                        label = preference.label,
                        selected = isSelected,
                        onClick = { viewModel.toggle(preference, !isSelected) },
                    )
                }
            }
        }
    }
}
