package com.menusemana.core.database

import com.menusemana.domain.model.Aisle
import com.menusemana.domain.model.Ingredient
import com.menusemana.domain.model.Meal
import com.menusemana.domain.model.MealCategory

/**
 * Datos de ejemplo que se insertan en la primera instalación (ver [DatabaseModule]).
 * Cumple HU-06.4: el usuario ve comidas de ejemplo al instalar la app.
 * Las fotos viven en `assets/seed/` y se referencian con `file:///android_asset/...`.
 */
object SeedData {
    private fun asset(name: String) = "file:///android_asset/seed/$name.jpg"

    val meals: List<Meal> =
        listOf(
            Meal(
                name = "Milanesa con puré",
                photoUri = asset("milanesa"),
                timeMinutes = 40,
                servings = 4,
                category = MealCategory.COMIDA.label,
                notes = "Clásico de la cocina argentina.",
                ingredients =
                    listOf(
                        Ingredient(name = "Nalga (milanesas)", quantity = "800 g", aisle = Aisle.CARNICERIA.label),
                        Ingredient(name = "Pan rallado", quantity = "300 g", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Huevos", quantity = "3", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Papas", quantity = "1 kg", aisle = Aisle.VERDULERIA.label),
                        Ingredient(name = "Leche", quantity = "200 ml", aisle = Aisle.LACTEOS.label),
                        Ingredient(name = "Manteca", quantity = "50 g", aisle = Aisle.LACTEOS.label),
                    ),
            ),
            Meal(
                name = "Empanadas de carne",
                photoUri = asset("empanadas"),
                timeMinutes = 60,
                servings = 6,
                category = MealCategory.COMIDA.label,
                notes = "Una docena para compartir.",
                ingredients =
                    listOf(
                        Ingredient(name = "Carne picada", quantity = "500 g", aisle = Aisle.CARNICERIA.label),
                        Ingredient(name = "Tapas de empanada", quantity = "12", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Cebolla", quantity = "2", aisle = Aisle.VERDULERIA.label),
                        Ingredient(name = "Huevo duro", quantity = "2", aisle = Aisle.ALMACEN.label),
                    ),
            ),
            Meal(
                name = "Ñoquis con salsa",
                photoUri = asset("noquis"),
                timeMinutes = 45,
                servings = 4,
                category = MealCategory.COMIDA.label,
                notes = "Para el 29 de cada mes.",
                ingredients =
                    listOf(
                        Ingredient(name = "Papas", quantity = "1 kg", aisle = Aisle.VERDULERIA.label),
                        Ingredient(name = "Harina", quantity = "300 g", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Salsa de tomate", quantity = "1 lata", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Queso rallado", quantity = "100 g", aisle = Aisle.LACTEOS.label),
                    ),
            ),
            Meal(
                name = "Tira de asado",
                photoUri = asset("asado"),
                timeMinutes = 90,
                servings = 4,
                category = MealCategory.COMIDA.label,
                notes = "A fuego lento.",
                ingredients =
                    listOf(
                        Ingredient(name = "Tira de asado", quantity = "1.5 kg", aisle = Aisle.CARNICERIA.label),
                        Ingredient(name = "Sal gruesa", quantity = "c/n", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Chimichurri", quantity = "1 frasco", aisle = Aisle.ALMACEN.label),
                    ),
            ),
            Meal(
                name = "Tarta de verdura",
                photoUri = asset("tarta"),
                timeMinutes = 50,
                servings = 4,
                category = MealCategory.COMIDA.label,
                notes = "Liviana y rendidora.",
                ingredients =
                    listOf(
                        Ingredient(name = "Acelga", quantity = "1 atado", aisle = Aisle.VERDULERIA.label),
                        Ingredient(name = "Tapas de tarta", quantity = "2", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Huevos", quantity = "3", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Queso cremoso", quantity = "200 g", aisle = Aisle.LACTEOS.label),
                    ),
            ),
            Meal(
                name = "Guiso de lentejas",
                photoUri = asset("guiso"),
                timeMinutes = 70,
                servings = 6,
                category = MealCategory.COMIDA.label,
                notes = "Ideal para días fríos.",
                ingredients =
                    listOf(
                        Ingredient(name = "Lentejas", quantity = "500 g", aisle = Aisle.ALMACEN.label),
                        Ingredient(name = "Chorizo colorado", quantity = "2", aisle = Aisle.CARNICERIA.label),
                        Ingredient(name = "Zanahoria", quantity = "2", aisle = Aisle.VERDULERIA.label),
                        Ingredient(name = "Cebolla", quantity = "1", aisle = Aisle.VERDULERIA.label),
                    ),
            ),
        )
}
