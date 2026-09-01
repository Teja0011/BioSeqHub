package com.example.biokt.analysis

import kotlin.math.roundToInt

data class Primer(
    val type: String, // "Forward (5'->3')" or "Reverse (5'->3')"
    val sequence: String,
    val length: Int,
    val meltingTempTm: Double,
    val gcPercent: Double,
    val startPosition: Int,
    val endPosition: Int,
    val gcClamp: Boolean
)

data class PcrPrimerPair(
    val forwardPrimer: Primer,
    val reversePrimer: Primer,
    val ampliconLength: Int,
    val tmDifference: Double,
    val ampliconSequence: String
)

/**
 * PCR Primer Designer with thermodynamic melting temperature (Tm) in BioKt
 */
object PrimerDesigner {

    fun calculateTm(primerSequence: String): Double {
        val clean = primerSequence.uppercase().filter { it in "ATGC" }
        if (clean.length < 14) {
            // Wallace Rule for short oligos: Tm = 2(A+T) + 4(G+C)
            val wA = clean.count { it == 'A' }
            val xT = clean.count { it == 'T' }
            val yG = clean.count { it == 'G' }
            val zC = clean.count { it == 'C' }
            return 2.0 * (wA + xT) + 4.0 * (yG + zC)
        } else {
            // SantaLucia nearest-neighbor approximation
            val wA = clean.count { it == 'A' }
            val xT = clean.count { it == 'T' }
            val yG = clean.count { it == 'G' }
            val zC = clean.count { it == 'C' }
            return 64.9 + 41.0 * ((yG + zC - 16.4) / (wA + xT + yG + zC))
        }
    }

    fun calculateGc(primerSequence: String): Double {
        val clean = primerSequence.uppercase().filter { it in "ATGC" }
        if (clean.isEmpty()) return 0.0
        val gc = clean.count { it == 'G' || it == 'C' }
        return (gc.toDouble() / clean.length) * 100.0
    }

    fun designPrimers(
        dnaSequence: String,
        primerLength: Int = 20,
        targetAmpliconMinLen: Int = 150,
        targetAmpliconMaxLen: Int = 500
    ): List<PcrPrimerPair> {
        val clean = dnaSequence.uppercase().filter { it in "ATGC" }
        if (clean.length < targetAmpliconMinLen + primerLength) return emptyList()

        val pairs = mutableListOf<PcrPrimerPair>()
        val comp = mapOf('A' to 'T', 'T' to 'A', 'G' to 'C', 'C' to 'G')

        // Search forward primer candidates near the 5' region
        val fwdCandidates = mutableListOf<Primer>()
        for (i in 0 until (clean.length / 3).coerceAtMost(200)) {
            if (i + primerLength > clean.length) break
            val fSeq = clean.substring(i, i + primerLength)
            val tm = calculateTm(fSeq)
            val gc = calculateGc(fSeq)
            val gcClamp = fSeq.last() == 'G' || fSeq.last() == 'C'
            if (tm in 52.0..68.0 && gc in 40.0..60.0) {
                fwdCandidates.add(
                    Primer(
                        type = "Forward (5'->3')",
                        sequence = fSeq,
                        length = primerLength,
                        meltingTempTm = tm,
                        gcPercent = gc,
                        startPosition = i + 1,
                        endPosition = i + primerLength,
                        gcClamp = gcClamp
                    )
                )
            }
        }

        // Search reverse primer candidates
        for (fwd in fwdCandidates) {
            val minRevStart = fwd.endPosition + targetAmpliconMinLen
            val maxRevStart = (fwd.startPosition + targetAmpliconMaxLen).coerceAtMost(clean.length - primerLength)

            if (minRevStart >= maxRevStart) continue

            for (j in minRevStart..maxRevStart step 10) {
                if (j + primerLength > clean.length) break
                val templateSub = clean.substring(j, j + primerLength)
                // Reverse complement of the template strand is the 5'->3' reverse primer
                val revSeq = templateSub.reversed().map { comp[it] ?: it }.joinToString("")
                val tm = calculateTm(revSeq)
                val gc = calculateGc(revSeq)
                val tmDiff = kotlin.math.abs(fwd.meltingTempTm - tm)

                if (tm in 52.0..68.0 && gc in 40.0..60.0 && tmDiff <= 3.0) {
                    val revPrimer = Primer(
                        type = "Reverse (5'->3')",
                        sequence = revSeq,
                        length = primerLength,
                        meltingTempTm = tm,
                        gcPercent = gc,
                        startPosition = j + 1,
                        endPosition = j + primerLength,
                        gcClamp = revSeq.last() == 'G' || revSeq.last() == 'C'
                    )

                    val amplicon = clean.substring(fwd.startPosition - 1, revPrimer.endPosition)
                    pairs.add(
                        PcrPrimerPair(
                            forwardPrimer = fwd,
                            reversePrimer = revPrimer,
                            ampliconLength = amplicon.length,
                            tmDifference = tmDiff,
                            ampliconSequence = amplicon
                        )
                    )
                    if (pairs.size >= 5) return pairs
                }
            }
        }

        return pairs
    }
}
