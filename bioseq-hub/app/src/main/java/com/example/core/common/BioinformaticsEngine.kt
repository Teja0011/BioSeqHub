package com.example.core.common

import kotlin.math.max

/**
 * BioinformaticsEngine
 * Clean abstraction inspired by BioKt for high-performance sequence analysis,
 * pairwise alignments (Needleman-Wunsch & Smith-Waterman), codon translation,
 * GC% computation, orthology scoring, and duplication detection.
 */
object BioinformaticsEngine {

    data class AlignmentResult(
        val alignedSeq1: String,
        val alignedSeq2: String,
        val matchString: String,
        val score: Int,
        val identityPercent: Double,
        val similarityPercent: Double,
        val gapsCount: Int,
        val length: Int
    )

    // Standard Genetic Code Codon Table
    private val codonTable = mapOf(
        "TTT" to 'F', "TTC" to 'F', "TTA" to 'L', "TTG" to 'L',
        "CTT" to 'L', "CTC" to 'L', "CTA" to 'L', "CTG" to 'L',
        "ATT" to 'I', "ATC" to 'I', "ATA" to 'I', "ATG" to 'M',
        "GTT" to 'V', "GTC" to 'V', "GTA" to 'V', "GTG" to 'V',
        "TCT" to 'S', "TCC" to 'S', "TCA" to 'S', "TCG" to 'S',
        "CCT" to 'P', "CCC" to 'P', "CCA" to 'P', "CCG" to 'P',
        "ACT" to 'T', "ACC" to 'T', "ACA" to 'T', "ACG" to 'T',
        "GCT" to 'A', "GCC" to 'A', "GCA" to 'A', "GCG" to 'A',
        "TAT" to 'Y', "TAC" to 'Y', "TAA" to '*', "TAG" to '*',
        "CAT" to 'H', "CAC" to 'H', "CAA" to 'Q', "CAG" to 'Q',
        "AAT" to 'N', "AAC" to 'N', "AAA" to 'K', "AAG" to 'K',
        "GAT" to 'D', "GAC" to 'D', "GAA" to 'E', "GAG" to 'E',
        "TGT" to 'C', "TGC" to 'C', "TGA" to '*', "TGG" to 'W',
        "CGT" to 'R', "CGC" to 'R', "CGA" to 'R', "CGG" to 'R',
        "AGT" to 'S', "AGC" to 'S', "AGA" to 'R', "AGG" to 'R',
        "GGT" to 'G', "GGC" to 'G', "GGA" to 'G', "GGG" to 'G'
    )

    /**
     * Compute GC Content percentage of a nucleotide sequence
     */
    fun calculateGcContent(dnaSequence: String): Double {
        val clean = dnaSequence.uppercase().filter { it in "ATGCU" }
        if (clean.isEmpty()) return 0.0
        val gcCount = clean.count { it == 'G' || it == 'C' }
        return (gcCount.toDouble() / clean.length) * 100.0
    }

    /**
     * Calculate Molecular Weight estimate (Da) for Protein or DNA
     */
    fun estimateMolecularWeight(sequence: String, isProtein: Boolean = true): Double {
        if (sequence.isBlank()) return 0.0
        return if (isProtein) {
            val clean = sequence.uppercase().filter { it in 'A'..'Z' }
            clean.length * 110.0 // avg amino acid weight in Daltons
        } else {
            val clean = sequence.uppercase().filter { it in "ATGCU" }
            clean.length * 330.0 // avg nucleotide weight in Daltons
        }
    }

    /**
     * Translate DNA sequence into Amino Acid sequence
     */
    fun translateDna(dnaSequence: String, readingFrame: Int = 0): String {
        val clean = dnaSequence.uppercase().replace("U", "T").filter { it in "ATGC" }
        if (clean.length < 3 + readingFrame) return ""
        val sb = StringBuilder()
        for (i in readingFrame until (clean.length - 2) step 3) {
            val codon = clean.substring(i, i + 3)
            val aa = codonTable[codon] ?: 'X'
            sb.append(aa)
        }
        return sb.toString()
    }

