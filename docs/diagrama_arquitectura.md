# Diagrama de Arquitectura — MenúSemana

**Versión:** 1.0  
**Fecha:** 2026-05-31

---

## Diagrama de capas y flujo de datos

```
┌─────────────────────────────────────────────────────────────────┐
│                          PRESENTATION                           │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐  ┌───────┐  │
│  │ MiSemana     │  │ MisComidas   │  │  Recetas  │  │Compras│  │
│  │ Screen       │  │ Screen       │  │  Screen   │  │Screen │  │
│  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘  └───┬───┘  │
│         │                 │                │             │      │
│  ┌──────▼───────┐  ┌──────▼───────┐  ┌─────▼─────┐  ┌───▼───┐  │
│  │ PlanViewModel│  │MisComidasVM  │  │ RecetasVM │  │Shpg.VM│  │
│  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘  └───┬───┘  │
└─────────┼─────────────────┼────────────────┼─────────────┼─────┘
          │                 │                │             │
          ▼                 ▼                ▼             ▼
┌─────────────────────────────────────────────────────────────────┐
│                            DOMAIN                               │
│                                                                 │
│   Repositories (interfaces)        Use Cases                    │
│   ┌────────────────────┐           ┌─────────────────────────┐  │
│   │  MealRepository    │           │  AssignMealToSlotUseCase│  │
│   │  PlanRepository    │           │  GenerateShoppingList   │  │
│   │  RecipeRepository  │           │  UseCase                │  │
│   └────────────────────┘           └─────────────────────────┘  │
│                                                                 │
│   Models: Meal · Ingredient · PlannedMeal · Recipe              │
│           MealCategory · MealSlot · Aisle · ShoppingSection     │
└─────────────────────────────────────────────────────────────────┘
          │                                   │
          ▼                                   ▼
┌────────────────────────────┐   ┌────────────────────────────────┐
│           DATA             │   │           DATA                 │
│    (local persistence)     │   │      (remote / cache)          │
│                            │   │                                │
│  MealRepositoryImpl        │   │  RecipeRepositoryImpl          │
│  PlanRepositoryImpl        │   │                                │
│         │                  │   │    Retrofit  ──►  TheMealDB    │
│         ▼                  │   │       │           REST API     │
│  ┌─────────────────────┐   │   │       ▼                        │
│  │     Room DB         │   │   │  RecipeCacheDao                │
│  │  ┌─────────────┐    │   │   │  (offline fallback)            │
│  │  │   meals     │    │   │   └────────────────────────────────┘
│  │  │ ingredients │    │   │
│  │  │planned_meals│    │   │   ┌────────────────────────────────┐
│  │  │recipe_cache │    │   │   │        DataStore               │
│  │  └─────────────┘    │   │   │   onboarding_seen: Boolean     │
│  └─────────────────────┘   │   └────────────────────────────────┘
└────────────────────────────┘
```

### Regla de dependencias

| Capa | Qué contiene | Qué sabe |
|------|-------------|----------|
| **Presentation** | Screens (Compose) + ViewModels | Solo modelos de dominio y estado UI |
| **Domain** | Modelos, interfaces de repositorios, use cases | Nada de Android ni de bases de datos |
| **Data** | Implementaciones de repositorios, DAOs, DTOs | Room, Retrofit, DataStore |

Las dependencias apuntan hacia adentro: Presentation depende de Domain, Data depende de Domain, pero **Domain no depende de nadie**. Esto permite cambiar la base de datos o la API sin tocar la lógica de negocio.

---

## Patrón general

La aplicación sigue una arquitectura **MVVM + Repository + Use Cases** en capas estrictamente separadas:

```
feature  →  domain  →  data  →  [ Room DB / Retrofit / DataStore ]
```

---

## Estructura de paquetes

```
com.menusemana/
├── MainActivity.kt
├── MenuSemanaApp.kt              ← Application, inicializa Hilt
├── navigation/
│   └── AppNavigation.kt         ← Grafo de navegación tipado
├── feature/
│   ├── onboarding/              ← Pantalla de bienvenida (una sola vez)
│   ├── plan/                    ← Planificador semanal
│   ├── meals/                   ← CRUD de comidas propias
│   ├── recipes/                 ← Búsqueda en TheMealDB
│   └── shopping/                ← Lista de compras generada
├── domain/
│   ├── model/Models.kt          ← Modelos puros sin dependencias Android
│   ├── repository/              ← Interfaces de repositorio
│   └── usecase/                 ← Casos de uso independientes
├── data/
│   └── repository/              ← Implementaciones concretas + módulo Hilt
└── core/
    ├── database/                ← Room: entidades, DAOs, base de datos
    ├── network/                 ← Retrofit: API, DTOs, módulo Hilt
    ├── designsystem/            ← Tema, componentes compartidos
    ├── common/                  ← Utilidades transversales
    └── datastore/               ← DataStore para preferencias
```

