package com.menusemana.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientTranslatorTest {

    // ── translate() ───────────────────────────────────────────────────────────

    @Test fun `known ingredient returns spanish translation`() =
        assertEquals("Pollo", IngredientTranslator.translate("chicken"))

    @Test fun `known ingredient with capital letter is found`() =
        assertEquals("Ajo", IngredientTranslator.translate("Garlic"))

    @Test fun `known ingredient all caps is found`() =
        assertEquals("Sal", IngredientTranslator.translate("SALT"))

    @Test fun `unknown ingredient returns original name`() =
        assertEquals("xyzunknown", IngredientTranslator.translate("xyzunknown"))

    @Test fun `leading and trailing whitespace is trimmed`() =
        assertEquals("Pollo", IngredientTranslator.translate("  chicken  "))

    @Test fun `compound ingredient name translates correctly`() =
        assertEquals("Aceite de oliva", IngredientTranslator.translate("olive oil"))

    @Test fun `tomatoes translates correctly`() =
        assertEquals("Tomates", IngredientTranslator.translate("tomatoes"))

    @Test fun `onion translates correctly`() =
        assertEquals("Cebolla", IngredientTranslator.translate("onion"))

    @Test fun `butter translates correctly`() =
        assertEquals("Mantequilla", IngredientTranslator.translate("butter"))

    @Test fun `eggs translates correctly`() =
        assertEquals("Huevos", IngredientTranslator.translate("eggs"))

    // ── translateArea() ───────────────────────────────────────────────────────

    @Test fun `italian area translates to Italiana`() =
        assertEquals("Italiana", IngredientTranslator.translateArea("Italian"))

    @Test fun `mexican area translates to Mexicana`() =
        assertEquals("Mexicana", IngredientTranslator.translateArea("Mexican"))

    @Test fun `area is case insensitive`() =
        assertEquals("Francesa", IngredientTranslator.translateArea("FRENCH"))

    @Test fun `unknown area returns original value`() =
        assertEquals("Atlantean", IngredientTranslator.translateArea("Atlantean"))

    // ── translateCategory() ───────────────────────────────────────────────────

    @Test fun `beef category translates to Carne vacuna`() =
        assertEquals("Carne vacuna", IngredientTranslator.translateCategory("beef"))

    @Test fun `dessert category translates to Postre`() =
        assertEquals("Postre", IngredientTranslator.translateCategory("Dessert"))

    @Test fun `vegetarian category translates`() =
        assertEquals("Vegetariana", IngredientTranslator.translateCategory("Vegetarian"))

    @Test fun `unknown category returns original value`() =
        assertEquals("Fusion", IngredientTranslator.translateCategory("Fusion"))
}
