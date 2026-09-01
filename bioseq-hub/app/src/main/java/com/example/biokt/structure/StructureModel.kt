package com.example.biokt.structure

import kotlin.math.sqrt

data class Atom3D(
    val serial: Int,
    val name: String,
    val resName: String,
    val chainId: String,
    val resSeq: Int,
    val x: Double,
    val y: Double,
    val z: Double,
    val element: String,
    val bFactor: Double = 0.0
) {
    fun distanceTo(other: Atom3D): Double {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

data class Residue3D(
    val name: String,
    val resSeq: Int,
    val chainId: String,
    val atoms: List<Atom3D>
) {
    val alphaCarbon: Atom3D? get() = atoms.find { it.name == "CA" }
    val nitrogen: Atom3D? get() = atoms.find { it.name == "N" }
    val carbonylCarbon: Atom3D? get() = atoms.find { it.name == "C" }
}

data class Chain3D(
    val id: String,
    val residues: List<Residue3D>
)

data class ProteinStructure3D(
    val id: String,
    val title: String,
    val chains: List<Chain3D>
) {
    val allAtoms: List<Atom3D> get() = chains.flatMap { it.residues }.flatMap { it.atoms }
    val totalResidues: Int get() = chains.sumOf { it.residues.size }

    val centerOfMass: Triple<Double, Double, Double>
        get() {
            val atoms = allAtoms
            if (atoms.isEmpty()) return Triple(0.0, 0.0, 0.0)
            val meanX = atoms.map { it.x }.average()
            val meanY = atoms.map { it.y }.average()
            val meanZ = atoms.map { it.z }.average()
            return Triple(meanX, meanY, meanZ)
        }
}
