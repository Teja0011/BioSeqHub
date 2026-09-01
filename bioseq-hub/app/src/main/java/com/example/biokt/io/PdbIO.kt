package com.example.biokt.io

data class PdbAtom(
    val serial: Int,
    val name: String,
    val altLoc: Char,
    val resName: String,
    val chainId: String,
    val resSeq: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val occupancy: Double,
    val tempFactor: Double,
    val element: String
)

data class PdbRecord(
    val id: String,
    val header: String,
    val title: String,
    val atoms: List<PdbAtom>
) {
    val chains: List<String> get() = atoms.map { it.chainId }.distinct()
    val residueCount: Int get() = atoms.map { "${it.chainId}_${it.resSeq}" }.distinct().size
}

object PdbIO {
    fun parse(pdbContent: String, pdbId: String = "PDB"): PdbRecord {
        var header = ""
        var title = ""
        val atoms = mutableListOf<PdbAtom>()

        for (line in pdbContent.lines()) {
            if (line.startsWith("HEADER")) {
                header = line.substring(6).trim()
            } else if (line.startsWith("TITLE")) {
                title = (title + " " + line.substring(5).trim()).trim()
            } else if (line.startsWith("ATOM  ") || line.startsWith("HETATM")) {
                try {
                    val serial = line.substring(6, 11).trim().toIntOrNull() ?: 0
                    val name = line.substring(12, 16).trim()
                    val altLoc = line.getOrNull(16) ?: ' '
                    val resName = line.substring(17, 20).trim()
                    val chainId = line.substring(21, 22).trim().ifBlank { "A" }
                    val resSeq = line.substring(22, 26).trim().toIntOrNull() ?: 0
                    val x = line.substring(30, 38).trim().toDoubleOrNull() ?: 0.0
                    val y = line.substring(38, 46).trim().toDoubleOrNull() ?: 0.0
                    val z = line.substring(46, 54).trim().toDoubleOrNull() ?: 0.0
                    val occupancy = line.getOrNull(54)?.let { line.substring(54, 60).trim().toDoubleOrNull() } ?: 1.0
                    val tempFactor = line.getOrNull(60)?.let { line.substring(60, 66).trim().toDoubleOrNull() } ?: 0.0
                    val element = if (line.length >= 78) line.substring(76, 78).trim() else name.take(1)

                    atoms.add(
                        PdbAtom(
                            serial = serial,
                            name = name,
                            altLoc = altLoc,
                            resName = resName,
                            chainId = chainId,
                            resSeq = resSeq,
                            x = x,
                            y = y,
                            z = z,
                            occupancy = occupancy,
                            tempFactor = tempFactor,
                            element = element
                        )
                    )
                } catch (e: Exception) {
                    // Ignore malformed line
                }
            }
        }

        return PdbRecord(
            id = pdbId,
            header = header,
            title = title,
            atoms = atoms
        )
    }
}
