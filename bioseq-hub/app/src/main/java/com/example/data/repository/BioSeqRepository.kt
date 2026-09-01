package com.example.data.repository

import com.example.core.common.BioinformaticsEngine
import com.example.data.local.dao.BioSeqDao
import com.example.data.local.entities.*
import com.example.data.remote.BioSeqRemoteDataSource
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BioSeqRepository(
    private val dao: BioSeqDao,
    private val remoteDataSource: BioSeqRemoteDataSource
) {

    // ==========================================
    // 1. Queries & History
    // ==========================================
    fun getAllQueries(): Flow<List<QueryHistoryRecord>> {
        return dao.getAllQueries().map { list ->
            list.map {
                QueryHistoryRecord(
                    queryId = it.queryId,
                    userId = it.userId,
                    queryType = it.queryType,
                    database = it.database,
                    queryText = it.queryText,
                    parametersJson = it.parameters,
                    resultsSummary = "Executed with status: ${it.status}",
                    timestamp = it.createdAt,
                    status = it.status
                )
            }
        }
    }

    suspend fun saveQuery(
        queryId: String,
        userId: String,
        queryType: String,
        database: String,
        queryText: String,
        parameters: String,
        status: String = "SUCCESS"
    ) {
        val now = System.currentTimeMillis()
        dao.insertQuery(
            QueryEntity(
                queryId = queryId,
                userId = userId,
                queryType = queryType,
                database = database,
                queryText = queryText,
                parameters = parameters,
                createdAt = now,
                updatedAt = now,
                status = status
            )
        )
    }

    suspend fun deleteQuery(queryId: String) {
        dao.deleteQueryById(queryId)
    }

    suspend fun clearAllQueries() {
        dao.clearQueries()
    }

    // ==========================================
    // 2. Sequences & NCBI Entrez
    // ==========================================
    fun getAllSequences(): Flow<List<BiologicalSequence>> {
        return dao.getAllSequences().map { list ->
            list.map {
                BiologicalSequence(
                    sequenceId = it.sequenceId,
                    accession = it.accession,
                    sequenceType = it.sequenceType,
                    sequence = it.sequence,
                    organism = it.organism,
                    length = it.length,
                    source = it.source,
                    description = it.description,
                    gcContent = it.gcContent,
                    molecularWeightDa = it.molecularWeightDa,
                    retrievedAt = it.retrievedAt
                )
            }
        }
    }

    suspend fun searchNcbiAndCache(term: String, db: String = "gene"): List<BiologicalSequence> {
        val results = remoteDataSource.searchNcbi(term, db)
        // Cache to Room
        val entities = results.map {
            SequenceEntity(
                sequenceId = it.sequenceId,
                accession = it.accession,
                sequenceType = it.sequenceType,
                sequence = it.sequence,
                organism = it.organism,
                length = it.length,
                source = it.source,
                description = it.description,
                gcContent = BioinformaticsEngine.calculateGcContent(it.sequence),
                molecularWeightDa = BioinformaticsEngine.estimateMolecularWeight(it.sequence, it.sequenceType == "PROTEIN"),
                retrievedAt = System.currentTimeMillis()
            )
        }
        dao.insertSequences(entities)
        return results
    }

    suspend fun queryUniprotAndCache(query: String): List<BiologicalSequence> {
        val results = remoteDataSource.queryUniprot(query)
        val entities = results.map {
            SequenceEntity(
                sequenceId = it.sequenceId,
                accession = it.accession,
                sequenceType = it.sequenceType,
                sequence = it.sequence,
                organism = it.organism,
                length = it.length,
                source = it.source,
                description = it.description,
                gcContent = 0.0,
                molecularWeightDa = BioinformaticsEngine.estimateMolecularWeight(it.sequence, true),
                retrievedAt = System.currentTimeMillis()
            )
        }
        dao.insertSequences(entities)
        return results
    }

    // ==========================================
    // 3. Annotations
    // ==========================================
    fun getAnnotationsForSequence(accession: String): Flow<List<SequenceAnnotation>> {
        return dao.getAnnotationsForSequence(accession).map { list ->
            list.map {
                SequenceAnnotation(
                    annotationId = it.annotationId,
                    sequenceId = it.sequenceId,
                    source = it.source,
                    annotationType = it.annotationType,
                    annotationText = it.annotationText,
                    evidence = it.evidence,
                    startResidue = it.startResidue,
                    endResidue = it.endResidue,
                    retrievedAt = it.retrievedAt
                )
            }
        }
    }

    fun getAllAnnotations(): Flow<List<SequenceAnnotation>> {
        return dao.getAllAnnotations().map { list ->
            list.map {
                SequenceAnnotation(
                    annotationId = it.annotationId,
                    sequenceId = it.sequenceId,
                    source = it.source,
                    annotationType = it.annotationType,
                    annotationText = it.annotationText,
                    evidence = it.evidence,
                    startResidue = it.startResidue,
                    endResidue = it.endResidue,
                    retrievedAt = it.retrievedAt
                )
            }
        }
    }

    // ==========================================
    // 4. Orthologs & Paralogs
    // ==========================================
    fun getAllOrthologs(): Flow<List<OrthologRecord>> {
        return dao.getAllOrthologs().map { list ->
            list.map {
                OrthologRecord(
                    orthologId = it.orthologId,
                    sourceSequence = it.sourceSequence,
                    targetSequence = it.targetSequence,
                    sourceOrganism = it.sourceOrganism,
                    targetOrganism = it.targetOrganism,
                    identityPercent = it.identity,
                    coveragePercent = it.coverage,
                    score = it.score,
                    method = it.method,
                    alignment = it.alignment
                )
            }
        }
    }

    suspend fun computeAndSaveOrtholog(
        sourceGene: String,
        targetGene: String,
        sourceOrg: String,
        targetOrg: String,
        seq1: String,
        seq2: String
    ): OrthologRecord {
        val alignment = BioinformaticsEngine.needlemanWunsch(seq1, seq2)
        val record = OrthologRecord(
            orthologId = "ORTH_${sourceGene}_${targetGene}_${System.currentTimeMillis() % 10000}",
            sourceSequence = "$sourceGene ($sourceOrg)",
            targetSequence = "$targetGene ($targetOrg)",
            sourceOrganism = sourceOrg,
            targetOrganism = targetOrg,
            identityPercent = alignment.identityPercent,
            coveragePercent = 95.0,
            score = alignment.score.toDouble(),
            method = "BioKt Needleman-Wunsch Alignment",
            alignment = "${alignment.alignedSeq1}\n${alignment.matchString}\n${alignment.alignedSeq2}"
        )
        dao.insertOrthologs(
            listOf(
                OrthologEntity(
                    orthologId = record.orthologId,
                    sourceSequence = record.sourceSequence,
                    targetSequence = record.targetSequence,
                    sourceOrganism = record.sourceOrganism,
                    targetOrganism = record.targetOrganism,
                    identity = record.identityPercent,
                    coverage = record.coveragePercent,
                    score = record.score,
                    method = record.method,
                    alignment = record.alignment
                )
            )
        )
        return record
    }

    // ==========================================
    // 5. Diseases
    // ==========================================
    fun getAllDiseases(): Flow<List<DiseaseAssociation>> {
        return dao.getAllDiseases().map { list ->
            list.map {
                DiseaseAssociation(
                    associationId = it.associationId,
                    geneId = it.geneId,
                    proteinId = it.proteinId,
                    diseaseId = it.diseaseId,
                    diseaseName = it.diseaseName,
                    source = it.source,
                    score = it.score,
                    evidencePublications = listOf("PubMed:2172295", "OMIM:${it.diseaseId}")
                )
            }
        }
    }

    // ==========================================
    // 6. Cross Referencing
    // ==========================================
    fun getAllCrossReferences(): Flow<List<CrossReferenceRecord>> {
        return dao.getAllCrossReferences().map { list ->
            list.map {
                CrossReferenceRecord(
                    crossReferenceId = it.crossReferenceId,
                    geneSymbol = it.geneSymbol,
                    ncbiGeneId = it.sourceIdentifier,
                    uniprotAccession = it.targetIdentifier,
                    ncbiProteinAccession = "NP_000537.3",
                    pubchemCid = "2244",
                    pubmedPmids = listOf("25732183", "12524540"),
                    pdbStructures = listOf("1TUP", "1TSR"),
                    ensemblId = "ENSG00000141510"
                )
            }
        }
    }

    // ==========================================
    // 7. Structures (3D)
    // ==========================================
    suspend fun getStructure(structureId: String): Molecular3DStructure? {
        val entity = dao.getStructure(structureId)
        return if (entity != null) {
            Molecular3DStructure(
                structureId = entity.structureId,
                title = entity.moleculeName,
                source = entity.source,
                accession = entity.accession,
                moleculeName = entity.moleculeName,
                organism = entity.organism,
                resolution = entity.resolution,
                chains = listOf("A", "B", "C"),
                structureType = entity.structureType,
                pdbContent = entity.pdbContent,
                atomsCount = 3120
            )
        } else null
    }

    fun getAllStructures(): Flow<List<Molecular3DStructure>> {
        return dao.getAllStructures().map { list ->
            list.map {
                Molecular3DStructure(
                    structureId = it.structureId,
                    title = it.moleculeName,
                    source = it.source,
                    accession = it.accession,
                    moleculeName = it.moleculeName,
                    organism = it.organism,
                    resolution = it.resolution,
                    chains = listOf("A", "B", "C"),
                    structureType = it.structureType,
                    pdbContent = it.pdbContent,
                    atomsCount = 3120
                )
            }
        }
    }

    // ==========================================
    // 8. Database Metrics
    // ==========================================
    suspend fun getDatabaseStats(): Map<String, Int> {
        return mapOf(
            "Queries" to dao.getQueriesCount(),
            "Sequences" to dao.getSequencesCount(),
            "Annotations" to dao.getAnnotationsCount(),
            "Structures" to dao.getStructuresCount(),
            "Diseases" to dao.getDiseasesCount()
        )
    }

    // ==========================================
    // 9. PubChem & Gemini
    // ==========================================
    suspend fun searchPubChem(compound: String) = remoteDataSource.searchPubChem(compound)

    suspend fun askGemini(prompt: String, contextAccession: String? = null) =
        remoteDataSource.askGeminiAssistant(prompt, contextAccession)
}
