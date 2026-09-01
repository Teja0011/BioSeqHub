package com.example.domain.model

data class BiologicalSequence(
    val sequenceId: String,
    val accession: String,
    val sequenceType: String, // PROTEIN, DNA, RNA
    val sequence: String,
    val organism: String,
    val length: Int,
    val source: String, // NCBI, UniProt, Ensembl
    val description: String = "",
    val gcContent: Double = 0.0,
    val molecularWeightDa: Double = 0.0,
    val retrievedAt: Long = System.currentTimeMillis()
)

data class SequenceAnnotation(
    val annotationId: String,
    val sequenceId: String,
    val source: String, // UniProt, InterPro, Pfam, GO
    val annotationType: String, // FUNCTION, DOMAIN, FAMILY, INTERACTION, PTM
    val annotationText: String,
    val evidence: String, // ECO:0000269, Curated, Experimental
    val startResidue: Int? = null,
    val endResidue: Int? = null,
    val retrievedAt: Long = System.currentTimeMillis()
)

data class OrthologRecord(
    val orthologId: String,
    val sourceSequence: String,
    val targetSequence: String,
    val sourceOrganism: String,
    val targetOrganism: String,
    val identityPercent: Double,
    val coveragePercent: Double,
    val score: Double,
    val method: String = "Reciprocal BLAST (BioKt)",
    val alignment: String = ""
)

data class ParalogRecord(
    val paralogId: String,
    val geneSymbol: String,
    val duplicatedGene: String,
    val chromosomeLocation: String,
    val identityPercent: Double,
    val duplicationType: String, // Tandem, Whole-Genome, Segmental
    val evolutionaryDistance: Double
)

data class DiseaseAssociation(
    val associationId: String,
    val geneId: String,
    val proteinId: String,
    val diseaseId: String,
    val diseaseName: String,
    val source: String, // DisGeNET, OMIM, ClinVar
    val score: Double,
    val evidencePublications: List<String> = emptyList()
)

data class CrossReferenceRecord(
    val crossReferenceId: String,
    val geneSymbol: String,
    val ncbiGeneId: String,
    val uniprotAccession: String,
    val ncbiProteinAccession: String,
    val pubchemCid: String?,
    val pubmedPmids: List<String>,
    val pdbStructures: List<String>,
    val ensemblId: String = ""
)

data class Molecular3DStructure(
    val structureId: String, // e.g., "1TUP", "CID_2244"
    val title: String,
    val source: String, // RCSB PDB, AlphaFold DB, PubChem 3D
    val accession: String,
    val moleculeName: String,
    val organism: String,
    val resolution: String,
    val chains: List<String>,
    val structureType: String, // PROTEIN, SMALL_MOLECULE
    val pdbContent: String = "",
    val atomsCount: Int = 0
)

data class QueryHistoryRecord(
    val queryId: String,
    val userId: String,
    val queryType: String,
    val database: String,
    val queryText: String,
    val parametersJson: String,
    val resultsSummary: String,
    val timestamp: Long,
    val status: String
)

data class NetworkNode(
    val id: String,
    val label: String,
    val type: String, // GENE, PROTEIN, DISEASE, DRUG, PATHWAY
    val x: Float,
    val y: Float
)

data class NetworkEdge(
    val fromNodeId: String,
    val toNodeId: String,
    val relationship: String,
    val weight: Float = 1.0f
)

data class BiologicalNetwork(
    val nodes: List<NetworkNode>,
    val edges: List<NetworkEdge>
)

data class SheetTabInfo(
    val name: String,
    val columnCount: Int,
    val rowCount: Int,
    val description: String,
    val requiredColumns: List<String>
)

data class GeneOntologyTerm(
    val goId: String,
    val termName: String,
    val category: String, // Biological Process, Molecular Function, Cellular Component
    val evidenceCode: String,
    val definition: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<String> = emptyList(),
    val actionSuggestion: String? = null
)
