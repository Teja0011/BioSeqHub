package com.example.biokt.alignment

import kotlin.math.max

/**
 * Smith-Waterman Local Sequence Alignment in BioKt
 */
object SmithWaterman {

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
                type = "Local (Smith-Waterman)",
                queryAligned = "",
                targetAligned = "",
                matchBar = "",
                score = 0,
                identityPercent = 0.0,
                similarityPercent = 0.0,
                gapsCount = 0,
                alignmentLength = 0
            )
        }

        val matrix = Array(n + 1) { IntArray(m + 1) }
        var maxScore = 0
        var maxI = 0
        var maxJ = 0

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
                val current = max(0, max(match, max(delete, insert)))
                matrix[i][j] = current

                if (current > maxScore) {
                    maxScore = current
                    maxI = i
                    maxJ = j
                }
            }
        }

        val align1 = StringBuilder()
        val align2 = StringBuilder()
        val bar = StringBuilder()

        var i = maxI
        var j = maxJ
        var matches = 0
        var similarities = 0
        var gaps = 0

        while (i > 0 && j > 0 && matrix[i][j] > 0) {
            val subScore = if (isProtein) SubstitutionMatrix.scoreBlosum62(s1[i - 1], s2[j - 1])
            else SubstitutionMatrix.scoreDna(s1[i - 1], s2[j - 1])

            if (matrix[i][j] == matrix[i - 1][j - 1] + subScore) {
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
            } else if (matrix[i][j] == matrix[i - 1][j] + gapPenalty) {
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
            type = "Local (Smith-Waterman)",
            queryAligned = qRev,
            targetAligned = tRev,
            matchBar = bRev,
            score = maxScore,
            identityPercent = identity,
            similarityPercent = similarity,
            gapsCount = gaps,
            alignmentLength = len
        )
    }
}
