package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queries")
data class QueryEntity(
    @PrimaryKey val queryId: String,
    val userId: String,
    val queryType: String,
    val database: String,
    val queryText: String,
    val parameters: String,
    val createdAt: Long,
    val updatedAt: Long,
    val status: String
)

@Entity(tableName = "query_results")
data class QueryResultEntity(
    @PrimaryKey val resultId: String,
    val queryId: String,
    val recordId: String,
    val database: String,
    val resultType: String,
    val summary: String,
    val createdAt: Long
)

@Entity(tableName = "sequences")
data class SequenceEntity(
    @PrimaryKey val sequenceId: String,
    val accession: String,
    val sequenceType: String,
    val sequence: String,
    val organism: String,
    val length: Int,
    val source: String,
    val description: String,
    val gcContent: Double,
    val molecularWeightDa: Double,
    val retrievedAt: Long
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
    @PrimaryKey val annotationId: String,
    val sequenceId: String,
    val source: String,
    val annotationType: String,
    val annotationText: String,
    val evidence: String,
    val startResidue: Int?,
    val endResidue: Int?,
    val retrievedAt: Long
)

@Entity(tableName = "orthologs")
data class OrthologEntity(
    @PrimaryKey val orthologId: String,
    val sourceSequence: String,
    val targetSequence: String,
    val sourceOrganism: String,
    val targetOrganism: String,
    val identity: Double,
    val coverage: Double,
    val score: Double,
    val method: String,
    val alignment: String = ""
)

@Entity(tableName = "disease_associations")
data class DiseaseAssociationEntity(
    @PrimaryKey val associationId: String,
    val geneId: String,
    val proteinId: String,
    val diseaseId: String,
    val diseaseName: String,
    val source: String,
    val score: Double,
    val evidence: String
)

@Entity(tableName = "cross_referencing")
data class CrossReferenceEntity(
    @PrimaryKey val crossReferenceId: String,
    val geneSymbol: String,
    val sourceDatabase: String,
    val sourceIdentifier: String,
    val targetDatabase: String,
    val targetIdentifier: String,
    val relationship: String,
    val fullRecordJson: String = ""
)

@Entity(tableName = "structures")
data class StructureEntity(
    @PrimaryKey val structureId: String,
    val source: String,
    val sourceIdentifier: String,
    val accession: String,
    val structureType: String,
    val moleculeName: String,
    val organism: String,
    val chainCount: Int,
    val resolution: String,
    val chainsJson: String,
    val pdbContent: String,
    val retrievedAt: Long
)

@Entity(tableName = "analysis_metadata")
data class AnalysisMetadataEntity(
    @PrimaryKey val metadataId: String,
    val queryId: String,
    val algorithm: String,
    val executionTimeMs: Long,
    val cacheHit: Boolean,
    val timestamp: Long
)
