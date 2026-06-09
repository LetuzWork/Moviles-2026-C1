package com.example.finalmobile

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.menusemana.MainActivity



@RunWith(AndroidJUnit4::class)

class MenuSemanaE2ETest {
// Inicia la MainActivity antes de cada prueba
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun escenario1_flujoOnboardingAPantallaPrincipal() {
        // Verifica si la pantalla de Onboarding está visible
        val nodosOnboarding = composeTestRule.onAllNodes(hasText("Planificá tu semana", ignoreCase = true, substring = true))

        if (nodosOnboarding.fetchSemanticsNodes().isNotEmpty()) {
            // Si la encontró, valida que esté el subtítulo
            composeTestRule.onNode(hasText("Asigná comidas a cada turno del día y tené todo organizado de un vistazo.", ignoreCase = true, substring = true)).assertExists()

            // Toca el botón de Saltar
            composeTestRule.onNode(hasText("Saltar", ignoreCase = true, substring = true)).performClick()
        }

        // 2. Verifica que llego a la pantalla principal de la app.
        composeTestRule.onNode(
            hasText("Mis comidas", ignoreCase = true, substring = true) and hasClickAction()
        ).assertExists()
    }

    @Test
    fun escenario2_navegarMisComidasYVerificarPantalla() {
        // Saltar Onboarding si está visible
        val nodosSaltar = composeTestRule.onAllNodes(hasText("Saltar", ignoreCase = true, substring = true))
        if (nodosSaltar.fetchSemanticsNodes().isNotEmpty()) {
            nodosSaltar.onFirst().performClick()
        }

        // Clic en la pestaña "Mis comidas"
        composeTestRule.onNode(
            hasText("Mis comidas", ignoreCase = true, substring = true) and hasClickAction()
        ).performClick()


        composeTestRule.onNode(hasText("Mis comidas", ignoreCase = true, substring = true))
            .assertExists()

        // Verifica que cargó el filtro de categoría "Todas"
        composeTestRule.onNode(hasText("Todas", ignoreCase = true, substring = true))
            .assertExists()

        //Verifica que se renderizó el botón de agregar (+) por su contentDescription
        composeTestRule.onNodeWithContentDescription("Sumar comida")
            .assertExists()
    }

    @Test
    fun escenario3_crearNuevaComida() {

        val nodosSaltar = composeTestRule.onAllNodes(hasText("Saltar", ignoreCase = true, substring = true))
        if (nodosSaltar.fetchSemanticsNodes().isNotEmpty()) {

            nodosSaltar.onFirst().performClick()
        }

        //Ir a la sección de comidas.
        composeTestRule.onNode(
            hasText("Mis comidas", ignoreCase = true, substring = true) and hasClickAction()
        ).performClick()

        //Toca el botón flotante (+) para agregar.
        composeTestRule.onNodeWithContentDescription("Sumar comida").performClick()

        //Valida que estamos en la pantalla de creación.
        composeTestRule.onNodeWithText("Nueva comida").assertIsDisplayed()

        // Llena el input del nombre.
        composeTestRule.onNodeWithText("Nombre *")
            .performTextInput("Tortilla de papas")

        // 5. Llena el tiempo y porciones.
        composeTestRule.onNodeWithText("Minutos")
            .performTextReplacement("45")

        composeTestRule.onNodeWithText("Porciones *")
            .performTextReplacement("4")

        // desliza la pantalla hacia arriba
        composeTestRule.onRoot().performTouchInput { swipeUp() }

        // Guarda la nueva comida
        composeTestRule.onNodeWithText("Guardar").performClick()
    }
}

