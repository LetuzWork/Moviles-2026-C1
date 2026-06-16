package com.menusemana.domain.model

data class Ingredient(
    val id: Long = 0,
    val name: String,
    val quantity: String,
    val aisle: String,
)

data class Meal(
    val id: Long = 0,
    val name: String,
    val photoUri: String? = null,
    val timeMinutes: Int = 0,
    val servings: Int = 1,
    val category: String = MealCategory.COMIDA.label,
    val notes: String? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val sourceRecipeId: String? = null,
)

enum class MealCategory(
    val label: String,
) {
    COMIDA("Comida"),
    COLACION("Colación"),
}

enum class MealSlot(
    val index: Int,
    val label: String,
) {
    MANANA(0, "Mañana"),
    MEDIODIA(1, "Mediodía"),
    TARDE(2, "Tarde"),
    NOCHE(3, "Noche"),
}

enum class Aisle(
    val label: String,
) {
    VERDULERIA("Verdulería"),
    CARNICERIA("Carnicería"),
    LACTEOS("Lácteos"),
    ALMACEN("Almacén"),
}

/**
 * Preferencias dietarias del usuario (HU-01.3). [mealDbCategory] es la categoría
 * de TheMealDB con la que se filtran las recetas (null = no filtra la API).
 */
enum class DietaryPreference(
    val label: String,
    val mealDbCategory: String?,
) {
    VEGETARIANO("Vegetariano", "Vegetarian"),
    VEGANO("Vegano", "Vegan"),
    SIN_GLUTEN("Sin gluten", null),
    SIN_LACTEOS("Sin lácteos", null),
}

data class PlannedMeal(
    val id: Long = 0,
    val weekStartEpochDay: Long,
    val dayOfWeek: Int,
    val slot: Int,
    val meal: Meal,
)

data class Recipe(
    val mealDbId: String,
    val name: String,
    val nameEs: String? = null,
    val thumbUrl: String?,
    val area: String?,
    val category: String?,
    val instructions: String?,
    val instructionsEs: String? = null,
    val ingredients: List<Pair<String, String>>,
    val ingredientsEs: List<Pair<String, String>>? = null,
)

data class ShoppingItem(
    val name: String,
    val quantity: String,
    val aisle: String,
    val bought: Boolean = false,
)

data class ShoppingSection(
    val aisle: String,
    val items: List<ShoppingItem>,
)
