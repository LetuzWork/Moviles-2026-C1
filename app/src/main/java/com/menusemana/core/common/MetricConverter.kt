package com.menusemana.core.common

object MetricConverter {

    private val MIXED = Regex("""^(\d+)\s+(\d+)/(\d+)(.*)$""")
    private val FRACTION = Regex("""^(\d+)/(\d+)(.*)$""")
    private val DECIMAL = Regex("""^(\d+(?:\.\d+)?)(.*)$""")

    fun convert(raw: String): String {
        val s = raw.trim()
        if (s.isBlank()) return s

        val (amount, rest) = parseAmount(s) ?: return s
        val unit = rest.trim().lowercase()

        return when {
            unit.startsWith("cup")                              -> metric(amount * 240, "ml")
            unit.startsWith("tbsp") || unit.startsWith("tablespoon") -> metric(amount * 15, "ml")
            unit.startsWith("tsp")  || unit.startsWith("teaspoon")   -> metric(amount * 5, "ml")
            unit.startsWith("fl oz") || unit.startsWith("fluid ounce") -> metric(amount * 30, "ml")
            unit.startsWith("pint")                            -> metric(amount * 475, "ml")
            unit.startsWith("quart")                           -> metric(amount * 950, "ml")
            unit.startsWith("gallon")                          -> metric(amount * 3800, "ml")
            unit.startsWith("oz") || unit.startsWith("ounce") -> metric(amount * 28, "g")
            unit.startsWith("lb") || unit.startsWith("pound") -> metric(amount * 450, "g")
            unit.startsWith("stick")                           -> metric(amount * 115, "g")
            else -> s
        }
    }

    private fun parseAmount(s: String): Pair<Double, String>? {
        MIXED.find(s)?.let { m ->
            val whole = m.groupValues[1].toDouble()
            val num   = m.groupValues[2].toDouble()
            val den   = m.groupValues[3].toDouble()
            return (whole + num / den) to m.groupValues[4]
        }
        FRACTION.find(s)?.let { m ->
            val num = m.groupValues[1].toDouble()
            val den = m.groupValues[2].toDouble()
            return (num / den) to m.groupValues[3]
        }
        DECIMAL.find(s)?.let { m ->
            return m.groupValues[1].toDouble() to m.groupValues[2]
        }
        return null
    }

    private fun metric(value: Double, baseUnit: String): String {
        val (scaled, unit) = when (baseUnit) {
            "ml" -> if (value >= 1000) value / 1000 to "l" else value to "ml"
            "g"  -> if (value >= 1000) value / 1000 to "kg" else value to "g"
            else -> value to baseUnit
        }
        val formatted = if (scaled == scaled.toLong().toDouble()) {
            scaled.toLong().toString()
        } else {
            "%.0f".format(scaled)
        }
        return "$formatted $unit"
    }
}