---

## Flujo de datos reactivo

```
Room → DAO (Flow) → RepositoryImpl.map() → ViewModel.StateFlow → Compose (collectAsStateWithLifecycle)
```

---

## Grafo de navegación

```
Onboarding  →  (una sola vez, guardado en DataStore)
                     ↓
                   Plan (Mi semana)     ←→  Meals (Mis comidas)
                     │                            │
                     │                    AddMeal / EditMeal
                     │                    MealDetail
                     │
                  Recipes (Recetas)
                     │
                  RecipeDetail
                     │
                  Shopping (Compras)
```

La bottom navigation bar conecta Plan, Meals, Recipes y Shopping.

---

## Descripción de las tecnologías elegidas

### Kotlin 2.0.21
Lenguaje oficial de Android desde 2019. Se eligió por su sintaxis concisa, null-safety nativa, coroutines integradas para operaciones asíncronas, y compatibilidad total con Jetpack. Las **coroutines** (`suspend fun`, `Flow`) son la base del manejo asíncrono en toda la app: reemplazan callbacks y AsyncTask con código lineal y fácil de leer.

### Jetpack Compose + Material3
Framework declarativo de UI de Google. En lugar de inflar XMLs y manipular vistas imperativamente, cada pantalla se describe como una función `@Composable` que recibe estado y emite UI. Se eligió porque:
- El estado fluye en una sola dirección (ViewModel → Screen), lo que elimina bugs de sincronización
- La recomposición automática actualiza solo los elementos que cambiaron
- Material3 provee componentes accesibles y coherentes con el diseño de Android moderno

### Hilt (Inyección de Dependencias)
Biblioteca oficial de DI para Android, construida sobre Dagger. Genera el código de inyección en tiempo de compilación (no en runtime), lo que lo hace rápido y seguro. Se eligió para evitar pasar dependencias manualmente entre clases y facilitar el testing al poder sustituir implementaciones. Cada ViewModel, Repository e DAO se obtiene automáticamente sin `new` ni singletons manuales.

### Room 2.6.1 (Base de Datos Local)
ORM sobre SQLite incluido en Jetpack. Se eligió por tres razones: genera SQL a partir de anotaciones Kotlin (menos código boilerplate), devuelve `Flow<T>` en las queries lo que hace que la UI se actualice automáticamente cuando cambian los datos, y valida las queries SQL en tiempo de compilación. La app tiene cuatro tablas: `meals`, `ingredients`, `planned_meals` y `recipe_cache`.

### Retrofit 2.11.0 + Moshi (Red)
**Retrofit** convierte una interfaz Kotlin en llamadas HTTP completas — basta declarar el endpoint y el tipo de retorno, Retrofit se encarga de abrir la conexión, enviar el request y leer la respuesta. **Moshi** serializa/deserializa el JSON a data classes Kotlin con adaptadores generados en tiempo de compilación (más rápido que Gson). Se eligieron sobre alternativas como Ktor por su madurez, documentación extensa, y soporte nativo de coroutines.

### Navigation Compose 2.8.3 (Navegación)
Sistema de navegación oficial para Compose con rutas type-safe. En lugar de strings (`"meal_detail/{id}"`) que fallan en runtime, se usan data classes serializables (`MealDetail(mealId: Long)`) que el compilador verifica. Se eligió para evitar errores de navegación en producción y tener un único grafo de navegación centralizado en `MainActivity`.

### Coil (Carga de Imágenes)
Biblioteca de carga de imágenes escrita en Kotlin puro, optimizada para Compose. Maneja caché en memoria y en disco, redimensionado, y carga asíncrona con un componente `AsyncImage` nativo de Compose. Se eligió sobre Glide/Picasso por ser la opción más integrada con el ecosistema Kotlin/Compose.

### DataStore (Preferencias)
Reemplazo moderno de `SharedPreferences` basado en `Flow` y Protobuf. Se usa exclusivamente para persistir si el usuario ya vio el onboarding. Se eligió sobre SharedPreferences porque las escrituras son asíncronas (no bloquean el hilo principal) y el acceso es reactivo.

### TheMealDB (API de Recetas)
API REST gratuita, sin necesidad de API key para el plan básico, con más de 280 recetas organizadas por categoría y origen. Se eligió sobre alternativas de pago (Edamam, Spoonacular) dado el contexto académico del proyecto. La app complementa sus limitaciones con conversión de medidas imperiales a métricas (`MetricConverter`) y traducción de ingredientes al español (`IngredientTranslator`).
