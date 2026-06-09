//MEJORAR... TENGO QUE ARREGLARLO.

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

        repeat(2) {
            device.wait(Until.findObject(By.text("Siguiente")), 1000)?.click()
        }
        device.wait(Until.findObject(By.text("Empezar")), 1000)?.click()

        device.waitForIdle(3000)


        val tabEncontrado =
            device.wait(Until.findObject(By.res("tab_mis_comidas")), 5000)?.also { it.click() }
                ?: device.wait(Until.findObject(By.text("Mis comidas")), 5000)?.also { it.click() }
                ?: device.wait(Until.findObject(By.desc("Mis comidas")), 5000)?.also { it.click() }

        check(tabEncontrado != null) { "No se encontró el tab 'Mis comidas'" }

        device.waitForIdle(2000)

        if (device.findObject(By.text("Todavía no tenés comidas")) != null) {
            println("Sin comidas precargadas, saltando iteración.")
            return@measureRepeated
        }

        val todosScrollables = device.findObjects(By.scrollable(true))
        val lista = todosScrollables?.firstOrNull { obj ->
            runCatching { obj.visibleBounds.height() > obj.visibleBounds.width() }.getOrDefault(false)
        }

        check(lista != null) { "No se encontró ningún elemento scrollable vertical en 'Mis comidas'." }

        lista.setGestureMargin(device.displayWidth / 5)
        repeat(5) {
            lista.scroll(Direction.DOWN, 0.8f)
            device.waitForIdle(500)
        }
    }
}