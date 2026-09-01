# Database Schema Documentation

BioSeq Research Hub uses **Room** as its primary offline-first local persistence engine and **Google Sheets API v4** as its cloud research sharing and backup schema.

## 📊 Google Sheets Required Tabs & Columns

### 1. `Queries`
- `queryId` (String): Unique query identifier
- `userId` (String): Google OAuth researcher ID
- `queryType` (String): e.g. NCBI_GENE, UNIPROT_SPARQL, ORTHOLOG, BATCH_FASTA
- `database` (String): NCBI, UniProt, PubChem, PubMed, DisGeNET
- `queryText` (String): Raw search text or SPARQL query
- `parameters` (String): JSON-encoded query filters and parameters
- `createdAt` (Long/ISO8601): Timestamp of initial query
- `updatedAt` (Long/ISO8601): Timestamp of latest execution
- `status` (String): SUCCESS, PENDING, FAILED, CACHED

### 2. `QueryResults`
- `resultId` (String): Primary key
- `queryId` (String): Foreign reference to Queries
- `recordId` (String): Biological record accession (e.g., P04637, 7157)
- `database` (String): Source database
- `resultType` (String): PROTEIN, GENE, COMPOUND, PUBLICATION
- `summary` (String): Human-readable biological summary
- `createdAt` (Long): Retrieval timestamp

### 3. `Sequences`
- `sequenceId` (String): Primary key
- `accession` (String): e.g., NP_000537.3, P04637
- `sequenceType` (String): AMINO_ACID, DNA, RNA
- `sequence` (String): FASTA formatted or raw sequence characters
- `organism` (String): e.g., Homo sapiens
- `length` (Int): Residue count
- `source` (String): NCBI / UniProt
- `retrievedAt` (Long): Timestamp

### 4. `Annotations`
- `annotationId` (String): Primary key
- `sequenceId` (String): Reference sequence or accession
- `source` (String): UniProt, InterPro, Pfam
- `annotationType` (String): DOMAIN, FUNCTION, INTERACTION, PTM
- `annotationText` (String): Curated description
- `evidence` (String): Experimental (ECO:0000269) or computational
- `retrievedAt` (Long): Timestamp

### 5. `Orthologs`
- `orthologId` (String): Primary key
- `sourceSequence` (String): Source gene/accession
- `targetSequence` (String): Orthologous target gene/accession
- `sourceOrganism` (String): Source species
- `targetOrganism` (String): Target species
- `identity` (Double): Sequence identity percentage (0.00 - 100.00)
- `coverage` (Double): Alignment coverage percentage
- `score` (Double): Bit score / alignment score
- `method` (String): Reciprocal BLAST / BioKt Needleman-Wunsch

### 6. `DiseaseAssociations`
- `associationId` (String): Primary key
- `geneId` (String): Associated gene symbol or Entrez ID
- `proteinId` (String): UniProt accession
- `diseaseId` (String): OMIM / UMLS / MeSH ID
- `diseaseName` (String): Clinical disease name
- `source` (String): DisGeNET / OMIM / ClinVar
- `evidence` (String): Score and PubMed citations

### 7. `CrossReferencing`
- `crossReferenceId` (String): Primary key
- `sourceDatabase` (String): NCBI, UniProt, PubChem, PDB
- `sourceIdentifier` (String): Source accession / CID / ID
- `targetDatabase` (String): Target database name
- `targetIdentifier` (String): Target accession / CID / ID
- `relationship` (String): ENCODES, HOMOLOG, INTERACTS_WITH, BINDS, STRUCTURE_FOR

---

## 💾 Room Entity Architecture

The local SQLite schema mirrors the above domain structures with indexed columns and primary keys to guarantee offline instantaneous queries and local caching.