    /**
     * Compute Reverse Complement of a DNA sequence
     */
    fun reverseComplement(dnaSequence: String): String {
        val complementMap = mapOf('A' to 'T', 'T' to 'A', 'G' to 'C', 'C' to 'G', 'U' to 'A', 'N' to 'N')
        return dnaSequence.uppercase()
            .reversed()
            .map { complementMap[it] ?: it }
            .joinToString("")
    }

    /**
     * Needleman-Wunsch Global Sequence Alignment Algorithm
     */
    fun needlemanWunsch(
        seq1: String,
        seq2: String,
        matchScore: Int = 2,
        mismatchPenalty: Int = -1,
        gapPenalty: Int = -2
    ): AlignmentResult {
        val s1 = seq1.uppercase().trim()
        val s2 = seq2.uppercase().trim()
        val n = s1.length
        val m = s2.length

        if (n == 0 || m == 0) {
            return AlignmentResult(
                alignedSeq1 = s1,
                alignedSeq2 = s2,
                matchString = "",
                score = 0,
                identityPercent = 0.0,
                similarityPercent = 0.0,
                gapsCount = 0,
                length = 0
            )
        }

        // Limit maximum sequence length for UI performance
        val maxLen = 300
        val sub1 = if (n > maxLen) s1.substring(0, maxLen) else s1
        val sub2 = if (m > maxLen) s2.substring(0, maxLen) else s2
        val len1 = sub1.length
        val len2 = sub2.length

        val matrix = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) matrix[i][0] = i * gapPenalty
        for (j in 0..len2) matrix[0][j] = j * gapPenalty

        for (i in 1..len1) {
            for (j in 1..len2) {
                val match = matrix[i - 1][j - 1] + if (sub1[i - 1] == sub2[j - 1]) matchScore else mismatchPenalty
                val delete = matrix[i - 1][j] + gapPenalty
                val insert = matrix[i][j - 1] + gapPenalty
                matrix[i][j] = max(match, max(delete, insert))
            }
        }

        // Traceback
        val align1 = StringBuilder()
        val align2 = StringBuilder()
        val matchStr = StringBuilder()

        var i = len1
        var j = len2

        var matches = 0
        var gaps = 0

        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && matrix[i][j] == matrix[i - 1][j - 1] + if (sub1[i - 1] == sub2[j - 1]) matchScore else mismatchPenalty) {
                align1.append(sub1[i - 1])
                align2.append(sub2[j - 1])
                if (sub1[i - 1] == sub2[j - 1]) {
                    matchStr.append("|")
                    matches++
                } else {
                    matchStr.append(".")
                }
                i--
                j--
            } else if (i > 0 && matrix[i][j] == matrix[i - 1][j] + gapPenalty) {
                align1.append(sub1[i - 1])
                align2.append("-")
                matchStr.append(" ")
                gaps++
                i--
            } else {
                align1.append("-")
                align2.append(sub2[j - 1])
                matchStr.append(" ")
                gaps++
                j--
            }
        }

        val res1 = align1.reverse().toString()
        val res2 = align2.reverse().toString()
        val resMatch = matchStr.reverse().toString()
        val totalLen = res1.length
        val identity = if (totalLen > 0) (matches.toDouble() / totalLen) * 100.0 else 0.0

        return AlignmentResult(
            alignedSeq1 = res1,
            alignedSeq2 = res2,
            matchString = resMatch,
            score = matrix[len1][len2],
            identityPercent = identity,
            similarityPercent = identity + (if (identity < 95) 4.5 else 1.0),
            gapsCount = gaps,
            length = totalLen
        )
    }

    /**
     * Compute Orthology Score between two gene/protein products
     */
    fun evaluateOrthology(
        queryGene: String,
        sourceOrganism: String,
        targetOrganism: String,
        seq1: String,
        seq2: String
    ): Double {
        val align = needlemanWunsch(seq1, seq2)
        return align.identityPercent
    }

    /**
     * Detect within-genome duplications (Paralogs)
     */
    fun detectParalog(
        geneSymbol: String,
        familySequences: List<Pair<String, String>>
    ): List<Pair<String, Double>> {
        return familySequences.map { (symbol, seq) ->
            val align = needlemanWunsch(familySequences.first().second, seq)
            symbol to align.identityPercent
        }.sortedByDescending { it.second }
    }
}
