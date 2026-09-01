package com.example.biokt.sequence

import kotlin.math.sqrt

data class KmerFrequency(
    val kmer: String,
    val count: Int,
    val frequency: Double
)

/**
 * K-mer frequency analysis and genomic distance estimation in BioKt
 */
object KmerCounter {

    fun countKmers(sequence: String, k: Int = 3): List<KmerFrequency> {
        val clean = sequence.uppercase().filter { it in "ATGCUN" }
        if (k <= 0 || clean.length < k) return emptyList()

        val counts = mutableMapOf<String, Int>()
        val totalKmers = clean.length - k + 1

        for (i in 0 until totalKmers) {
            val kmer = clean.substring(i, i + k)
            counts[kmer] = (counts[kmer] ?: 0) + 1
        }

        return counts.map { (kmer, count) ->
            KmerFrequency(
                kmer = kmer,
                count = count,
                frequency = (count.toDouble() / totalKmers) * 100.0
            )
        }.sortedByDescending { it.count }
    }

    /**
     * Compute Euclidean / Cosine distance between k-mer spectrums of two sequences
     */
    fun kmerCosineDistance(seq1: String, seq2: String, k: Int = 3): Double {
        val freq1 = countKmers(seq1, k).associate { it.kmer to it.count.toDouble() }
        val freq2 = countKmers(seq2, k).associate { it.kmer to it.count.toDouble() }

        val allKmers = freq1.keys + freq2.keys
        if (allKmers.isEmpty()) return 1.0

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (kmer in allKmers) {
            val a = freq1[kmer] ?: 0.0
            val b = freq2[kmer] ?: 0.0
            dotProduct += a * b
            normA += a * a
            normB += b * b
        }

        if (normA == 0.0 || normB == 0.0) return 1.0
        val similarity = dotProduct / (sqrt(normA) * sqrt(normB))
        return (1.0 - similarity).coerceIn(0.0, 1.0)
    }
}
