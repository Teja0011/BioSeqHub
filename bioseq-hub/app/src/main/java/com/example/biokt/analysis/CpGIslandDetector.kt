package com.example.biokt.analysis

data class CpGIsland(
    val start: Int,
    val end: Int,
    val length: Int,
    val gcPercent: Double,
    val obsExpRatio: Double,
    val sequence: String
)

/**
 * CpG Island Detector using Gardiner-Garden & Frommer criteria:
 * - Length >= 200 bp
 * - GC% >= 50%
 * - Observed/Expected CpG ratio >= 0.60
 */
object CpGIslandDetector {

    fun detect(
        dnaSequence: String,
        windowSize: Int = 200,
        stepSize: Int = 50,
        minGcPercent: Double = 50.0,
        minObsExpRatio: Double = 0.60
    ): List<CpGIsland> {
        val clean = dnaSequence.uppercase().filter { it in "ATGC" }
        if (clean.length < windowSize) return emptyList()

        val islands = mutableListOf<CpGIsland>()

        for (i in 0..(clean.length - windowSize) step stepSize) {
            val window = clean.substring(i, i + windowSize)
            val cCount = window.count { it == 'C' }
            val gCount = window.count { it == 'G' }
            val cgCount = countDinucleotide(window, "CG")

            val gc = ((cCount + gCount).toDouble() / windowSize) * 100.0
            val expectedCg = (cCount.toDouble() * gCount.toDouble()) / windowSize
            val obsExp = if (expectedCg > 0) cgCount.toDouble() / expectedCg else 0.0

            if (gc >= minGcPercent && obsExp >= minObsExpRatio) {
                islands.add(
                    CpGIsland(
                        start = i + 1,
                        end = i + windowSize,
                        length = windowSize,
                        gcPercent = gc,
                        obsExpRatio = obsExp,
                        sequence = window
                    )
                )
            }
        }
        return islands
    }

    private fun countDinucleotide(seq: String, dinuc: String): Int {
        var count = 0
        for (i in 0 until seq.length - 1) {
            if (seq.substring(i, i + 2) == dinuc) count++
        }
        return count
    }
}
