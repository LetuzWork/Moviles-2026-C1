package com.menusemana.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class MetricConverterTest {

    // ── cups ─────────────────────────────────────────────────────────────────

    @Test fun `1 cup converts to 240 ml`() =
        assertEquals("240 ml", MetricConverter.convert("1 cup"))

    @Test fun `2 cups converts to 480 ml`() =
        assertEquals("480 ml", MetricConverter.convert("2 cups"))

    @Test fun `half cup fraction converts correctly`() =
        assertEquals("120 ml", MetricConverter.convert("1/2 cup"))

    @Test fun `quarter cup converts correctly`() =
        assertEquals("60 ml", MetricConverter.convert("1/4 cup"))

    @Test fun `mixed number one and a half cups converts correctly`() =
        assertEquals("360 ml", MetricConverter.convert("1 1/2 cups"))

    @Test fun `5 cups exceeds 1 litre and converts to l`() =
        assertEquals("1 l", MetricConverter.convert("5 cups"))

    // ── tablespoon / teaspoon ─────────────────────────────────────────────────

    @Test fun `1 tbsp converts to 15 ml`() =
        assertEquals("15 ml", MetricConverter.convert("1 tbsp"))

    @Test fun `2 tablespoons converts to 30 ml`() =
        assertEquals("30 ml", MetricConverter.convert("2 tablespoons"))

    @Test fun `1 tsp converts to 5 ml`() =
        assertEquals("5 ml", MetricConverter.convert("1 tsp"))

    @Test fun `half tsp converts to 3 ml`() =
        assertEquals("3 ml", MetricConverter.convert("1/2 tsp"))

    // ── oz / lb ───────────────────────────────────────────────────────────────

    @Test fun `1 oz converts to 28 g`() =
        assertEquals("28 g", MetricConverter.convert("1 oz"))

    @Test fun `2 oz converts to 56 g`() =
        assertEquals("56 g", MetricConverter.convert("2 oz"))

    @Test fun `1 lb converts to 450 g`() =
        assertEquals("450 g", MetricConverter.convert("1 lb"))

    @Test fun `3 lb exceeds 1 kg`() =
        assertEquals("1 kg", MetricConverter.convert("3 lb"))

    @Test fun `1 pound converts same as lb`() =
        assertEquals("450 g", MetricConverter.convert("1 pound"))

    // ── fluid oz / pint / quart ───────────────────────────────────────────────

    @Test fun `1 fl oz converts to 30 ml`() =
        assertEquals("30 ml", MetricConverter.convert("1 fl oz"))

    @Test fun `1 pint converts to 475 ml`() =
        assertEquals("475 ml", MetricConverter.convert("1 pint"))

    @Test fun `3 pints exceeds 1 litre`() =
        assertEquals("1 l", MetricConverter.convert("3 pints"))

    @Test fun `1 quart converts to 950 ml`() =
        assertEquals("950 ml", MetricConverter.convert("1 quart"))

    // ── stick ─────────────────────────────────────────────────────────────────

    @Test fun `1 stick of butter converts to 115 g`() =
        assertEquals("115 g", MetricConverter.convert("1 stick"))

    // ── passthrough cases ─────────────────────────────────────────────────────

    @Test fun `empty string returns empty string`() =
        assertEquals("", MetricConverter.convert(""))

    @Test fun `blank whitespace returns blank`() =
        assertEquals("", MetricConverter.convert("   "))

    @Test fun `unknown unit is returned unchanged`() =
        assertEquals("to taste", MetricConverter.convert("to taste"))

    @Test fun `plain number with no unit is returned unchanged`() =
        assertEquals("2", MetricConverter.convert("2"))

    @Test fun `already metric grams are returned unchanged`() =
        assertEquals("200g", MetricConverter.convert("200g"))
}
