package com.menusemana.core.common

import com.menusemana.domain.model.Aisle

object AisleClassifier {

    private val VERDULERIA = setOf(
        "tomato", "onion", "garlic", "pepper", "lemon", "lime", "orange", "apple",
        "carrot", "potato", "sweet potato", "spinach", "kale", "parsley", "cilantro",
        "coriander", "basil", "thyme", "rosemary", "oregano", "mint", "dill",
        "ginger", "celery", "lettuce", "cucumber", "zucchini", "courgette",
        "eggplant", "aubergine", "mushroom", "avocado", "broccoli", "cauliflower",
        "cabbage", "peas", "corn", "spring onion", "scallion", "leek", "shallot",
        "beetroot", "beet", "radish", "asparagus", "fennel", "artichoke",
        "pumpkin", "squash", "banana", "mango", "pineapple", "grape", "strawberry",
        "blueberry", "raspberry", "chili", "jalapeño", "habanero", "capsicum",
    )

    private val CARNICERIA = setOf(
        "chicken", "beef", "pork", "lamb", "turkey", "duck", "veal", "venison",
        "minced", "mince", "ground", "sausage", "chorizo", "salami", "bacon",
        "ham", "prosciutto", "pancetta", "fish", "salmon", "tuna", "cod",
        "tilapia", "sea bass", "trout", "mackerel", "herring", "anchovy",
        "sardine", "shrimp", "prawn", "crab", "lobster", "mussel", "clam",
        "squid", "octopus", "scallop", "steak", "ribs", "loin", "breast",
        "thigh", "wing", "leg", "fillet",
    )

    private val LACTEOS = setOf(
        "milk", "cream", "butter", "cheese", "yogurt", "yoghurt", "egg", "eggs",
        "mozzarella", "parmesan", "cheddar", "ricotta", "brie", "camembert",
        "feta", "gouda", "emmental", "gruyere", "sour cream", "whipping cream",
        "double cream", "heavy cream", "condensed milk", "evaporated milk",
        "ghee", "creme fraiche", "cream cheese",
    )

    fun classify(ingredient: String): String {
        val lower = ingredient.lowercase()
        return when {
            VERDULERIA.any { lower.contains(it) } -> Aisle.VERDULERIA.label
            CARNICERIA.any { lower.contains(it) } -> Aisle.CARNICERIA.label
            LACTEOS.any { lower.contains(it) }    -> Aisle.LACTEOS.label
            else                                   -> Aisle.ALMACEN.label
        }
    }
}
