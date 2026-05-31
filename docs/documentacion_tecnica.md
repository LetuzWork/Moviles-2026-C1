# Documentación Técnica — MenúSemana

**Versión:** 1.0  
**Plataforma:** Android  
**Fecha:** 2026-05-31

---

## 1. Descripción general

MenúSemana es una aplicación Android nativa para planificación semanal de comidas. Permite al usuario gestionar su propio recetario personal, asignar comidas a los días de la semana, explorar recetas externas y generar automáticamente la lista de compras de la semana.

---

## 2. Requisitos técnicos

| Parámetro | Valor |
|-----------|-------|
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 35 |
| `compileSdk` | 36 |
| Lenguaje | Kotlin 100% |
| UI toolkit | Jetpack Compose + Material 3 |
| JVM target | Java 17 |

---

## 3. Arquitectura

La aplicación sigue una arquitectura **MVVM + Repository + Use Cases** en capas estrictamente separadas. Ver diagrama completo de capas, estructura de paquetes y grafo de navegación en [diagrama_arquitectura.md](diagrama_arquitectura.md).

---

## 4. Dependencias principales

| Librería | Propósito |
|----------|-----------|
| Jetpack Compose + Material 3 | UI declarativa |
| Hilt | Inyección de dependencias |
| Room | Base de datos local SQLite |
| Retrofit + Moshi | Consumo de API REST + deserialización JSON |
| CameraX | Captura de fotografías |
| Coil | Carga asíncrona de imágenes |
| DataStore (Preferences) | Persistencia de preferencias ligeras |
| Coroutines + Flow | Programación asíncrona reactiva |
| Kotlinx.serialization | Rutas de navegación tipadas |
| JUnit + MockK + Turbine | Testing unitario y de flows |

---

## 5. Capa de dominio

### 5.1 Modelos

```kotlin
data class Meal(
    val id: Long,
    val name: String,
    val photoUri: String?,
    val timeMinutes: Int,
    val servings: Int,
    val category: String,       // "Comida" | "Colación"
    val notes: String?,
    val ingredients: List<Ingredient>,
    val sourceRecipeId: String?, // referencia a TheMealDB si fue importada
)

data class Ingredient(
    val id: Long,
    val name: String,
    val quantity: String,
    val aisle: String,          // pasillo del supermercado
)

data class PlannedMeal(
    val id: Long,
    val weekStartEpochDay: Long, // lunes de la semana como EpochDay
    val dayOfWeek: Int,          // 1=Lunes … 7=Domingo
    val slot: Int,               // 0=Mañana, 1=Mediodía, 2=Tarde, 3=Noche
    val meal: Meal,
)

data class Recipe(              // receta de TheMealDB (lectura)
    val mealDbId: String,
    val name: String,
    val thumbUrl: String?,
    val area: String?,
    val category: String?,
    val instructions: String?,
    val ingredients: List<Pair<String, String>>, // nombre → medida
)

data class ShoppingItem(val name: String, val quantity: String, val aisle: String, val bought: Boolean)
data class ShoppingSection(val aisle: String, val items: List<ShoppingItem>)
```

**Enumeraciones clave:**

```kotlin
enum class MealCategory(val label: String) { COMIDA("Comida"), COLACION("Colación") }

enum class MealSlot(val index: Int, val label: String) {
    MANANA(0, "Mañana"), MEDIODIA(1, "Mediodía"), TARDE(2, "Tarde"), NOCHE(3, "Noche")
}

enum class Aisle(val label: String) {
    VERDULERIA("Verdulería"), CARNICERIA("Carnicería"), LACTEOS("Lácteos"), ALMACEN("Almacén")
}
```

### 5.2 Interfaces de repositorio

```kotlin
interface MealRepository {
    fun getAllMeals(): Flow<List<Meal>>
    suspend fun getMealById(id: Long): Meal?
    suspend fun saveMeal(meal: Meal): Long
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMeal(meal: Meal)
}

interface PlanRepository {
    fun getWeekPlan(weekStartEpochDay: Long): Flow<List<PlannedMeal>>
    suspend fun assignMeal(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int, mealId: Long)
    suspend fun clearSlot(weekStartEpochDay: Long, dayOfWeek: Int, slot: Int)
}

interface RecipeRepository {
    suspend fun search(query: String): Result<List<Recipe>>
    suspend fun getById(mealDbId: String): Result<Recipe>
    fun getCachedRecipes(): Flow<List<Recipe>>
}
```

### 5.3 Casos de uso

**`GenerateShoppingListUseCase`**  
Combina el plan de la semana y todas las comidas via `combine()`. Agrupa los ingredientes por pasillo, consolida duplicados y devuelve `Flow<List<ShoppingSection>>`.

**`AssignMealToSlotUseCase`**  
Delegado delgado sobre `PlanRepository.assignMeal()`. Centraliza la lógica de asignación para facilitar testing.

### 5.4 Resultado sellado

