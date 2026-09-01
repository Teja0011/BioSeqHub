package com.example.biokt.alignment

import kotlin.math.max

data class BioKtAlignment(
    val type: String,
    val queryAligned: String,
    val targetAligned: String,
    val matchBar: String,
    val score: Int,
    val identityPercent: Double,
    val similarityPercent: Double,
    val gapsCount: Int,
    val alignmentLength: Int
)

/**
 * Needleman-Wunsch Global Sequence Alignment with custom substitution scoring
 */
object NeedlemanWunsch {

    fun align(
        seq1: String,
        seq2: String,
        isProtein: Boolean = false,
        gapPenalty: Int = -2
    ): BioKtAlignment {
        val s1 = seq1.uppercase().trim()
        val s2 = seq2.uppercase().trim()
        val n = s1.length
        val m = s2.length

        if (n == 0 || m == 0) {
            return BioKtAlignment(
                type = "Global (Needleman-Wunsch)",
                queryAligned = s1,
                targetAligned = s2,
                matchBar = "",
                score = 0,
                identityPercent = 0.0,
                similarityPercent = 0.0,
                gapsCount = 0,
                alignmentLength = 0
            )
        }

        val matrix = Array(n + 1) { IntArray(m + 1) }

        for (i in 0..n) matrix[i][0] = i * gapPenalty
        for (j in 0..m) matrix[0][j] = j * gapPenalty

        for (i in 1..n) {
            for (j in 1..m) {
                val subScore = if (isProtein) {
                    SubstitutionMatrix.scoreBlosum62(s1[i - 1], s2[j - 1])
                } else {
                    SubstitutionMatrix.scoreDna(s1[i - 1], s2[j - 1])
                }

                val match = matrix[i - 1][j - 1] + subScore
                val delete = matrix[i - 1][j] + gapPenalty
                val insert = matrix[i][j - 1] + gapPenalty
                matrix[i][j] = max(match, max(delete, insert))
            }
        }

        val align1 = StringBuilder()
        val align2 = StringBuilder()
        val bar = StringBuilder()

        var i = n
        var j = m
        var matches = 0
        var similarities = 0
        var gaps = 0

        while (i > 0 || j > 0) {
            val subScore = if (i > 0 && j > 0) {
                if (isProtein) SubstitutionMatrix.scoreBlosum62(s1[i - 1], s2[j - 1])
                else SubstitutionMatrix.scoreDna(s1[i - 1], s2[j - 1])
            } else 0

            if (i > 0 && j > 0 && matrix[i][j] == matrix[i - 1][j - 1] + subScore) {
                align1.append(s1[i - 1])
                align2.append(s2[j - 1])
                if (s1[i - 1] == s2[j - 1]) {
                    bar.append("|")
                    matches++
                    similarities++
                } else if (isProtein && subScore > 0) {
                    bar.append(":")
                    similarities++
                } else {
                    bar.append(".")
                }
                i--
                j--
            } else if (i > 0 && matrix[i][j] == matrix[i - 1][j] + gapPenalty) {
                align1.append(s1[i - 1])
                align2.append("-")
                bar.append(" ")
                gaps++
                i--
            } else {
                align1.append("-")
                align2.append(s2[j - 1])
                bar.append(" ")
                gaps++
                j--
            }
        }

        val qRev = align1.reverse().toString()
        val tRev = align2.reverse().toString()
        val bRev = bar.reverse().toString()
        val len = qRev.length
        val identity = if (len > 0) (matches.toDouble() / len) * 100.0 else 0.0
        val similarity = if (len > 0) (similarities.toDouble() / len) * 100.0 else 0.0

        return BioKtAlignment(
            type = "Global (Needleman-Wunsch)",
            queryAligned = qRev,
            targetAligned = tRev,
            matchBar = bRev,
            score = matrix[n][m],
            identityPercent = identity,
            similarityPercent = similarity,
            gapsCount = gaps,
            alignmentLength = len
        )
    }
}
