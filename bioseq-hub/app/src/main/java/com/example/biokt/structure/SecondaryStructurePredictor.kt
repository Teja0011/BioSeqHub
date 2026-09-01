package com.example.biokt.structure

enum class SecondaryStructureType(val symbol: Char, val label: String) {
    HELIX('H', "Alpha Helix (H)"),
    SHEET('E', "Beta Strand (E)"),
    COIL('C', "Random Coil (C)")
}

data class SecondaryStructurePrediction(
    val sequence: String,
    val predictedStateString: String,
    val helixPercentage: Double,
    val sheetPercentage: Double,
    val coilPercentage: Double
)

/**
 * Chou-Fasman based secondary structure propensity predictor in BioKt
 */
object SecondaryStructurePredictor {

    // Chou-Fasman conformational parameters P(a) and P(b)
    private val helixPropensity = mapOf(
        'E' to 1.51, 'A' to 1.42, 'L' to 1.21, 'M' to 1.45, 'Q' to 1.11,
        'K' to 1.16, 'R' to 0.98, 'H' to 1.00, 'V' to 1.06, 'I' to 1.08,
        'D' to 1.01, 'T' to 0.83, 'S' to 0.77, 'C' to 0.70, 'Y' to 0.69,
        'N' to 0.67, 'F' to 1.13, 'W' to 1.08, 'G' to 0.57, 'P' to 0.57
    )

    private val sheetPropensity = mapOf(
        'M' to 1.05, 'V' to 1.70, 'I' to 1.60, 'C' to 1.19, 'Y' to 1.47,
        'F' to 1.38, 'Q' to 1.10, 'L' to 1.30, 'T' to 1.19, 'W' to 1.37,
        'A' to 0.83, 'R' to 0.93, 'G' to 0.75, 'D' to 0.54, 'K' to 0.74,
        'S' to 0.75, 'H' to 0.87, 'N' to 0.89, 'P' to 0.55, 'E' to 0.37
    )

    fun predict(proteinSequence: String): SecondaryStructurePrediction {
        val clean = proteinSequence.uppercase().filter { it in 'A'..'Z' }
        if (clean.isEmpty()) {
            return SecondaryStructurePrediction("", "", 0.0, 0.0, 0.0)
        }

        val states = StringBuilder()
        val window = 5

        for (i in clean.indices) {
            val start = (i - window / 2).coerceAtLeast(0)
            val end = (i + window / 2 + 1).coerceAtMost(clean.length)
            val sub = clean.substring(start, end)

            val avgH = sub.map { helixPropensity[it] ?: 1.0 }.average()
            val avgE = sub.map { sheetPropensity[it] ?: 1.0 }.average()

            when {
                avgH > 1.15 && avgH > avgE -> states.append('H')
                avgE > 1.15 && avgE > avgH -> states.append('E')
                else -> states.append('C')
            }
        }

        val stateStr = states.toString()
        val total = stateStr.length.toDouble()
        val hCount = stateStr.count { it == 'H' }
        val eCount = stateStr.count { it == 'E' }
        val cCount = stateStr.count { it == 'C' }

        return SecondaryStructurePrediction(
            sequence = clean,
            predictedStateString = stateStr,
            helixPercentage = (hCount / total) * 100.0,
            sheetPercentage = (eCount / total) * 100.0,
            coilPercentage = (cCount / total) * 100.0
        )
    }
}
