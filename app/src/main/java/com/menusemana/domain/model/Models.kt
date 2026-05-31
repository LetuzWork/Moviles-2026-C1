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

enum class MealCategory(val label: String) {
    COMIDA("Comida"),
    COLACION("Colación"),
}

enum class MealSlot(val index: Int, val label: String) {
    MANANA(0, "Mañana"),
    MEDIODIA(1, "Mediodía"),
    TARDE(2, "Tarde"),
    NOCHE(3, "Noche"),
}

enum class Aisle(val label: String) {
    VERDULERIA("Verdulería"),
    CARNICERIA("Carnicería"),
    LACTEOS("Lácteos"),
    ALMACEN("Almacén"),
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
    val thumbUrl: String?,
    val area: String?,
    val category: String?,
    val instructions: String?,
    val ingredients: List<Pair<String, String>>,
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
