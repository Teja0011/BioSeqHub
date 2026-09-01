package com.example.biokt.sequence

import java.util.regex.Pattern

data class MotifMatch(
    val motifName: String,
    val pattern: String,
    val startIndex: Int,
    val endIndex: Int,
    val matchedSequence: String
)

/**
 * Motif Finder with IUPAC degenerate nucleotide search support
 */
object MotifFinder {

    private val iupacMap = mapOf(
        'A' to "A",
        'C' to "C",
        'G' to "G",
        'T' to "T",
        'U' to "U",
        'R' to "[AG]",
        'Y' to "[CT]",
        'S' to "[GC]",
        'W' to "[AT]",
        'K' to "[GT]",
        'M' to "[AC]",
        'B' to "[CGT]",
        'D' to "[AGT]",
        'H' to "[ACT]",
        'V' to "[ACG]",
        'N' to "[ACGTU]"
    )

    fun iupacToRegex(iupacPattern: String): String {
        val sb = StringBuilder()
        for (ch in iupacPattern.uppercase()) {
            sb.append(iupacMap[ch] ?: ch.toString())
        }
        return sb.toString()
    }

    fun findMotifs(
        sequence: String,
        pattern: String,
        motifName: String = "Custom Motif",
        isIupac: Boolean = true
    ): List<MotifMatch> {
        val regexStr = if (isIupac) iupacToRegex(pattern) else pattern
        val regex = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE)
        val matcher = regex.matcher(sequence)
        val matches = mutableListOf<MotifMatch>()

        while (matcher.find()) {
            matches.add(
                MotifMatch(
                    motifName = motifName,
                    pattern = pattern,
                    startIndex = matcher.start(),
                    endIndex = matcher.end(),
                    matchedSequence = matcher.group()
                )
            )
        }
        return matches
    }

    val standardPromoterMotifs = listOf(
        Pair("TATA Box (Promoter Core)", "TATAAA"),
        Pair("Pribnow Box (Bacterial -10)", "TATAAT"),
        Pair("CAAT Box (Eukaryotic -80)", "GGCCAATCT"),
        Pair("GC Box (Sp1 Binding)", "GGGCGG"),
        Pair("E-Box (bHLH Transcription)", "CANNTG"),
        Pair("Kozak Consensus (Translation Init)", "GCCRCCATGG"),
        Pair("Shine-Dalgarno (Prokaryotic Ribosome)", "AGGAGG"),
        Pair("Polyadenylation Signal", "AATAAA")
    )

    fun scanKnownMotifs(sequence: String): List<MotifMatch> {
        val results = mutableListOf<MotifMatch>()
        for ((name, pattern) in standardPromoterMotifs) {
            results.addAll(findMotifs(sequence, pattern, name, isIupac = true))
        }
        return results.sortedBy { it.startIndex }
    }
}
