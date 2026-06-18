# Checklist de Heurísticas de Nielsen — MenúSemana

**Versión:** 1.0  
**Plataforma:** Android (Jetpack Compose + Material 3)  
**Fecha:** 2026-06-15

Evaluación de la interfaz de MenúSemana contra las **10 heurísticas de usabilidad de Jakob Nielsen**. Para cada una se indica cómo la app la cumple (con evidencia concreta en pantallas/componentes), el estado y las acciones pendientes de mejora.

**Leyenda de estado:** ✅ Cumple · 🟡 Parcial · ⛔ Pendiente

---

## 1. Visibilidad del estado del sistema — ✅

La app mantiene al usuario informado de qué está ocurriendo.

- **Indicadores de carga:** `CircularProgressIndicator` mientras se buscan recetas (`RecetasScreen`) o se carga el detalle (`RecipeDetailScreen`).
- **Estado de conexión:** banner *"Sin conexión. Mostrando recetas guardadas."* cuando se usa la caché (`RecetasScreen`, `state.isOffline`).
- **Confirmación de acciones:** snackbar *"\<receta\> se agregó a Mis comidas"* tras importar una receta.
- **Pestaña activa:** la `NavigationBar` resalta la sección actual (píldora + color Persimmon).

---

## 2. Correspondencia entre el sistema y el mundo real — ✅

El lenguaje y los conceptos son los del dominio del usuario, no los técnicos.

- Vocabulario cotidiano: *"Mi semana"*, *"Mis comidas"*, *"Recetas"*, *"Compras"*.
- Lista de compras agrupada por **pasillos reales de góndola**: Verdulería, Carnicería, Lácteos, Almacén (`AisleClassifier`).
- **Traducción y unidades locales:** ingredientes traducidos al español (`IngredientTranslator`) y medidas convertidas al sistema métrico (`MetricConverter`).
- Franjas horarias naturales en el plan: Mañana, Mediodía, Tarde, Noche.

---

## 3. Control y libertad del usuario — ✅

El usuario puede deshacer y salir de los flujos con facilidad.

- **Botón "atrás"** consistente en todas las pantallas de detalle (`MsTopAppBar` con `onNavigateUp`).
- Tras agregar una receta, la app **vuelve al listado** conservando la búsqueda (sin atrapar al usuario en otra sección).
- Posibilidad de **vaciar un slot** del plan semanal (`clearSlot`) y de **eliminar** comidas propias.
- 🟡 *Pendiente:* confirmación ("¿Seguro?") antes de eliminar una comida, y opción "Deshacer" en el snackbar de borrado.

---

## 4. Consistencia y estándares — ✅

- **Material Design 3** en toda la app: `NavigationBar`, `TopAppBar`, `Card`, `TextField`, `Button`, `Snackbar`.
- **Design system propio** centralizado (`screens/ui`): componentes reutilizables (`MsPrimaryButton`, `MsSearchBar`, `MsTopAppBar`, `MealPhotoCard`, `MsEmptyState`) → misma apariencia y comportamiento en cada pantalla.
- Tipografía, colores, formas y espaciados unificados (`Type`, `Color`, `Shape`, `Spacing`).
- Soporte de **modo oscuro** y **dynamic color** (Android 12+).

---

## 5. Prevención de errores — 🟡

- **Validación de formularios** al crear/editar comidas (`AddEditMealViewModel`): no permite guardar sin nombre, muestra errores de validación.
- Navegación por pestañas con `launchSingleTop` → evita apilar duplicados de la misma pantalla.
- Fix de "falsos toques" entre tabs (PR #47).
- 🟡 *Pendiente:* diálogo de confirmación en acciones destructivas (eliminar comida / vaciar plan).

---

## 6. Reconocimiento antes que recuerdo — ✅

- Las opciones están **siempre visibles** en la barra inferior; el usuario no memoriza rutas.
- **Recetas con foto** en tarjetas (`MealPhotoCard`) → se reconocen visualmente.
- La búsqueda muestra resultados con imagen, nombre y categoría.
- Al importar una receta, sus ingredientes se precargan automáticamente (el usuario no los reescribe).

---

## 7. Flexibilidad y eficiencia de uso — ✅

- **Búsqueda** de recetas por nombre (`MsSearchBar`).
- **Generación automática** de la lista de compras a partir del plan semanal (`GenerateShoppingListUseCase`) → ahorra trabajo manual.
- **Importar receta → comida** en un toque, con ingredientes ya clasificados por pasillo.
- Caché local: las recetas vistas quedan disponibles sin conexión.

---

## 8. Diseño estético y minimalista — ✅

- Pantallas con jerarquía clara: encabezado (`MsLargeHeader`), contenido y acción principal.
- Grilla de 2 columnas para recetas; uso de espacios en blanco y tipografía escalable (`sp`, escala con el sistema).
- Sin información superflua; cada pantalla tiene un objetivo único.

---

## 9. Ayudar a reconocer, diagnosticar y recuperarse de errores — ✅

- **Estados vacíos** descriptivos (`MsEmptyState`): *"Sin resultados — Probá con otro término de búsqueda"*.
- **Recuperación de errores de red:** mensaje *"Revisá tu conexión e intentá de nuevo"* + botón **"Reintentar"** (`viewModel.retry()`).
- Mensajes en lenguaje claro, sin códigos de error técnicos.

---

## 10. Ayuda y documentación — 🟡

- **Onboarding** inicial de 3 pantallas que explica las funciones clave (calendario, foto, compras).
- Documentación de usuario en [`documentacion_usuario.md`](documentacion_usuario.md).
- 🟡 *Pendiente:* acceso a la ayuda/onboarding desde dentro de la app (p. ej. un ítem "Ayuda" o re-ver el onboarding).

---

## Resumen

| # | Heurística | Estado |
|---|-----------|--------|
| 1 | Visibilidad del estado del sistema | ✅ |
| 2 | Correspondencia con el mundo real | ✅ |
| 3 | Control y libertad del usuario | ✅ |
| 4 | Consistencia y estándares | ✅ |
| 5 | Prevención de errores | 🟡 |
| 6 | Reconocimiento antes que recuerdo | ✅ |
| 7 | Flexibilidad y eficiencia | ✅ |
| 8 | Diseño estético y minimalista | ✅ |
| 9 | Recuperación de errores | ✅ |
| 10 | Ayuda y documentación | 🟡 |

**Cumplimiento: 8/10 plenas, 2 parciales.** Mejoras priorizadas para el cierre: (a) confirmación en acciones destructivas + "Deshacer", (b) acceso a la ayuda dentro de la app.
