---
marp: true
theme: default
paginate: true
size: 16:9
---

<!--
Deck del pitch de H2 (HU-10.5). Formato Marp.
Renderizar a PDF/PPTX con la extensión "Marp for VS Code" o el Marp CLI:
  npx @marp-team/marp-cli docs/pitch_h2_slides.md --pdf
Guion ampliado (notas del orador): docs/pitch_h2.md
-->

# 📱 MenúSemana

### Planificá tu semana, comé mejor, comprá justo lo necesario

IFTS 18 · Desarrollo de Apps Móviles · 2° Año "B" · 2026

**Jeampierre Gonzalez** (Tech Lead) · **Rosana Sosa** (UX/UI)
**Facundo Palmaricciotti** (QA/DevOps) · **Martin Laguna** (PO/Tech Lead)

---

## El problema

- Decidir **qué cocinar cada día** es una carga mental diaria.
- Se compra **de más** (se desperdicia) o **de menos** (falta un ingrediente).
- Las recetas están dispersas y no se traducen en una **lista de compras**.

> Planificar, cocinar y comprar están **desconectados**.

---

## Los usuarios

- **Quién:** personas/familias que cocinan en casa y quieren organizar la semana y el presupuesto.
- **Dónde:** en el celular, en la cocina o en el súper — a veces **sin buena conexión**.
- **Qué necesitan:** rapidez, recetas con foto, lista de compras lista para usar, que funcione **offline**.

---

## La solución

Una app Android que conecta **planificar → cocinar → comprar**:

1. 📅 **Mi semana** — asignás comidas a cada turno del día.
2. 🍽️ **Mis comidas** — tu recetario personal (CRUD, con foto de cámara).
3. 🔎 **Recetas** — buscás en TheMealDB y las importás en un toque.
4. 🛒 **Compras** — lista **autogenerada** desde tu plan, por pasillo.

---

## Demo en vivo

1. **Onboarding** (primera vez).
2. **Recetas** → ver detalle → *Agregar a mis comidas*.
3. **Mis comidas** → crear comida con **foto de cámara**.
4. **Mi semana** → asignar comidas (resumen **X/28** + huecos).
5. **Compras** → lista por pasillos → **modo avión** = fallback offline.
6. **Perfil** → preferencias dietarias → filtran las recetas.
7. Modo **oscuro / dynamic color**.

---

## Métricas

| Métrica | Objetivo | Resultado |
|---|---|---|
| Cold start | < 2.5 s | **~1.4–1.6 s** (Macrobenchmark) |
| Scroll | > 54 fps p90 | a medir en Pixel 9 Pro |
| Tests unitarios | — | 7 suites |
| Tests E2E | ≥ 3 escenarios | ✅ 4 escenarios |
| CI | lint + test por PR | ✅ + ktlint/detekt |

> Optimizado con **Baseline Profile** + R8/minify.

---

## Arquitectura

**Kotlin 100% · MVVM + Repository + Use Cases · Jetpack Compose**

```
UI (Compose) → ViewModel (StateFlow) → UseCase → Repository → ┬ Room (local)
                                                              └ Retrofit/TheMealDB
```

- **DI:** Hilt · **Local:** Room + DataStore · **Remoto:** Retrofit + Moshi (con **fallback a caché**)
- **Sensor:** CameraX · **UI:** Material 3, dark mode, dynamic color, fuentes escalables

---

## Decisiones clave

- **TheMealDB** como backend REST público → foco en el cliente.
- **Caché-first** en recetas → funciona offline.
- **Lista de compras autogenerada** con clasificación por pasillo → valor diferencial.
- **Multi-módulo** (`app` + `benchmark`) para medir performance reproducible.
- **GitFlow + PRs + CI** → trabajo en equipo evidenciable.

---

## Aprendizajes

- El patrón **multi-back-stack** de Compose exige grafos anidados; aplicarlo sobre un grafo plano genera bugs (detectado y corregido).
- **Macrobenchmark + Baseline Profiles** para requisitos no funcionales medibles.
- Importancia de **firmar** correctamente el APK/AAB para un RC instalable.
- **CI temprano** evita romper `main`.

---

## Roadmap

- Compartir el plan / sincronización multi-dispositivo.
- Confirmación en acciones destructivas + "Deshacer".
- Firma del release vía Secrets en CI + publicación del AAB.
- Más filtros dietarios y sugerencias inteligentes.

---

# ¡Gracias!

### MenúSemana — de la idea a la góndola, sin fricción.

**¿Preguntas?**
