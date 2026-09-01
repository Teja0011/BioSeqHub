package com.example.biokt.io

data class FastqRecord(
    val id: String,
    val sequence: String,
    val qualityHeader: String,
    val qualityScoresString: String
) {
    val length: Int get() = sequence.length

    val phredQualityScores: List<Int>
        get() = qualityScoresString.map { it.code - 33 }

    val meanQualityScore: Double
        get() {
            val scores = phredQualityScores
            return if (scores.isNotEmpty()) scores.average() else 0.0
        }

    val q30Percent: Double
        get() {
            val scores = phredQualityScores
            if (scores.isEmpty()) return 0.0
            val q30Count = scores.count { it >= 30 }
            return (q30Count.toDouble() / scores.size) * 100.0
        }

    fun toFastqString(): String {
        return "@$id\n$sequence\n+$qualityHeader\n$qualityScoresString\n"
    }
}

object FastqIO {
    fun parse(fastqContent: String): List<FastqRecord> {
        val records = mutableListOf<FastqRecord>()
        val lines = fastqContent.lines().map { it.trim() }.filter { it.isNotEmpty() }

        var i = 0
        while (i + 3 < lines.size) {
            val line1 = lines[i]
            val line2 = lines[i + 1]
            val line3 = lines[i + 2]
            val line4 = lines[i + 3]

            if (line1.startsWith("@") && line3.startsWith("+")) {
                val id = line1.substring(1)
                val seq = line2
                val qualHeader = if (line3.length > 1) line3.substring(1) else ""
                val qual = line4
                records.add(FastqRecord(id, seq, qualHeader, qual))
                i += 4
            } else {
                i++
            }
        }
        return records
    }

    fun write(records: List<FastqRecord>): String {
        return records.joinToString("") { it.toFastqString() }
    }
}
