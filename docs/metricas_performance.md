# Métricas de Performance — MenúSemana

**Versión:** 1.0 · **Fecha:** 2026-06-15  
**Herramienta:** AndroidX **Macrobenchmark** (módulo [`:benchmark`](../benchmark)) + **Baseline Profile** + R8/minify.

Mide los dos requisitos no funcionales de la consigna: **cold start < 2.5 s** y **scroll > 54 fps**.

---

## 1. Metodología

- **Build medido:** buildType `benchmark` (release-like: `isMinifyEnabled = true`, R8), con **Baseline Profile** instalado vía `ProfileInstaller`.
- **Iteraciones:** 5, `StartupMode.COLD`.
- **Métricas:** `StartupTimingMetric` (cold start) y `FrameTimingMetric` (scroll/fps).
- **Comando de reproducción:**
  ```bash
  ./gradlew :benchmark:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.example.benchmark.ExampleStartupBenchmark,com.example.benchmark.ScrollBenchmark
  ```

---

## 2. Cold start (HU-08.4) — ✅ cumple el objetivo

**Objetivo:** < 2.5 s.

| Estadística | timeToInitialDisplay |
|-------------|----------------------|
| Mínimo | **1.173 s** |
| **Mediana** | **1.386 s** |
| Máximo | 2.309 s |
| Coef. de variación | 0.29 |

✅ La **mediana (1.39 s)** está holgadamente bajo el objetivo de 2.5 s; incluso el máximo (2.31 s) cumple.

> ⚠️ **Entorno de esta corrida:** emulador `sdk_gphone64_x86_64` (API 36), **no** un Pixel 9 Pro físico. En hardware real el cold start suele ser **igual o mejor**. Para la evidencia final de H2 se recomienda repetir en el dispositivo objetivo (Pixel 9 Pro, 4 GB RAM / 2 cores) y reemplazar esta tabla.

---

## 3. Scroll / fps (HU-08.5) — ⛔ pendiente de dispositivo + seed

**Objetivo:** > 54 fps p90 (frame timing).

La corrida en emulador **no produjo resultados** por dos motivos:

1. **Falta seed de datos (#25):** `ScrollBenchmark.scrollComidas` hace scroll sobre **"Mis comidas"** y aborta si la lista está vacía (`"La lista está vacía…"`). En una instalación limpia no hay comidas que scrollear.
2. **Limitación del emulador:** la captura de traza Perfetto/`TraceProcessor` es inestable en emulador (`BR_DEAD_REPLY`); `FrameTimingMetric` requiere preferentemente un **dispositivo físico**.

**Para completar la evidencia:**
- Implementar el **seed inicial (#25)** o precargar comidas manualmente antes de correr.
- Ejecutar `ScrollBenchmark` en un **dispositivo físico** y registrar `frameDurationCpuMs`/`frameOverrunMs` (p90) acá.

---

## 4. Optimizaciones aplicadas

- **Baseline Profile** (`BaselineProfileGenerator`) → reduce el JIT en el arranque y el scroll.
- **R8/minify** en el build de release.
- **`ProfileInstaller`** incluido como dependencia de `app`.
- Carga de imágenes asíncrona con **Coil**; listas con `LazyColumn`/`LazyVerticalGrid` y `key` estable.

---

## 5. Resumen

| Requisito | Objetivo | Resultado | Estado |
|-----------|----------|-----------|--------|
| Cold start | < 2.5 s | 1.39 s (mediana, emulador) | ✅ (repetir en Pixel 9 Pro) |
| Scroll | > 54 fps p90 | — | ⛔ pendiente (dispositivo + seed #25) |
