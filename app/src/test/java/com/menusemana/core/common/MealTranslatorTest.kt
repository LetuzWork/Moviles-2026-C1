package com.menusemana.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MealTranslatorTest {

    // ── translate() ───────────────────────────────────────────────────────────

    @Test fun `known ingredient returns spanish translation`() =
        assertEquals("Pollo", MealTranslator.translate("chicken"))

    @Test fun `known ingredient with capital letter is found`() =
        assertEquals("Ajo", MealTranslator.translate("Garlic"))

    @Test fun `known ingredient all caps is found`() =
        assertEquals("Sal", MealTranslator.translate("SALT"))

    @Test fun `unknown ingredient returns original name`() =
        assertEquals("xyzunknown", MealTranslator.translate("xyzunknown"))

    @Test fun `leading and trailing whitespace is trimmed`() =
        assertEquals("Pollo", MealTranslator.translate("  chicken  "))

    @Test fun `compound ingredient name translates correctly`() =
        assertEquals("Aceite de oliva", MealTranslator.translate("olive oil"))

    @Test fun `tomatoes translates correctly`() =
        assertEquals("Tomates", MealTranslator.translate("tomatoes"))

    @Test fun `onion translates correctly`() =
        assertEquals("Cebolla", MealTranslator.translate("onion"))

    @Test fun `butter translates correctly`() =
        assertEquals("Mantequilla", MealTranslator.translate("butter"))

    @Test fun `eggs translates correctly`() =
        assertEquals("Huevos", MealTranslator.translate("eggs"))

    // ── translateArea() ───────────────────────────────────────────────────────

    @Test fun `italian area translates to Italiana`() =
        assertEquals("Italiana", MealTranslator.translateArea("Italian"))

    @Test fun `mexican area translates to Mexicana`() =
        assertEquals("Mexicana", MealTranslator.translateArea("Mexican"))

    @Test fun `area is case insensitive`() =
        assertEquals("Francesa", MealTranslator.translateArea("FRENCH"))

    @Test fun `unknown area returns original value`() =
        assertEquals("Atlantean", MealTranslator.translateArea("Atlantean"))

    // ── translateCategory() ───────────────────────────────────────────────────

    @Test fun `beef category translates to Carne vacuna`() =
        assertEquals("Carne vacuna", MealTranslator.translateCategory("beef"))

    @Test fun `dessert category translates to Postre`() =
        assertEquals("Postre", MealTranslator.translateCategory("Dessert"))

    @Test fun `vegetarian category translates`() =
        assertEquals("Vegetariana", MealTranslator.translateCategory("Vegetarian"))

    @Test fun `unknown category returns original value`() =
        assertEquals("Fusion", MealTranslator.translateCategory("Fusion"))

    // ── translateMeasure() — unit conversion ──────────────────────────────────

    @Test fun `1 cup converts to 240 ml`() =
        assertEquals("240 ml", MealTranslator.translateMeasure("1 cup"))

    @Test fun `2 cups converts to 480 ml`() =
        assertEquals("480 ml", MealTranslator.translateMeasure("2 cups"))

    @Test fun `half cup fraction converts correctly`() =
        assertEquals("120 ml", MealTranslator.translateMeasure("1/2 cup"))

    @Test fun `quarter cup converts correctly`() =
        assertEquals("60 ml", MealTranslator.translateMeasure("1/4 cup"))

    @Test fun `mixed number one and a half cups converts correctly`() =
        assertEquals("360 ml", MealTranslator.translateMeasure("1 1/2 cups"))

    @Test fun `5 cups exceeds 1 litre and converts to l`() =
        assertEquals("1 l", MealTranslator.translateMeasure("5 cups"))

    @Test fun `1 tbsp converts to 15 ml`() =
        assertEquals("15 ml", MealTranslator.translateMeasure("1 tbsp"))

    @Test fun `2 tablespoons converts to 30 ml`() =
        assertEquals("30 ml", MealTranslator.translateMeasure("2 tablespoons"))

    @Test fun `1 tsp converts to 5 ml`() =
        assertEquals("5 ml", MealTranslator.translateMeasure("1 tsp"))

    @Test fun `half tsp converts to 3 ml`() =
        assertEquals("3 ml", MealTranslator.translateMeasure("1/2 tsp"))

    @Test fun `1 oz converts to 28 g`() =
        assertEquals("28 g", MealTranslator.translateMeasure("1 oz"))

    @Test fun `2 oz converts to 56 g`() =
        assertEquals("56 g", MealTranslator.translateMeasure("2 oz"))

    @Test fun `1 lb converts to 450 g`() =
        assertEquals("450 g", MealTranslator.translateMeasure("1 lb"))

    @Test fun `3 lb exceeds 1 kg`() =
        assertEquals("1 kg", MealTranslator.translateMeasure("3 lb"))

    @Test fun `1 pound converts same as lb`() =
        assertEquals("450 g", MealTranslator.translateMeasure("1 pound"))

    @Test fun `1 fl oz converts to 30 ml`() =
        assertEquals("30 ml", MealTranslator.translateMeasure("1 fl oz"))

    @Test fun `1 pint converts to 475 ml`() =
        assertEquals("475 ml", MealTranslator.translateMeasure("1 pint"))

    @Test fun `3 pints exceeds 1 litre`() =
        assertEquals("1 l", MealTranslator.translateMeasure("3 pints"))

    @Test fun `1 quart converts to 950 ml`() =
        assertEquals("950 ml", MealTranslator.translateMeasure("1 quart"))

    @Test fun `1 stick of butter converts to 115 g`() =
        assertEquals("115 g", MealTranslator.translateMeasure("1 stick"))

    @Test fun `empty string returns empty string`() =
        assertEquals("", MealTranslator.translateMeasure(""))

    @Test fun `blank whitespace returns blank`() =
        assertEquals("", MealTranslator.translateMeasure("   "))

    @Test fun `unknown unit is returned unchanged`() =
        assertEquals("to taste", MealTranslator.translateMeasure("to taste"))

    @Test fun `plain number with no unit is returned unchanged`() =
        assertEquals("2", MealTranslator.translateMeasure("2"))

    @Test fun `already metric grams are returned unchanged`() =
        assertEquals("200g", MealTranslator.translateMeasure("200g"))
}
