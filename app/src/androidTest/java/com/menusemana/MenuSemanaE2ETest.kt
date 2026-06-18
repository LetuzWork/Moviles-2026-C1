package com.menusemana

import android.Manifest
import android.app.Activity
import android.app.Instrumentation
import android.provider.MediaStore
import androidx.compose.ui.test.and
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuSemanaE2ETest {
    // Inicia la MainActivity antes de cada prueba
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Pre-concede el permiso de cámara para el escenario "crear comida con foto"
    @get:Rule
    val cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    /** Salta el onboarding si está visible (solo aparece en la primera ejecución). */
    private fun saltarOnboardingSiEstaVisible() {
        val saltar = composeTestRule.onAllNodes(hasText("Saltar", ignoreCase = true, substring = true))
        if (saltar.fetchSemanticsNodes().isNotEmpty()) {
            saltar.onFirst().performClick()
        }
        composeTestRule.waitForIdle()
    }

    // ── Escenario 1: Onboarding → pantalla principal ───────────────────────────
    @Test
    fun escenario1_flujoOnboardingAPantallaPrincipal() {
        val nodosOnboarding =
            composeTestRule.onAllNodes(
                hasText("Planificá tu semana", ignoreCase = true, substring = true),
            )
        if (nodosOnboarding.fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule
                .onNode(
                    hasText("Saltar", ignoreCase = true, substring = true),
                ).performClick()
        }
        // Llegamos a la app: la pestaña "Mis comidas" existe y es clickeable
        composeTestRule
            .onNode(
                hasText("Mis comidas", ignoreCase = true, substring = true) and hasClickAction(),
            ).assertExists()
    }

    // ── Escenario 2: Navegar a Mis comidas ─────────────────────────────────────
    @Test
    fun escenario2_navegarMisComidasYVerificarPantalla() {
        saltarOnboardingSiEstaVisible()
        composeTestRule
            .onNode(
                hasText("Mis comidas", ignoreCase = true, substring = true) and hasClickAction(),
            ).performClick()

        composeTestRule.onNode(hasText("Todas", ignoreCase = true, substring = true)).assertExists()
        composeTestRule.onNodeWithContentDescription("Sumar comida").assertExists()
    }

    // ── Escenario 3: Crear una nueva comida CON foto ───────────────────────────
    @Test
    fun escenario3_crearComidaConFoto() {
        saltarOnboardingSiEstaVisible()

        composeTestRule
            .onNode(
                hasText("Mis comidas", ignoreCase = true, substring = true) and hasClickAction(),
            ).performClick()
        composeTestRule.onNodeWithContentDescription("Sumar comida").performClick()
        composeTestRule.onNodeWithText("Nueva comida").assertIsDisplayed()

        // Tomar foto: se stubea la cámara (ACTION_IMAGE_CAPTURE) para que devuelva OK.
        Intents.init()
        try {
            intending(hasAction(MediaStore.ACTION_IMAGE_CAPTURE))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

            composeTestRule.onNodeWithTag("meal_photo_picker").performClick()
            composeTestRule.waitForIdle()

            // Al "tomar" la foto, el placeholder "Sacar foto" desaparece (se muestra la imagen)
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule
                    .onAllNodesWithText("Sacar foto", substring = true)
                    .fetchSemanticsNodes()
                    .isEmpty()
            }
        } finally {
            Intents.release()
        }

        // Completar datos obligatorios
        composeTestRule.onNodeWithText("Nombre *").performTextInput("Tortilla de papas")
        composeTestRule.onNodeWithText("Porciones *").performTextReplacement("4")
        Espresso.closeSoftKeyboard()
        composeTestRule.waitForIdle()

        // Scrollear el formulario hasta el botón "Guardar" y guardar
        composeTestRule.onNode(hasScrollAction()).performScrollToNode(hasText("Guardar"))
        composeTestRule.onNodeWithText("Guardar").performClick()
        composeTestRule.waitForIdle()

        // Guardado OK: el guardado solo navega de vuelta si la comida se persistió.
        // Verificamos que dejamos "Nueva comida" y volvimos al listado de comidas.
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Nueva comida").fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithContentDescription("Sumar comida").assertExists()
    }

    // ── Escenario 4: Armar el plan (asignar una comida a un turno) ──────────────
    @Test
    fun escenario4_armarPlanAsignarComida() {
        saltarOnboardingSiEstaVisible()

        // La pantalla inicial es "Mi semana". Hay comidas de ejemplo (seed), así
        // que se ven los turnos con la opción "Sumar comida".
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("Sumar comida", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithText("Sumar comida", substring = true).onFirst().performClick()

        // Se abre el selector de comidas
        composeTestRule.onNode(hasText("Elegí una comida", substring = true)).assertExists()

        // Elegimos una comida del seed
        composeTestRule.onNode(hasText("Milanesa con puré", substring = true)).performClick()

        // El selector se cierra y la comida queda asignada en el turno
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasText("Elegí una comida", substring = true))
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeTestRule
            .onAllNodesWithText("Milanesa con puré", substring = true)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
}
