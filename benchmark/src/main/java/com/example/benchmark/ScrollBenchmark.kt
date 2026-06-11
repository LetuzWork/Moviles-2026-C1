package com.example.benchmark

import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun scrollComidas() = benchmarkRule.measureRepeated(
        packageName = "com.menusemana",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()

        // 1. Pasar el Onboarding
        repeat(2) {
            device.wait(Until.findObject(By.text("Siguiente")), 1000)?.click()
        }
        device.wait(Until.findObject(By.text("Empezar")), 1000)?.click()

        device.waitForIdle()

        // 2. Encontrar y hacer clic en la pestaña
        val tabEncontrado =
            device.wait(Until.findObject(By.res("tab_mis_comidas")), 5000)?.also { it.click() }
                ?: device.wait(Until.findObject(By.text("Mis comidas")), 5000)?.also { it.click() }
                ?: device.wait(Until.findObject(By.desc("Mis comidas")), 5000)?.also { it.click() }

        check(tabEncontrado != null) { "No se encontró el tab 'Mis comidas'" }

        device.waitForIdle()

        // 3. Validar contenido
        if (device.findObject(By.text("Todavía no tenés comidas")) != null) {
            throw AssertionError("La lista está vacía. Por favor, precargá comidas manualmente antes de correr el test para poder medir el scroll.")
        }

        // 4. Buscar la lista y medir el scroll
        val todosScrollables = device.findObjects(By.scrollable(true))
        val lista = todosScrollables?.firstOrNull { obj ->
            runCatching { obj.visibleBounds.height() > obj.visibleBounds.width() }.getOrDefault(false)
        }

        check(lista != null) { "No se encontró ningún elemento scrollable vertical en 'Mis comidas'." }

        lista.setGestureMargin(device.displayWidth / 5)

        // Usamos fling para simular un swipe rápido y natural del usuario
        repeat(3) {
            lista.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}