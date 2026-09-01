package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BioSeqDao
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        QueryEntity::class,
        QueryResultEntity::class,
        SequenceEntity::class,
        AnnotationEntity::class,
        OrthologEntity::class,
        DiseaseAssociationEntity::class,
        CrossReferenceEntity::class,
        StructureEntity::class,
        AnalysisMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BioSeqDatabase : RoomDatabase() {

    abstract fun bioSeqDao(): BioSeqDao

    companion object {
        @Volatile
        private var INSTANCE: BioSeqDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BioSeqDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BioSeqDatabase::class.java,
                    "bioseq_research_hub.db"
                )
                .addCallback(BioSeqDatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BioSeqDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.bioSeqDao())
                }
            }
        }

        suspend fun populateInitialData(dao: BioSeqDao) {
            val now = System.currentTimeMillis()

            // 1. Initial Queries
            dao.insertQuery(
                QueryEntity(
                    queryId = "Q_7157",
                    userId = "bodduteja2021@gmail.com",
                    queryType = "NCBI_GENE",
                    database = "NCBI Entrez",
                    queryText = "TP53 tumor protein p53",
                    parameters = "{\"taxid\": 9606, \"db\": \"gene\"}",
                    createdAt = now - 3600000,
                    updatedAt = now - 3600000,
                    status = "SUCCESS"
                )
            )
            dao.insertQuery(
                QueryEntity(
                    queryId = "Q_BRCA1",
                    userId = "bodduteja2021@gmail.com",
                    queryType = "UNIPROT_SPARQL",
                    database = "UniProt",
                    queryText = "SELECT ?protein ?name WHERE { ?protein up:mnemonic 'BRCA1_HUMAN' }",
                    parameters = "{\"endpoint\": \"sparql.uniprot.org/sparql\"}",
                    createdAt = now - 86400000,
                    updatedAt = now - 86400000,
                    status = "CACHED"
                )
            )

            // 2. Initial Sequences
            dao.insertSequences(
                listOf(
                    SequenceEntity(
                        sequenceId = "SEQ_P04637",
                        accession = "P04637",
                        sequenceType = "PROTEIN",
                        sequence = "MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGPDEAPRMPEAAPPVAPAPAAPTPAAPAPAPSWPLSSSVPSQKTYQGSYGFRLGFLHSGTAKSVTCTYSPALNKMFCQLAKTCPVQLWVDSTPPPGTRVRAMAIYKQSQHMTEVVRRCPHHERCSDSDGLAPPQHLIRVEGNLRVEYLDDRNTFRHSVVVPYEPPEVGSDCTTIHYNYMCNSSCMGGMNRRPILTIITLEDSSGNLLGRNSFEVRVCACPGRDRRTEEENLRKKGEPHHELPPGSTKRALPNNTSSSPQPKKKPLDGEYFTLQIRGRERFEMFRELNEALELKDAQAGKEPGGSRAHSSHLKSKKGQSTSRHKKLMFKTEGPDSD",
                        organism = "Homo sapiens",
                        length = 393,
                        source = "UniProt",
                        description = "Cellular tumor antigen p53, acts as a tumor suppressor in many tumor types",
                        gcContent = 0.0,
                        molecularWeightDa = 43653.0,
                        retrievedAt = now
                    ),
                    SequenceEntity(
                        sequenceId = "SEQ_P02340",
                        accession = "P02340",
                        sequenceType = "PROTEIN",
                        sequence = "MEESQSDISLELPLSQETFSGLWKLLPPEDILPSPHCMDDLLLPQDVEEFFEGPSEALRVSGAPAAQDPVTETPGPVAPAPATPWPLSSFVPSQKTYQGNYGFHLGFLQSGTAKSVMCTYSISLNKLFCQLAKTCPVQLWVDSTPPPGSRVRAMAIYKKSQHMTEVVRRCPHHERCSDGDGLAPPQHLIRVEGNLRAEYLDDRNTFRHSVVVPYESPEIESECETIHYNYMCNSSCMGGMNRRPILTIITLEDSSGNLLGRNSFEVRICACPGRDRRTEEKNFQKKGEPCPELPPKSAKRALPTNTSSSPQPKKKPLDGEFLTLKIRGRKRFEMFRELAEALELKDAHAAKESPGGSRAHSSHLKAKKGQSTSRHKKLMFKREGPDSD",
                        organism = "Mus musculus",
                        length = 390,
                        source = "UniProt",
                        description = "Cellular tumor antigen p53 (Mouse ortholog)",
                        gcContent = 0.0,
                        molecularWeightDa = 43410.0,
                        retrievedAt = now
                    ),
                    SequenceEntity(
                        sequenceId = "SEQ_NC_000017",
                        accession = "NC_000017.11",
                        sequenceType = "DNA",
                        sequence = "GATCGATCGATCGAATTCGCGCGCGATATACGCGCTAGCTAGCTAGCGCTAGCGATCGAATTCGATCGATCGAATTCGCGCGCGATATACGCGCTAGCTAGCTAGCGCTAGCGATCGAATTC",
                        organism = "Homo sapiens",
                        length = 124,
                        source = "NCBI Nucleotide",
                        description = "Homo sapiens chromosome 17, GRCh38.p14 Primary Assembly (TP53 Region)",
                        gcContent = 54.8,
                        molecularWeightDa = 40920.0,
                        retrievedAt = now
                    )
                )
            )

            // 3. Initial Annotations
            dao.insertAnnotations(
                listOf(
                    AnnotationEntity(
                        annotationId = "ANN_1",
                        sequenceId = "P04637",
                        source = "UniProt",
                        annotationType = "FUNCTION",
                        annotationText = "Acts as a tumor suppressor in all types of cancers; induces growth arrest or apoptosis depending on physiological circumstances.",
                        evidence = "ECO:0000269|PubMed:12524540",
                        startResidue = 1,
                        endResidue = 393,
                        retrievedAt = now
                    ),
                    AnnotationEntity(
                        annotationId = "ANN_2",
                        sequenceId = "P04637",
                        source = "InterPro",
                        annotationType = "DOMAIN",
                        annotationText = "p53 DNA-binding domain (IPR011615), coordinates Zn2+ ion essential for specific response element binding.",
                        evidence = "ECO:0000269|PDB:1TUP",
                        startResidue = 102,
                        endResidue = 292,
                        retrievedAt = now
                    ),
                    AnnotationEntity(
                        annotationId = "ANN_3",
                        sequenceId = "P04637",
                        source = "Pfam",
                        annotationType = "FAMILY",
                        annotationText = "P53 tetramerisation domain (PF07710), forms dimer-of-dimers configuration.",
                        evidence = "ECO:0000305|Curated",
                        startResidue = 325,
                        endResidue = 356,
                        retrievedAt = now
                    )
                )
            )

            // 4. Initial Orthologs
            dao.insertOrthologs(
                listOf(
                    OrthologEntity(
                        orthologId = "ORTH_TP53_MM",
                        sourceSequence = "TP53 (Human P04637)",
                        targetSequence = "Trp53 (Mouse P02340)",
                        sourceOrganism = "Homo sapiens",
                        targetOrganism = "Mus musculus",
                        identity = 81.2,
                        coverage = 98.5,
                        score = 642.0,
                        method = "Reciprocal BLAST (BioKt)",
                        alignment = "MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDP...\n||| ||| | |||||||||| |||||| | | |||   ||||| | | | | | | | ...\nMEESQSDISLELPLSQETFSGLWKLLPPEDILPSPHCMDDLLLPQDVEEFFEGPSEAL..."
                    ),
                    OrthologEntity(
                        orthologId = "ORTH_TP53_DR",
                        sourceSequence = "TP53 (Human P04637)",
                        targetSequence = "tp53 (Zebrafish Q9W678)",
                        sourceOrganism = "Homo sapiens",
                        targetOrganism = "Danio rerio",
                        identity = 53.4,
                        coverage = 91.2,
                        score = 420.0,
                        method = "Reciprocal BLAST (BioKt)",
                        alignment = "Conserved DNA-binding domain with high identity in loop L1 and zinc finger."
                    )
                )
            )

            // 5. Initial Disease Associations
            dao.insertDiseases(
                listOf(
                    DiseaseAssociationEntity(
                        associationId = "DIS_1",
                        geneId = "TP53",
                        proteinId = "P04637",
                        diseaseId = "OMIM:151623",
                        diseaseName = "Li-Fraumeni Syndrome 1 (LFS1)",
                        source = "OMIM / DisGeNET",
                        score = 0.94,
                        evidence = "Germline mutations predispose to multiple early-onset neoplasms (PubMed:2172295)."
                    ),
                    DiseaseAssociationEntity(
                        associationId = "DIS_2",
                        geneId = "TP53",
                        proteinId = "P04637",
                        diseaseId = "UMLS:C0006826",
                        diseaseName = "Colorectal Neoplasms & Carcinoma",
                        source = "DisGeNET",
                        score = 0.88,
                        evidence = "Somatic missense mutations in hotspot codons R175, R248, R273."
                    ),
                    DiseaseAssociationEntity(
                        associationId = "DIS_3",
                        geneId = "BRCA1",
                        proteinId = "P38398",
                        diseaseId = "OMIM:604370",
                        diseaseName = "Hereditary Breast and Ovarian Cancer Syndrome",
                        source = "OMIM / ClinVar",
                        score = 0.96,
                        evidence = "Pathogenic truncating variants compromise homologous recombination DNA repair."
                    )
                )
            )

            // 6. Cross Referencing
            dao.insertCrossReferences(
                listOf(
                    CrossReferenceEntity(
                        crossReferenceId = "XREF_TP53",
                        geneSymbol = "TP53",
                        sourceDatabase = "NCBI Gene",
                        sourceIdentifier = "7157",
                        targetDatabase = "UniProt",
                        targetIdentifier = "P04637",
                        relationship = "ENCODES",
                        fullRecordJson = "{\"ncbi_protein\": \"NP_000537.3\", \"pdb\": \"1TUP\", \"pubmed\": [\"25732183\", \"12524540\"], \"pubchem\": \"CID_2244\"}"
                    ),
                    CrossReferenceEntity(
                        crossReferenceId = "XREF_EGFR",
                        geneSymbol = "EGFR",
                        sourceDatabase = "NCBI Gene",
                        sourceIdentifier = "1956",
                        targetDatabase = "UniProt",
                        targetIdentifier = "P00533",
                        relationship = "ENCODES",
                        fullRecordJson = "{\"ncbi_protein\": \"NP_005219.2\", \"pdb\": \"1M17\", \"pubmed\": [\"15118073\"], \"pubchem\": \"CID_176870\"}"
                    )
                )
            )

            // 7. Structures (PDB 1TUP & PubChem Aspirin)
            dao.insertStructure(
                StructureEntity(
                    structureId = "1TUP",
                    source = "RCSB PDB",
                    sourceIdentifier = "1TUP",
                    accession = "P04637",
                    structureType = "PROTEIN",
                    moleculeName = "Tumor Suppressor p53 Complexed with DNA",
                    organism = "Homo sapiens",
                    chainCount = 3,
                    resolution = "2.2 Å (X-ray Diffraction)",
                    chainsJson = "[\"A\", \"B\", \"C\"]",
                    pdbContent = "HEADER    TRANSCRIPTION/DNA                       20-APR-94   1TUP\nCOMPND    MOL_ID: 1; MOLECULE: TUMOR SUPPRESSOR P53; CHAIN: A, B, C\nATOM      1  N   GLU A  96      23.211  15.123  42.110  1.00 24.12           N\nATOM      2  CA  GLU A  96      24.110  16.023  41.220  1.00 23.89           C\nATOM      3  C   GLU A  96      25.420  15.220  40.900  1.00 22.10           C\nATOM      4  O   GLU A  96      25.890  14.410  41.710  1.00 21.80           O\nATOM      5  CB  GLU A  96      23.510  16.510  39.880  1.00 25.10           C\nATOM      6  N   VAL A  97      26.010  15.420  39.730  1.00 20.40           N\nATOM      7  CA  VAL A  97      27.280  14.730  39.310  1.00 19.50           C\nATOM      8  C   VAL A  97      28.410  15.710  39.020  1.00 18.90           C\nATOM      9  O   VAL A  97      29.560  15.340  38.810  1.00 18.20           O\nEND",
                    retrievedAt = now
                )
            )
            dao.insertStructure(
                StructureEntity(
                    structureId = "CID_2244",
                    source = "PubChem 3D",
                    sourceIdentifier = "2244",
                    accession = "CID_2244",
                    structureType = "SMALL_MOLECULE",
                    moleculeName = "Aspirin (Acetylsalicylic Acid)",
                    organism = "Synthetic",
                    chainCount = 1,
                    resolution = "Computed 3D Conformer",
                    chainsJson = "[\"MOL\"]",
                    pdbContent = "HETATM    1  C1  MOL     1      -1.220   0.450   0.000  1.00  0.00           C\nHETATM    2  C2  MOL     1      -0.050   1.210   0.000  1.00  0.00           C\nHETATM    3  O3  MOL     1       1.120   0.480   0.000  1.00  0.00           O\nHETATM    4  C4  MOL     1       1.240  -0.880   0.000  1.00  0.00           C\nHETATM    5  O5  MOL     1       0.280  -1.630   0.000  1.00  0.00           O\nEND",
                    retrievedAt = now
                )
            )
        }
    }
}
