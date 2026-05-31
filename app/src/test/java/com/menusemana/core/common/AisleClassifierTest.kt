package com.menusemana.core.common

import com.menusemana.domain.model.Aisle
import org.junit.Assert.assertEquals
import org.junit.Test

class AisleClassifierTest {

    // ── Verdulería ────────────────────────────────────────────────────────────

    @Test fun `tomato is verduleria`() =
        assertEquals(Aisle.VERDULERIA.label, AisleClassifier.classify("tomato"))

    @Test fun `garlic is verduleria`() =
        assertEquals(Aisle.VERDULERIA.label, AisleClassifier.classify("garlic"))

    @Test fun `spinach is verduleria`() =
        assertEquals(Aisle.VERDULERIA.label, AisleClassifier.classify("spinach"))

    @Test fun `compound name with keyword is verduleria`() =
        assertEquals(Aisle.VERDULERIA.label, AisleClassifier.classify("cherry tomatoes"))

    // ── Carnicería ────────────────────────────────────────────────────────────

    @Test fun `chicken is carniceria`() =
        assertEquals(Aisle.CARNICERIA.label, AisleClassifier.classify("chicken"))

    @Test fun `beef is carniceria`() =
        assertEquals(Aisle.CARNICERIA.label, AisleClassifier.classify("beef"))

    @Test fun `salmon is carniceria`() =
        assertEquals(Aisle.CARNICERIA.label, AisleClassifier.classify("salmon"))

    @Test fun `chicken breast compound name is carniceria`() =
        assertEquals(Aisle.CARNICERIA.label, AisleClassifier.classify("chicken breast"))

    // ── Lácteos ───────────────────────────────────────────────────────────────

    @Test fun `milk is lacteos`() =
        assertEquals(Aisle.LACTEOS.label, AisleClassifier.classify("milk"))

    @Test fun `butter is lacteos`() =
        assertEquals(Aisle.LACTEOS.label, AisleClassifier.classify("butter"))

    @Test fun `eggs is lacteos`() =
        assertEquals(Aisle.LACTEOS.label, AisleClassifier.classify("eggs"))

    @Test fun `parmesan cheese is lacteos`() =
        assertEquals(Aisle.LACTEOS.label, AisleClassifier.classify("parmesan cheese"))

    // ── Almacén (default) ─────────────────────────────────────────────────────

    @Test fun `flour is almacen`() =
        assertEquals(Aisle.ALMACEN.label, AisleClassifier.classify("flour"))

    @Test fun `sugar is almacen`() =
        assertEquals(Aisle.ALMACEN.label, AisleClassifier.classify("sugar"))

    @Test fun `olive oil is almacen`() =
        assertEquals(Aisle.ALMACEN.label, AisleClassifier.classify("olive oil"))

    @Test fun `unknown ingredient defaults to almacen`() =
        assertEquals(Aisle.ALMACEN.label, AisleClassifier.classify("xyzabc"))

    // ── Case insensitivity ────────────────────────────────────────────────────

    @Test fun `classification is case insensitive`() =
        assertEquals(Aisle.CARNICERIA.label, AisleClassifier.classify("CHICKEN BREAST"))

    @Test fun `mixed case works`() =
        assertEquals(Aisle.VERDULERIA.label, AisleClassifier.classify("Fresh Tomatoes"))
}
