package com.example.biokt.sequence

import kotlin.math.roundToInt

/**
 * Type of biological sequence
 */
enum class SequenceType {
    DNA,
    RNA,
    PROTEIN,
    UNKNOWN
}

/**
 * Base BioSequence interface representing biological macromolecules in BioKt
 */
interface BioSequence {
    val rawSequence: String
    val sequenceType: SequenceType
    val length: Int get() = rawSequence.length
    val gcContent: Double
    val molecularWeight: Double

    fun subSequence(start: Int, end: Int): BioSequence
    fun reverseComplement(): BioSequence
    fun composition(): Map<Char, Int>
    fun kmers(k: Int): List<String>
}

/**
 * Deoxyribonucleic Acid (DNA) sequence representation
 */
class DnaSequence(sequence: String) : BioSequence {
    override val rawSequence: String = sequence.uppercase().filter { it in "ATGCUN" }
    override val sequenceType: SequenceType = SequenceType.DNA

    override val gcContent: Double
        get() {
            if (rawSequence.isEmpty()) return 0.0
            val gc = rawSequence.count { it == 'G' || it == 'C' }
            return (gc.toDouble() / rawSequence.length) * 100.0
        }

    override val molecularWeight: Double
        get() = rawSequence.length * 330.0 // Da per nucleotide approx

    override fun subSequence(start: Int, end: Int): DnaSequence {
        val s = start.coerceIn(0, rawSequence.length)
        val e = end.coerceIn(s, rawSequence.length)
        return DnaSequence(rawSequence.substring(s, e))
    }

    override fun reverseComplement(): DnaSequence {
        val comp = mapOf('A' to 'T', 'T' to 'A', 'G' to 'C', 'C' to 'G', 'U' to 'A', 'N' to 'N')
        val revComp = rawSequence.reversed().map { comp[it] ?: it }.joinToString("")
        return DnaSequence(revComp)
    }

    fun transcribe(): RnaSequence {
        return RnaSequence(rawSequence.replace('T', 'U'))
    }

    fun translate(readingFrame: Int = 0, codonTable: CodonTable = CodonTable.Standard): ProteinSequence {
        val rFrame = readingFrame.coerceIn(0, 2)
        val sb = StringBuilder()
        for (i in rFrame until (rawSequence.length - 2) step 3) {
            val codon = rawSequence.substring(i, i + 3)
            val aa = codonTable.translate(codon)
            sb.append(aa)
        }
        return ProteinSequence(sb.toString())
    }

    override fun composition(): Map<Char, Int> {
        val map = mutableMapOf('A' to 0, 'C' to 0, 'G' to 0, 'T' to 0)
        for (ch in rawSequence) {
            map[ch] = (map[ch] ?: 0) + 1
        }
        return map
    }

    override fun kmers(k: Int): List<String> {
        if (k <= 0 || k > rawSequence.length) return emptyList()
        return (0..rawSequence.length - k).map { rawSequence.substring(it, it + k) }
    }

    override fun toString(): String = rawSequence
}

/**
 * Ribonucleic Acid (RNA) sequence representation
 */
class RnaSequence(sequence: String) : BioSequence {
    override val rawSequence: String = sequence.uppercase().filter { it in "AUGCUN" }
    override val sequenceType: SequenceType = SequenceType.RNA

    override val gcContent: Double
        get() {
            if (rawSequence.isEmpty()) return 0.0
            val gc = rawSequence.count { it == 'G' || it == 'C' }
            return (gc.toDouble() / rawSequence.length) * 100.0
        }

    override val molecularWeight: Double
        get() = rawSequence.length * 340.0

    override fun subSequence(start: Int, end: Int): RnaSequence {
        val s = start.coerceIn(0, rawSequence.length)
        val e = end.coerceIn(s, rawSequence.length)
        return RnaSequence(rawSequence.substring(s, e))
    }

    override fun reverseComplement(): RnaSequence {
        val comp = mapOf('A' to 'U', 'U' to 'A', 'G' to 'C', 'C' to 'G', 'T' to 'A', 'N' to 'N')
        val revComp = rawSequence.reversed().map { comp[it] ?: it }.joinToString("")
        return RnaSequence(revComp)
    }

    fun backTranscribe(): DnaSequence {
        return DnaSequence(rawSequence.replace('U', 'T'))
    }

    fun translate(readingFrame: Int = 0, codonTable: CodonTable = CodonTable.Standard): ProteinSequence {
        return backTranscribe().translate(readingFrame, codonTable)
    }

    override fun composition(): Map<Char, Int> {
        val map = mutableMapOf('A' to 0, 'C' to 0, 'G' to 0, 'U' to 0)
        for (ch in rawSequence) {
            map[ch] = (map[ch] ?: 0) + 1
        }
        return map
    }

    override fun kmers(k: Int): List<String> {
        if (k <= 0 || k > rawSequence.length) return emptyList()
        return (0..rawSequence.length - k).map { rawSequence.substring(it, it + k) }
    }

    override fun toString(): String = rawSequence
}

/**
 * Protein sequence representation with physico-chemical properties
 */
class ProteinSequence(sequence: String) : BioSequence {
    override val rawSequence: String = sequence.uppercase().filter { it in 'A'..'Z' || it == '*' }
    override val sequenceType: SequenceType = SequenceType.PROTEIN

    override val gcContent: Double get() = 0.0

    override val molecularWeight: Double
        get() {
            // Average residue molecular weight in Daltons
            val aaWeights = mapOf(
                'A' to 89.09, 'R' to 174.20, 'N' to 132.12, 'D' to 133.10, 'C' to 121.16,
                'E' to 147.13, 'Q' to 146.15, 'G' to 75.07, 'H' to 155.16, 'I' to 131.18,
                'L' to 131.18, 'K' to 146.19, 'M' to 149.21, 'F' to 165.19, 'P' to 115.13,
                'S' to 105.09, 'T' to 119.12, 'W' to 204.23, 'Y' to 181.19, 'V' to 117.15
            )
            val sum = rawSequence.sumOf { aaWeights[it] ?: 110.0 }
            return if (rawSequence.isNotEmpty()) sum - (rawSequence.length - 1) * 18.015 else 0.0
        }

    val isoelectricPointEstimate: Double
        get() {
            val pos = rawSequence.count { it == 'K' || it == 'R' || it == 'H' }
            val neg = rawSequence.count { it == 'D' || it == 'E' }
            return 7.0 + (pos - neg) * 0.15
        }

    val hydrophobicityPercent: Double
        get() {
            if (rawSequence.isEmpty()) return 0.0
            val hydro = rawSequence.count { it in "AILMFWV" }
            return (hydro.toDouble() / rawSequence.length) * 100.0
        }

    override fun subSequence(start: Int, end: Int): ProteinSequence {
        val s = start.coerceIn(0, rawSequence.length)
        val e = end.coerceIn(s, rawSequence.length)
        return ProteinSequence(rawSequence.substring(s, e))
    }

    override fun reverseComplement(): ProteinSequence {
        return ProteinSequence(rawSequence.reversed())
    }

    override fun composition(): Map<Char, Int> {
        val map = mutableMapOf<Char, Int>()
        for (ch in rawSequence) {
            map[ch] = (map[ch] ?: 0) + 1
        }
        return map.toSortedMap()
    }

    override fun kmers(k: Int): List<String> {
        if (k <= 0 || k > rawSequence.length) return emptyList()
        return (0..rawSequence.length - k).map { rawSequence.substring(it, it + k) }
    }

    override fun toString(): String = rawSequence
}