```kotlin
sealed interface Result<out T> {
    data object Loading : Result<Nothing>
    data class Success<T>(val data: T) : Result<T>
    data class Error(val type: ErrorType, val cause: Throwable? = null) : Result<Nothing>
}

enum class ErrorType { Network, NotFound, Unknown }
```

---

## 6. Capa de datos

### 6.1 Base de datos Room

**Clase de base de datos:** `MenuSemanaDatabase`  
**Nombre del archivo:** `menusemana.db`  
**Estrategia de migración:** `fallbackToDestructiveMigration()` (uso en desarrollo)

#### Entidades

| Entidad | Tabla | Descripción |
|---------|-------|-------------|
| `MealEntity` | `meals` | Comida sin ingredientes |
| `IngredientEntity` | `ingredients` | FK → `meals.id` con CASCADE |
| `PlannedMealEntity` | `planned_meals` | Índice único `(weekStart, day, slot)` |
| `RecipeCacheEntity` | `recipe_cache` | Caché de recetas externas |

#### DAOs

**`MealDao`**

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getAllMealsWithIngredients()` | `Flow<List<MealWithIngredients>>` | Observa todos con @Transaction |
| `getMealWithIngredientsById(id)` | `suspend` | Consulta única por ID |
| `searchMeals(query)` | `Flow<List<MealEntity>>` | Búsqueda LIKE |
| `insertMeal(meal)` | `suspend Long` | Devuelve el ID generado |
| `updateMeal(meal)` | `suspend` | Actualización completa |
| `deleteMeal(meal)` | `suspend` | Elimina (cascade ingredientes) |
| `insertIngredients(list)` | `suspend` | Inserción masiva |
| `deleteIngredientsByMealId(id)` | `suspend` | Limpia antes de re-insertar |

**`PlanDao`**

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getPlannedMealsForWeek(weekStart)` | `Flow<List<PlannedMealEntity>>` | Filtra por semana |
| `insertOrReplace(entity)` | `suspend` | OnConflict = REPLACE |
| `delete(entity)` | `suspend` | Elimina entrada exacta |
| `clearSlot(weekStart, day, slot)` | `suspend` | Elimina por coordenada |

**`RecipeCacheDao`**

| Método | Tipo | Descripción |
|--------|------|-------------|
| `getById(id)` | `suspend` | Búsqueda puntual |
| `getAllCached()` | `Flow<List<RecipeCacheEntity>>` | Todos los cacheados |
| `searchCached(query)` | `suspend` | Búsqueda LIKE en caché |
| `insert(entity)` | `suspend` | Upsert (REPLACE) |

### 6.2 Networking

**API base:** `https://www.themealdb.com/api/json/v1/1/`

**Endpoints consumidos:**

| Endpoint | Query | Descripción |
|----------|-------|-------------|
| `search.php?s=` | nombre | Búsqueda de recetas por nombre |
| `lookup.php?i=` | ID | Detalle completo de una receta |
| `categories.php` | — | Listado de categorías |
| `filter.php?c=` | categoría | Recetas por categoría |

**Stack de red:** Retrofit + Moshi + OkHttp  
- `MoshiConverterFactory` para deserialización
- `HttpLoggingInterceptor` en nivel `BASIC` (debug)

**DTO principal:** `MealDbItemDto`  
Mapea los 20 pares `strIngredient{N}` / `strMeasure{N}` de la API a una lista de pares limpia mediante `getIngredients()`.

**Caché offline:** Las recetas obtenidas de la API se persisten en `recipe_cache` automáticamente. Si hay error de red, el repositorio sirve los datos de caché.

### 6.3 Implementaciones de repositorio

**`MealRepositoryImpl`**  
Encapsula transacciones atómicas: `saveMeal()` inserta la entidad, obtiene el ID generado y luego inserta los ingredientes con ese ID. `updateMeal()` elimina primero los ingredientes anteriores y los re-inserta.

**`PlanRepositoryImpl`**  
Usa `combine()` de Coroutines para cruzar el plan semanal (`PlanDao`) con la lista de comidas (`MealDao`) y construir objetos `PlannedMeal` hidratados en tiempo real.

**`RecipeRepositoryImpl`**  
Aplica el patrón *network-then-cache*: ejecuta la llamada de red, guarda en Room si tiene éxito y en caso de fallo devuelve `Result.Error` con el tipo apropiado.

---

## 7. Inyección de dependencias (Hilt)

| Módulo | Scope | Contenido |
|--------|-------|-----------|
| `DatabaseModule` | `@Singleton` | `MenuSemanaDatabase`, los tres DAOs |
| `NetworkModule` | `@Singleton` | `OkHttpClient`, `Moshi`, `Retrofit`, `TheMealDbApi` |
| `RepositoryModule` | `@Singleton` | Bindings abstractos de las tres interfaces |
| `DataStoreModule` | `@Singleton` | `DataStore<Preferences>` |

Todos los ViewModels están anotados con `@HiltViewModel` e inyectados con `@Inject constructor`.

---

## 8. Capa de UI

### 8.1 Navegación

