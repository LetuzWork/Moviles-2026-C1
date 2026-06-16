# Pitch H2 — MenúSemana

**Versión:** 1.0 · **Fecha:** 2026-06-15  
**Formato de defensa:** Demo en vivo (10 min) + Q&A (5 min)

Guion del pitch en formato de slides. Cada sección (`##`) = una diapositiva.

---

## 1. Portada

**MenúSemana** — Planificá tu semana, comé mejor, comprá justo lo necesario.

IFTS 18 · Desarrollo de Aplicaciones para Dispositivos Móviles · 2° Año "B" · 2026

**Equipo:** Jeampierre Gonzalez (Tech Lead) · Rosana Sosa (UX/UI) · Facundo Palmaricciotti (QA/DevOps) · Martin Laguna (PO/Tech Lead)

---

## 2. El problema

- Decidir **qué cocinar cada día** es una carga mental diaria.
- Se compra **de más** (comida que se desperdicia) o **de menos** (falta un ingrediente a mitad de receta).
- Las recetas están dispersas (capturas, webs, memoria) y no se traducen en una **lista de compras** concreta.

> El planificar/comprar está desconectado del cocinar.

---

## 3. Los usuarios

- **Persona principal:** persona/familia que cocina en casa y quiere organizar la semana y el presupuesto.
- **Contexto de uso:** móvil, en la cocina o en el supermercado, a veces **sin buena conexión**.
- **Necesidades:** rapidez, recetas con foto, una lista de compras lista para usar, funcionar offline.

---

## 4. La solución

Una app Android que conecta **planificar → cocinar → comprar**:

1. **Mi semana:** asignás comidas a cada franja del día.
2. **Mis comidas:** tu recetario personal (CRUD, con foto por cámara).
3. **Recetas:** explorás y buscás recetas externas (TheMealDB) y las importás en un toque.
4. **Compras:** lista generada **automáticamente** desde tu plan, agrupada por pasillo y con check.

---

## 5. Demo en vivo (recorrido sugerido)

1. **Onboarding** (primera vez).
2. Buscar una receta en **Recetas** → ver detalle → **"Agregar a mis comidas"** → vuelve al listado con confirmación.
3. **Mis comidas:** crear una comida nueva con **foto de cámara**.
4. **Mi semana:** asignar comidas a los días.
5. **Compras:** ver la lista autogenerada por pasillos; activar **modo avión** para mostrar el **fallback offline**.
6. Mostrar **modo oscuro / dynamic color**.

---

## 6. Métricas

| Métrica | Objetivo (consigna) | Resultado |
|---------|--------------------|-----------|
| Cold start | < 2.5 s (Pixel 9 Pro) | *(completar con Macrobenchmark — ver [`metricas_performance.md`](metricas_performance.md))* |
| Scroll | > 54 fps p90 | *(completar con Macrobenchmark)* |
| Tests unitarios | — | 7 suites (ViewModels, repos, use cases, utilitarios) |
| Tests E2E | ≥ 3 escenarios | `MenuSemanaE2ETest` |
| CI | lint + test por PR | ✅ GitHub Actions en verde |

> Optimizaciones aplicadas: **Baseline Profile** + `ProfileInstaller` + R8/minify en release.

---

## 7. Arquitectura

**Kotlin 100% · MVVM + Repository + Use Cases · Jetpack Compose**

```
UI (Compose)  →  ViewModel (StateFlow)  →  UseCase  →  Repository  →  ┬─ Room (local)
                                                                      └─ Retrofit/TheMealDB (remoto)
```

- **DI:** Hilt. **Local:** Room + DataStore. **Remoto:** Retrofit + Moshi (con **fallback a caché**).
- **Sensor:** CameraX (foto de comida). **UI:** Material 3, dark mode, dynamic color, tipografía escalable.
- Detalle completo en [`diagrama_arquitectura.md`](diagrama_arquitectura.md) y [`documentacion_tecnica.md`](documentacion_tecnica.md).

---

## 8. Decisiones clave

- **TheMealDB** como backend REST público → sin costo ni gestión de servidor, foco en el cliente.
- **Caché-first en recetas** → la app funciona offline (requisito no funcional de conectividad).
- **Lista de compras autogenerada** con clasificación por pasillo y traducción/conversión de ingredientes → valor diferencial.
- **Multi-módulo** (`app` + `benchmark`) para medir performance de forma reproducible.
- **GitFlow + PRs + CI** → trabajo en equipo evidenciable y builds verificables.

---

## 9. Aprendizajes

- Navegación con Compose: el patrón **multi-back-stack** exige grafos anidados por pestaña; aplicarlo sobre un grafo plano genera bugs de back-stack (detectado y corregido).
- **Macrobenchmark + Baseline Profiles** para cumplir requisitos no funcionales medibles.
- Importancia de **firmar** correctamente el APK/AAB para tener un RC instalable.
- CI temprano evita romper `main`.

---

## 10. Roadmap / próximos pasos

- Perfil + preferencias dietarias, resumen semanal, compartir lista.
- Confirmación en acciones destructivas + "Deshacer".
- Firma de release vía secrets en CI + publicación del AAB como artifact.

---

## 11. Cierre

**MenúSemana** — de la idea a la góndola, sin fricción.  
¿Preguntas?
