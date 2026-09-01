package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BioSeqDao {

    // Query Entity
    @Query("SELECT * FROM queries ORDER BY createdAt DESC")
    fun getAllQueries(): Flow<List<QueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: QueryEntity)

    @Delete
    suspend fun deleteQuery(query: QueryEntity)

    @Query("DELETE FROM queries WHERE queryId = :queryId")
    suspend fun deleteQueryById(queryId: String)

    // Sequences
    @Query("SELECT * FROM sequences ORDER BY retrievedAt DESC")
    fun getAllSequences(): Flow<List<SequenceEntity>>

    @Query("SELECT * FROM sequences WHERE accession = :accession OR sequenceId = :accession LIMIT 1")
    suspend fun getSequenceByAccession(accession: String): SequenceEntity?

    @Query("SELECT * FROM sequences WHERE organism LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR accession LIKE '%' || :query || '%'")
    fun searchSequences(query: String): Flow<List<SequenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequence(sequence: SequenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSequences(sequences: List<SequenceEntity>)

    // Annotations
    @Query("SELECT * FROM annotations WHERE sequenceId = :sequenceId OR sequenceId = :accession")
    fun getAnnotationsForSequence(sequenceId: String, accession: String = sequenceId): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations ORDER BY retrievedAt DESC")
    fun getAllAnnotations(): Flow<List<AnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotations(annotations: List<AnnotationEntity>)

    // Orthologs
    @Query("SELECT * FROM orthologs WHERE sourceSequence = :gene OR targetSequence = :gene")
    fun getOrthologsForGene(gene: String): Flow<List<OrthologEntity>>

    @Query("SELECT * FROM orthologs ORDER BY identity DESC")
    fun getAllOrthologs(): Flow<List<OrthologEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrthologs(orthologs: List<OrthologEntity>)

    // Disease Associations
    @Query("SELECT * FROM disease_associations WHERE geneId = :geneId OR proteinId = :geneId OR diseaseName LIKE '%' || :geneId || '%'")
    fun getDiseasesForGene(geneId: String): Flow<List<DiseaseAssociationEntity>>

    @Query("SELECT * FROM disease_associations ORDER BY score DESC")
    fun getAllDiseases(): Flow<List<DiseaseAssociationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseases(diseases: List<DiseaseAssociationEntity>)

    // Cross Referencing
    @Query("SELECT * FROM cross_referencing WHERE geneSymbol = :symbol OR sourceIdentifier = :symbol OR targetIdentifier = :symbol")
    fun getCrossReferences(symbol: String): Flow<List<CrossReferenceEntity>>

    @Query("SELECT * FROM cross_referencing")
    fun getAllCrossReferences(): Flow<List<CrossReferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossReferences(xrefs: List<CrossReferenceEntity>)

    // 3D Structures
    @Query("SELECT * FROM structures WHERE structureId = :structureId OR accession = :structureId LIMIT 1")
    suspend fun getStructure(structureId: String): StructureEntity?

    @Query("SELECT * FROM structures ORDER BY retrievedAt DESC")
    fun getAllStructures(): Flow<List<StructureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStructure(structure: StructureEntity)

    // Analysis Metadata / Metrics
    @Query("SELECT COUNT(*) FROM queries")
    suspend fun getQueriesCount(): Int

    @Query("SELECT COUNT(*) FROM sequences")
    suspend fun getSequencesCount(): Int

    @Query("SELECT COUNT(*) FROM annotations")
    suspend fun getAnnotationsCount(): Int

    @Query("SELECT COUNT(*) FROM structures")
    suspend fun getStructuresCount(): Int

    @Query("SELECT COUNT(*) FROM disease_associations")
    suspend fun getDiseasesCount(): Int

    @Query("DELETE FROM queries")
    suspend fun clearQueries()

    @Query("DELETE FROM sequences")
    suspend fun clearSequences()
}