**Motor:** Jetpack Navigation Compose con rutas tipadas via `@Serializable`. El grafo completo está en [diagrama_arquitectura.md](diagrama_arquitectura.md).

Las rutas disponibles son: `Onboarding`, `Plan`, `Meals`, `Recipes`, `Shopping`, `MealDetail(mealId)`, `AddMeal`, `EditMeal(mealId)` y `RecipeDetail(mealDbId)`. La bottom navigation bar conecta Plan, Meals, Recipes y Shopping.

### 8.2 ViewModels y estados de UI

**`PlanViewModel` / `PlanUiState`**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `weekStart` | `LocalDate` | Lunes de la semana visualizada |
| `isCurrentWeek` | `Boolean` | Para mostrar/ocultar "Hoy" |
| `plannedMeals` | `List<PlannedMeal>` | Plan cargado de Room |
| `allMeals` | `List<Meal>` | Catálogo completo para el picker |
| `showMealPicker` | `Boolean` | Controla el bottom sheet |
| `pickerDay` / `pickerSlot` | `Int?` | Coordenada seleccionada |

Acciones: `previousWeek()`, `nextWeek()`, `goToCurrentWeek()`, `openMealPicker()`, `assignMealToSlot()`, `clearSlot()`

**`MisComidasViewModel` / `MisComidasUiState`**

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `meals` | `List<Meal>` | Lista filtrada en tiempo real |
| `query` | `String` | Texto de búsqueda |
| `selectedCategory` | `String?` | Filtro de categoría activo |
| `isLoading` | `Boolean` | Estado inicial |

**`AddEditMealViewModel` / `AddEditMealUiState`**  
Gestiona el formulario de creación/edición. Si recibe `mealId` via `SavedStateHandle`, pre-carga la comida existente. Valida que el nombre no esté vacío antes de llamar a `save()`.

**`RecetasViewModel` / `RecetasUiState`**  
Aplica debounce en la búsqueda. Diferencia entre `isOffline` (caché disponible) y `error` (sin datos). Reintento con `retry()`.

**`ShoppingViewModel` / `ShoppingUiState`**  
La lista de compras es completamente derivada: se calcula del plan actual via `GenerateShoppingListUseCase`. El estado `checkedItems` (items marcados como comprados) se guarda solo en memoria durante la sesión.

### 8.3 Design System

Ubicación: `core/designsystem/`

**Tema:** Material 3 con `dynamicColor = false`. Tokens de color personalizados:

| Token | Color | Uso |
|-------|-------|-----|
| Primary | Persimmon (naranja) | Acciones principales, FAB |
| Secondary | Herb (verde) | Chips de categoría |
| Tertiary | Saffron (amarillo) | Destacados |
| Error | Danger (rojo) | Errores y eliminación |

**Componentes compartidos:** `Buttons`, `TextFields`, `Cards`, `Chips`, `EmptyState`, `BottomNav`, `AppBars`

---

## 9. Utilidades transversales

### `MetricConverter`
Convierte medidas imperiales a métricas para ingredientes de TheMealDB:
- Volumen: cups, tbsp, tsp, fl oz, pint, quart, gallon → ml/l
- Peso: oz, lb, stick → g

### `IngredientTranslator`
Diccionario estático de más de 500 términos inglés → español. Se aplica a los ingredientes de TheMealDB antes de mostrarlos al usuario.

### `AisleClassifier`
Clasifica un nombre de ingrediente (en español) en uno de los pasillos del supermercado definidos en `Aisle`. Usado al importar recetas externas para pre-rellenar el campo de pasillo.

### `PhotoStorage`
Abstrae la escritura y lectura de fotos en el almacenamiento privado de la aplicación. Trabaja con `FileProvider` para URIs compartibles con CameraX.

---

## 10. Permisos del sistema

| Permiso | Motivo |
|---------|--------|
| `INTERNET` | Llamadas a TheMealDB API |
| `CAMERA` | Fotografiar comidas propias |

El permiso de cámara se solicita en tiempo de ejecución desde `AddEditMealScreen` antes de lanzar CameraX.

---

## 11. Flujo de datos reactivo

Toda la UI se actualiza de forma reactiva sin llamadas pull:

```
Room → DAO (Flow) → RepositoryImpl.map() → ViewModel.StateFlow → Compose (collectAsStateWithLifecycle)
```

Los `StateFlow` en ViewModels se construyen con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initialState)` para detener la colección cuando la UI no está activa y reanudarla sin pérdida de estado.

---

## 12. Testing

| Tipo | Herramientas |
|------|-------------|
| Unitario (lógica pura) | JUnit 4, MockK |
| Flow (coroutines) | Turbine |
| Integración (DAOs) | Room in-memory database |

---

## 13. Limitaciones conocidas

- `fallbackToDestructiveMigration()` destruye datos al cambiar el esquema. En producción debe reemplazarse por migraciones explícitas.
- El estado de items comprados en la lista de la compra no se persiste entre sesiones.
- La clave de TheMealDB utilizada es la pública gratuita (`/1/`), que no requiere autenticación pero tiene límites de peticiones.
