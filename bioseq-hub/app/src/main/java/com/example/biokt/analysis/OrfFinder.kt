package com.example.biokt.analysis

import com.example.biokt.sequence.CodonTable

data class Orf(
    val frame: Int, // +1, +2, +3, -1, -2, -3
    val startNucleotideIndex: Int,
    val endNucleotideIndex: Int,
    val nucleotideLength: Int,
    val proteinLength: Int,
    val nucleotideSequence: String,
    val translatedProtein: String
)

/**
 * 6-Frame Open Reading Frame (ORF) finder in BioKt
 */
object OrfFinder {

    fun findOrfs(
        dnaSequence: String,
        minProteinLength: Int = 20,
        codonTable: CodonTable = CodonTable.Standard
    ): List<Orf> {
        val cleanDna = dnaSequence.uppercase().filter { it in "ATGCUN" }
        if (cleanDna.length < 30) return emptyList()

        val results = mutableListOf<Orf>()

        // 1. Forward Frames: +1, +2, +3
        for (f in 0..2) {
            results.addAll(scanFrame(cleanDna, frame = f + 1, isForward = true, minLen = minProteinLength, codonTable = codonTable))
        }

        // 2. Reverse Complement Frames: -1, -2, -3
        val comp = mapOf('A' to 'T', 'T' to 'A', 'G' to 'C', 'C' to 'G', 'U' to 'A', 'N' to 'N')
        val revComp = cleanDna.reversed().map { comp[it] ?: it }.joinToString("")
        for (f in 0..2) {
            results.addAll(scanFrame(revComp, frame = -(f + 1), isForward = false, minLen = minProteinLength, codonTable = codonTable))
        }

        return results.sortedByDescending { it.proteinLength }
    }

    private fun scanFrame(
        dna: String,
        frame: Int,
        isForward: Boolean,
        minLen: Int,
        codonTable: CodonTable
    ): List<Orf> {
        val orfs = mutableListOf<Orf>()
        val startOffset = (kotlin.math.abs(frame) - 1).coerceIn(0, 2)
        var inOrf = false
        var orfStart = 0

        for (i in startOffset until (dna.length - 2) step 3) {
            val codon = dna.substring(i, i + 3)
            if (codonTable.isStartCodon(codon) && !inOrf) {
                inOrf = true
                orfStart = i
            } else if (codonTable.isStopCodon(codon) && inOrf) {
                val orfEnd = i + 3
                val nucSeq = dna.substring(orfStart, orfEnd)
                val protSb = StringBuilder()
                for (j in 0 until nucSeq.length - 2 step 3) {
                    protSb.append(codonTable.translate(nucSeq.substring(j, j + 3)))
                }
                val prot = protSb.toString()
                if (prot.length >= minLen) {
                    orfs.add(
                        Orf(
                            frame = frame,
                            startNucleotideIndex = orfStart + 1,
                            endNucleotideIndex = orfEnd,
                            nucleotideLength = nucSeq.length,
                            proteinLength = prot.length,
                            nucleotideSequence = nucSeq,
                            translatedProtein = prot
                        )
                    )
                }
                inOrf = false
            }
        }
        return orfs
    }
}
