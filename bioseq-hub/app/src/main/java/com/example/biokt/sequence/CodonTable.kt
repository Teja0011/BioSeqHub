package com.example.biokt.sequence

/**
 * Codon translation tables in BioKt
 */
class CodonTable(
    val name: String,
    val table: Map<String, Char>,
    val startCodons: Set<String> = setOf("ATG", "GTG", "TTG"),
    val stopCodons: Set<String> = setOf("TAA", "TAG", "TGA")
) {
    fun translate(codon: String): Char {
        val upper = codon.uppercase().replace('U', 'T')
        return table[upper] ?: 'X'
    }

    fun isStartCodon(codon: String): Boolean = startCodons.contains(codon.uppercase().replace('U', 'T'))
    fun isStopCodon(codon: String): Boolean = stopCodons.contains(codon.uppercase().replace('U', 'T'))

    companion object {
        val Standard = CodonTable(
            name = "Standard Genetic Code (NCBI Table 1)",
            table = mapOf(
                "TTT" to 'F', "TTC" to 'F', "TTA" to 'L', "TTG" to 'L',
                "CTT" to 'L', "CTC" to 'L', "CTA" to 'L', "CTG" to 'L',
                "ATT" to 'I', "ATC" to 'I', "ATA" to 'I', "ATG" to 'M',
                "GTT" to 'V', "GTC" to 'V', "GTA" to 'V', "GTG" to 'V',
                "TCT" to 'S', "TCC" to 'S', "TCA" to 'S', "TCG" to 'S',
                "CCT" to 'P', "CCC" to 'P', "CCA" to 'P', "CCG" to 'P',
                "ACT" to 'T', "ACC" to 'T', "ACA" to 'T', "ACG" to 'T',
                "GCT" to 'A', "GCC" to 'A', "GCA" to 'A', "GCG" to 'A',
                "TAT" to 'Y', "TAC" to 'Y', "TAA" to '*', "TAG" to '*',
                "CAT" to 'H', "CAC" to 'H', "CAA" to 'Q', "CAG" to 'Q',
                "AAT" to 'N', "AAC" to 'N', "AAA" to 'K', "AAG" to 'K',
                "GAT" to 'D', "GAC" to 'D', "GAA" to 'E', "GAG" to 'E',
                "TGT" to 'C', "TGC" to 'C', "TGA" to '*', "TGG" to 'W',
                "CGT" to 'R', "CGC" to 'R', "CGA" to 'R', "CGG" to 'R',
                "AGT" to 'S', "AGC" to 'S', "AGA" to 'R', "AGG" to 'R',
                "GGT" to 'G', "GGC" to 'G', "GGA" to 'G', "GGG" to 'G'
            )
        )

        val VertebrateMitochondrial = CodonTable(
            name = "Vertebrate Mitochondrial Code (NCBI Table 2)",
            table = Standard.table.toMutableMap().apply {
                put("AGA", '*')
                put("AGG", '*')
                put("ATA", 'M')
                put("TGA", 'W')
            },
            startCodons = setOf("ATT", "ATC", "ATA", "ATG", "GTG"),
            stopCodons = setOf("TAA", "TAG", "AGA", "AGG")
        )
    }
}
