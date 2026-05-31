package com.menusemana.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.menusemana.core.designsystem.theme.Herb100
import com.menusemana.core.designsystem.theme.Herb800
import com.menusemana.core.designsystem.theme.Neutral0
import com.menusemana.core.designsystem.theme.Neutral300
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.core.designsystem.theme.PillShape

@Composable
fun MsFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier.height(34.dp),
        shape = PillShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Persimmon500,
            selectedLabelColor = Neutral0,
            containerColor = Neutral0,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = FilterChipDefaults.filterChipBorder(
            selected = selected,
            enabled = true,
            selectedBorderColor = Persimmon500,
            borderColor = Neutral300,
        ),
    )
}

@Composable
fun MsChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier.height(34.dp),
        shape = PillShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Herb100,
            selectedLabelColor = Herb800,
            containerColor = Neutral0,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = FilterChipDefaults.filterChipBorder(
            selected = selected,
            enabled = true,
            selectedBorderColor = Herb100,
            borderColor = Neutral300,
        ),
    )
}
