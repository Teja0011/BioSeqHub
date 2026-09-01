package com.example.biokt.io

data class GenBankFeature(
    val type: String,
    val location: String,
    val gene: String = "",
    val product: String = "",
    val translation: String = "",
    val dbXref: List<String> = emptyList()
)

data class GenBankRecord(
    val locus: String,
    val length: Int,
    val moleculeType: String,
    val topology: String,
    val division: String,
    val accession: String,
    val version: String,
    val definition: String,
    val organism: String,
    val features: List<GenBankFeature>,
    val originSequence: String
)

object GenBankIO {
    fun parse(content: String): List<GenBankRecord> {
        val records = mutableListOf<GenBankRecord>()
        val blocks = content.split("//")

        for (block in blocks) {
            val lines = block.lines()
            if (lines.isEmpty() || block.isBlank()) continue

            var locus = ""
            var length = 0
            var moleculeType = "DNA"
            var topology = "linear"
            var division = "PLN"
            var accession = ""
            var version = ""
            var definition = ""
            var organism = ""
            val features = mutableListOf<GenBankFeature>()
            val origin = StringBuilder()

            var inFeatures = false
            var inOrigin = false
            var currentFeatType = ""
            var currentFeatLoc = ""
            var currentGene = ""
            var currentProduct = ""
            var currentTranslation = ""
            val currentXrefs = mutableListOf<String>()

            fun flushFeature() {
                if (currentFeatType.isNotBlank()) {
                    features.add(
                        GenBankFeature(
                            type = currentFeatType,
                            location = currentFeatLoc,
                            gene = currentGene,
                            product = currentProduct,
                            translation = currentTranslation,
                            dbXref = currentXrefs.toList()
                        )
                    )
                    currentFeatType = ""
                    currentFeatLoc = ""
                    currentGene = ""
                    currentProduct = ""
                    currentTranslation = ""
                    currentXrefs.clear()
                }
            }

            for (line in lines) {
                val trimmed = line.trim()
                if (line.startsWith("LOCUS")) {
                    val parts = line.substring(5).trim().split("\\s+".toRegex())
                    locus = parts.getOrNull(0) ?: ""
                    length = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    moleculeType = parts.getOrNull(3) ?: "DNA"
                } else if (line.startsWith("DEFINITION")) {
                    definition = line.substring(10).trim()
                } else if (line.startsWith("ACCESSION")) {
                    accession = line.substring(9).trim().split("\\s+".toRegex()).firstOrNull() ?: ""
                } else if (line.startsWith("VERSION")) {
                    version = line.substring(7).trim()
                } else if (line.startsWith("  ORGANISM")) {
                    organism = line.substring(10).trim()
                } else if (line.startsWith("FEATURES")) {
                    inFeatures = true
                } else if (line.startsWith("ORIGIN")) {
                    flushFeature()
                    inFeatures = false
                    inOrigin = true
                } else if (inFeatures) {
                    if (line.startsWith("     ") && !line.startsWith("       /")) {
                        flushFeature()
                        val featLine = line.trim().split("\\s+".toRegex(), limit = 2)
                        currentFeatType = featLine.getOrNull(0) ?: "feature"
                        currentFeatLoc = featLine.getOrNull(1) ?: ""
                    } else if (line.contains("/gene=")) {
                        currentGene = line.substringAfter("/gene=").replace("\"", "").trim()
                    } else if (line.contains("/product=")) {
                        currentProduct = line.substringAfter("/product=").replace("\"", "").trim()
                    } else if (line.contains("/translation=")) {
                        currentTranslation = line.substringAfter("/translation=").replace("\"", "").trim()
                    } else if (line.contains("/db_xref=")) {
                        currentXrefs.add(line.substringAfter("/db_xref=").replace("\"", "").trim())
                    }
                } else if (inOrigin) {
                    val seqPart = trimmed.replace("\\d".toRegex(), "").replace("\\s".toRegex(), "")
                    origin.append(seqPart)
                }
            }
            flushFeature()

            if (locus.isNotBlank() || accession.isNotBlank() || origin.isNotEmpty()) {
                records.add(
                    GenBankRecord(
                        locus = locus.ifBlank { accession },
                        length = if (length > 0) length else origin.length,
                        moleculeType = moleculeType,
                        topology = topology,
                        division = division,
                        accession = accession.ifBlank { locus },
                        version = version,
                        definition = definition,
                        organism = organism,
                        features = features,
                        originSequence = origin.toString().uppercase()
                    )
                )
            }
        }
        return records
    }
}
