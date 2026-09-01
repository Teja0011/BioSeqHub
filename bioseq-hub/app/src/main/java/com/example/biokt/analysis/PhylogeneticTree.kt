package com.example.biokt.analysis

import kotlin.math.ln

data class PhyloNode(
    val id: String,
    val branchLength: Double = 0.0,
    val left: PhyloNode? = null,
    val right: PhyloNode? = null
) {
    val isLeaf: Boolean get() = left == null && right == null

    fun toNewick(): String {
        return if (isLeaf) {
            if (branchLength > 0) "$id:${String.format("%.4f", branchLength)}" else id
        } else {
            val leftStr = left?.toNewick() ?: ""
            val rightStr = right?.toNewick() ?: ""
            if (branchLength > 0) "($leftStr,$rightStr):${String.format("%.4f", branchLength)}"
            else "($leftStr,$rightStr)"
        }
    }
}

/**
 * Phylogenetic tree reconstruction using Jukes-Cantor distance & UPGMA/Neighbor-Joining in BioKt
 */
object PhylogeneticTree {

    fun calculateJukesCantorDistance(seq1: String, seq2: String): Double {
        val s1 = seq1.uppercase().trim()
        val s2 = seq2.uppercase().trim()
        val minLen = minOf(s1.length, s2.length)
        if (minLen == 0) return 0.0

        var diffs = 0
        var validSites = 0
        for (i in 0 until minLen) {
            val c1 = s1[i]
            val c2 = s2[i]
            if (c1 in "ATGC" && c2 in "ATGC") {
                validSites++
                if (c1 != c2) diffs++
            }
        }

        if (validSites == 0) return 0.0
        val p = diffs.toDouble() / validSites
        if (p >= 0.75) return 2.0 // Saturated evolutionary divergence
        return -0.75 * ln(1.0 - (4.0 / 3.0) * p)
    }

    fun buildUpgmaTree(sequences: List<Pair<String, String>>): PhyloNode {
        if (sequences.isEmpty()) return PhyloNode("Empty")
        if (sequences.size == 1) return PhyloNode(sequences.first().first)

        var clusters = sequences.map { (id, _) -> PhyloNode(id) }.toMutableList()
        val matrix = mutableMapOf<Pair<String, String>, Double>()

        for (i in sequences.indices) {
            for (j in i + 1 until sequences.size) {
                val d = calculateJukesCantorDistance(sequences[i].second, sequences[j].second)
                matrix[Pair(sequences[i].first, sequences[j].first)] = d
                matrix[Pair(sequences[j].first, sequences[i].first)] = d
            }
        }

        var clusterIndex = 1
        while (clusters.size > 1) {
            var minD = Double.MAX_VALUE
            var bestPair = Pair(0, 1)

            for (i in 0 until clusters.size) {
                for (j in i + 1 until clusters.size) {
                    val key = Pair(clusters[i].id, clusters[j].id)
                    val d = matrix[key] ?: 0.1
                    if (d < minD) {
                        minD = d
                        bestPair = Pair(i, j)
                    }
                }
            }

            val c1 = clusters[bestPair.first]
            val c2 = clusters[bestPair.second]
            val branchLen = minD / 2.0

            val newNode = PhyloNode(
                id = "Node_$clusterIndex",
                branchLength = branchLen,
                left = c1.copy(branchLength = branchLen),
                right = c2.copy(branchLength = branchLen)
            )
            clusterIndex++

            clusters.removeAt(maxOf(bestPair.first, bestPair.second))
            clusters.removeAt(minOf(bestPair.first, bestPair.second))

            // Update distance matrix for new cluster
            for (existing in clusters) {
                val d1 = matrix[Pair(c1.id, existing.id)] ?: 0.1
                val d2 = matrix[Pair(c2.id, existing.id)] ?: 0.1
                val avg = (d1 + d2) / 2.0
                matrix[Pair(newNode.id, existing.id)] = avg
                matrix[Pair(existing.id, newNode.id)] = avg
            }
            clusters.add(newNode)
        }

        return clusters.first()
    }
}
