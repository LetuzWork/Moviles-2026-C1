package com.menusemana.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.menusemana.core.designsystem.component.MsPrimaryButton
import com.menusemana.core.designsystem.theme.Neutral50
import com.menusemana.core.designsystem.theme.Persimmon300
import com.menusemana.core.designsystem.theme.Persimmon500
import com.menusemana.core.designsystem.theme.Saffron500

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Rounded.CalendarMonth,
        title = "Planificá tu semana",
        body = "Asigná comidas a cada turno del día y tené todo organizado de un vistazo.",
    ),
    OnboardingPage(
        icon = Icons.Rounded.PhotoCamera,
        title = "Guardá tus comidas",
        body = "Creá tus propias recetas con foto, ingredientes y pasos de preparación.",
    ),
    OnboardingPage(
        icon = Icons.Rounded.ShoppingBasket,
        title = "Lista de compras automática",
        body = "Generamos la lista de compras de la semana agrupada por rubro. Sin esfuerzo.",
    ),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var currentPage by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Persimmon500, Saffron500))
            )
    ) {
        TextButton(
            onClick = {
                viewModel.markOnboardingSeen()
                onFinished()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
        ) {
            Text("Saltar", color = Neutral50)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val page = pages[currentPage]

            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = Neutral50,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Neutral50,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = Neutral50.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    val color by animateColorAsState(
                        targetValue = if (index == currentPage) Neutral50 else Neutral50.copy(alpha = 0.4f),
                        animationSpec = tween(240),
                        label = "indicator",
                    )
                    Box(
                        modifier = Modifier
                            .size(if (index == currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            MsPrimaryButton(
                text = if (currentPage == pages.lastIndex) "Empezar" else "Siguiente",
                onClick = {
                    if (currentPage < pages.lastIndex) {
                        currentPage++
                    } else {
                        viewModel.markOnboardingSeen()
                        onFinished()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
