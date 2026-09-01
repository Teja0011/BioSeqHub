package com.example.biokt.io

data class FastaRecord(
    val id: String,
    val description: String,
    val sequence: String
) {
    val length: Int get() = sequence.length
    val formattedHeader: String get() = if (description.isNotBlank()) "$id $description" else id

    fun toFastaString(lineWidth: Int = 70): String {
        val sb = StringBuilder()
        sb.append(">").append(formattedHeader).append("\n")
        var i = 0
        while (i < sequence.length) {
            val end = (i + lineWidth).coerceAtMost(sequence.length)
            sb.append(sequence.substring(i, end)).append("\n")
            i += lineWidth
        }
        return sb.toString()
    }
}

object FastaIO {
    fun parse(fastaContent: String): List<FastaRecord> {
        val records = mutableListOf<FastaRecord>()
        val lines = fastaContent.lines()

        var currentId = ""
        var currentDesc = ""
        val currentSeq = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith(">")) {
                if (currentId.isNotBlank() || currentSeq.isNotEmpty()) {
                    records.add(
                        FastaRecord(
                            id = currentId,
                            description = currentDesc,
                            sequence = currentSeq.toString()
                        )
                    )
                    currentSeq.clear()
                }
                val header = trimmed.substring(1).trim()
                val parts = header.split("\\s+".toRegex(), limit = 2)
                currentId = parts.getOrNull(0) ?: "seq"
                currentDesc = parts.getOrNull(1) ?: ""
            } else if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                currentSeq.append(trimmed.replace("\\s".toRegex(), ""))
            }
        }

        if (currentId.isNotBlank() || currentSeq.isNotEmpty()) {
            records.add(
                FastaRecord(
                    id = currentId.ifBlank { "seq_1" },
                    description = currentDesc,
                    sequence = currentSeq.toString()
                )
            )
        }

        return records
    }

    fun write(records: List<FastaRecord>, lineWidth: Int = 70): String {
        return records.joinToString("\n") { it.toFastaString(lineWidth) }
    }
}
