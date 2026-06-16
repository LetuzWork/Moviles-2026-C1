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

**Objetivo:** < 2.5 s. **Medición:** `StartupTimingMetric`, 5 iteraciones `COLD`.

Se corrió 3 veces en emulador (`sdk_gphone64_x86_64`, API 36) para evaluar la dispersión:

| Corrida | Mín | **Mediana** | Máx | CoV |
|---------|-----|-------------|-----|-----|
| 1 | 1.173 s | **1.386 s** | 2.309 s | 0.29 |
| 2 (host cargado) | 1.568 s | 2.300 s | 2.459 s | — |
| 3 | 1.453 s | **1.587 s** | 2.332 s | 0.21 |

✅ La **mediana en condiciones normales es ~1.4–1.6 s**, holgadamente bajo el objetivo de 2.5 s. La corrida 2 (mediana 2.30 s) ocurrió con el host saturado por una compilación en paralelo: ilustra que **el emulador es ruidoso** y depende de la carga de la máquina.

> ⚠️ **Es emulador, no un Pixel 9 Pro físico.** El criterio pide medir en Pixel 9 Pro (4 GB / 2 cores). En hardware real el cold start suele ser **igual o mejor** y más estable. Para la evidencia formal de H2, correr el comando de abajo en el dispositivo objetivo y pegar la tabla resultante.

**Reproducir en el dispositivo objetivo (1 comando):**
```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.example.benchmark.ExampleStartupBenchmark
```
Resultado esperado: `timeToInitialDisplayMs` mediana **< 2500 ms**.

---

## 3. Scroll / fps (HU-08.5) — ⛔ pendiente de dispositivo físico

**Objetivo:** > 54 fps p90 (frame timing).

El **seed de datos (#25) ya está implementado**, así que "Mis comidas" arranca con comidas para scrollear. Sin embargo, las corridas en emulador **no produjeron resultados**: la captura de traza Perfetto/`TraceProcessor` es inestable en emulador (`BR_DEAD_REPLY`). `FrameTimingMetric` requiere un **dispositivo físico**.

**Para completar la evidencia**, correr en un teléfono conectado y pegar acá `frameDurationCpuMs`/`frameOverrunMs` (p90):
```bash
./gradlew :benchmark:connectedBenchmarkAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.example.benchmark.ScrollBenchmark
```

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
| Cold start | < 2.5 s | ~1.4–1.6 s (mediana, emulador) | ✅ (confirmar en Pixel 9 Pro) |
| Scroll | > 54 fps p90 | — | ⛔ pendiente (dispositivo físico; seed #25 ya hecho) |
