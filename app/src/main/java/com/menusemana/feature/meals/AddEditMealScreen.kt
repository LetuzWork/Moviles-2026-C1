package com.menusemana.feature.meals

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.menusemana.core.common.PhotoStorage
import com.menusemana.core.designsystem.component.MsChoiceChip
import com.menusemana.core.designsystem.component.MsPrimaryButton
import com.menusemana.core.designsystem.component.MsTextField
import com.menusemana.core.designsystem.component.MsTopAppBar
import com.menusemana.core.designsystem.theme.Neutral300
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.domain.model.Aisle
import com.menusemana.domain.model.MealCategory

@Composable
fun AddEditMealScreen(
    mealId: Long?,
    onNavigateUp: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditMealViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pendingPhotoUri = remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingPhotoUri.value?.let { viewModel.onPhotoTaken(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = PhotoStorage.createPhotoUri(context)
            pendingPhotoUri.value = uri
            takePictureLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            MsTopAppBar(
                title = if (mealId == null) "Nueva comida" else "Editar comida",
                onNavigateUp = onNavigateUp,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(1.dp, Neutral300, MaterialTheme.shapes.extraLarge)
                        .clickable { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.photoUri != null) {
                        AsyncImage(
                            model = state.photoUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge),
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, tint = Persimmon500, modifier = Modifier.size(40.dp))
                            Text("Sacar foto", style = MaterialTheme.typography.labelLarge, color = Persimmon500)
                            Text("(opcional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                MsTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nombre *",
                    isError = state.nameError,
                    errorMessage = if (state.nameError) "Este campo es obligatorio" else null,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MsTextField(
                        value = state.timeMinutes,
                        onValueChange = viewModel::onTimeChange,
                        label = "Minutos",
                        modifier = Modifier.weight(1f),
                    )
                    MsTextField(
                        value = state.servings,
                        onValueChange = viewModel::onServingsChange,
                        label = "Porciones *",
                        modifier = Modifier.weight(1f),
                        isError = state.servingsError,
                        errorMessage = if (state.servingsError) "Mín. 1" else null,
                    )
                }
            }

            item {
                Text("Categoría", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                    MealCategory.entries.forEach { cat ->
                        MsChoiceChip(
                            label = cat.label,
                            selected = state.category == cat.label,
                            onClick = { viewModel.onCategoryChange(cat.label) },
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ingredientes", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = viewModel::addIngredient) {
                        Icon(Icons.Rounded.Add, contentDescription = "Agregar ingrediente", tint = Persimmon500)
                    }
                }
            }

            itemsIndexed(state.ingredients) { index, ing ->
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MsTextField(value = ing.name, onValueChange = { viewModel.onIngredientNameChange(index, it) }, label = "Ingrediente")
                        MsTextField(value = ing.quantity, onValueChange = { viewModel.onIngredientQuantityChange(index, it) }, label = "Cantidad")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Aisle.entries.forEach { aisle ->
                                MsChoiceChip(
                                    label = aisle.label.take(4),
                                    selected = ing.aisle == aisle.label,
                                    onClick = { viewModel.onIngredientAisleChange(index, aisle.label) },
                                )
                            }
                        }
                    }
                    if (state.ingredients.size > 1) {
                        IconButton(onClick = { viewModel.removeIngredient(index) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Quitar")
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            item {
                MsTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = "Preparación / Notas",
                    singleLine = false,
                )
            }

            item {
                MsPrimaryButton(
                    text = "Guardar",
                    onClick = { viewModel.save(onSaved) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                )
            }
        }
    }
}
