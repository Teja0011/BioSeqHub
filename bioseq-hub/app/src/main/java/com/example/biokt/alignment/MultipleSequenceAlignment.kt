package com.example.biokt.alignment

data class MsaResult(
    val alignedSequences: List<Pair<String, String>>, // (id, alignedSequence)
    val consensusSequence: String,
    val conservationScores: List<Double>,
    val alignmentLength: Int
)

/**
 * Heuristic Multiple Sequence Alignment (MSA) in BioKt
 */
object MultipleSequenceAlignment {

    fun align(
        sequences: List<Pair<String, String>>, // (id, rawSeq)
        isProtein: Boolean = false
    ): MsaResult {
        if (sequences.isEmpty()) {
            return MsaResult(emptyList(), "", emptyList(), 0)
        }
        if (sequences.size == 1) {
            val (id, seq) = sequences.first()
            return MsaResult(
                alignedSequences = listOf(id to seq),
                consensusSequence = seq,
                conservationScores = List(seq.length) { 1.0 },
                alignmentLength = seq.length
            )
        }

        // 1. Pick reference sequence (longest)
        val sorted = sequences.sortedByDescending { it.second.length }
        val reference = sorted.first()
        val rest = sorted.drop(1)

        val alignedList = mutableListOf<Pair<String, String>>()
        alignedList.add(reference)

        // 2. Align each other sequence against the master reference profile
        val masterSeq = reference.second

        for ((id, seq) in rest) {
            val pairAlign = NeedlemanWunsch.align(masterSeq, seq, isProtein = isProtein)
            alignedList.add(id to pairAlign.targetAligned)
        }

        // Normalize lengths with padding
        val maxLen = alignedList.maxOf { it.second.length }
        val normalized = alignedList.map { (id, seq) ->
            id to seq.padEnd(maxLen, '-')
        }

        // 3. Compute Consensus Sequence & Conservation Profile
        val consensus = StringBuilder()
        val conservation = mutableListOf<Double>()

        for (col in 0 until maxLen) {
            val columnChars = normalized.map { it.second.getOrElse(col) { '-' } }.filter { it != '-' }
            if (columnChars.isEmpty()) {
                consensus.append('-')
                conservation.add(0.0)
            } else {
                val mode = columnChars.groupingBy { it }.eachCount().maxByOrNull { it.value }
                val dominantChar = mode?.key ?: 'N'
                val dominantFreq = (mode?.value?.toDouble() ?: 0.0) / normalized.size
                consensus.append(dominantChar)
                conservation.add(dominantFreq)
            }
        }

        return MsaResult(
            alignedSequences = normalized,
            consensusSequence = consensus.toString(),
            conservationScores = conservation,
            alignmentLength = maxLen
        )
    }
}
