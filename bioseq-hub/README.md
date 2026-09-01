# BioSeq Research Hub

BioSeq Research Hub is a modern bioinformatics and genomics research application designed for university students, faculty researchers, and laboratory scientists.

The application integrates sequence analysis, database retrieval (NCBI, UniProt, PubChem, PubMed), ortholog and paralog detection, annotation aggregation, interactive 3D molecular structure visualization, biological network graphs, offline Room persistence, Google Sheets cloud synchronization, and a context-aware Gemini AI research assistant.

---

## 🏛️ System Architecture

```
                    BIOSEQ RESEARCH HUB

                         ANDROID
                           │
                 Kotlin + Compose
                           │
                    ViewModels
                           │
                      Domain
                           │
                    Repository
                           │
             ┌─────────────┴─────────────┐
             │                           │
            Room                    FastAPI Backend
             │                           │
       Offline Cache                     │
                                         │
       ┌─────────────────────────────────┼───────────────┐
       │                 │               │               │
      NCBI            UniProt         PubChem          PubMed
       │                 │               │               │
       └─────────────────┴───────────────┴───────────────┘
                                         │
                                      Gemini
                                         │
                                         ▼
                                  Research Context
                                         │
                       ┌─────────────────┴─────────────┐
                       │                               │
                 Google Sheets                    Firebase
                 Cloud Backup                  FCM/Config/Storage
                       │
                       ▼
                  Researcher
```

---

## 📋 The 15 Mandatory Faculty Feature Modules

Every feature has its own dedicated card, screen, state model, Room entity, and domain use case:

1. **🔐 Google Sign-In** (`/auth`) - OAuth 2.0 researcher profile, Drive & Sheets authorization.
2. **📊 Sheets Schema** (`/sheets`) - 7 required tabs: *Queries, QueryResults, Sequences, Annotations, Orthologs, DiseaseAssociations, CrossReferencing*.
3. **💾 Room Database** (`/room`) - Offline-first caching engine with live database metrics and table inspect.
4. **☁️ Google APIs** (`/googleapis`) - Cloud sync orchestration for collaborative genomics research.
5. **🧬 NCBI Entrez** (`/ncbi`) - Gene, Nucleotide, Protein, and PubMed eUtils queries.
6. **🧬 UniProt SPARQL** (`/uniprot`) - SPARQL query builder and semantic endpoint runner.
7. **📦 Batch Sequence Retrieval** (`/batch`) - Bulk accession resolver with live progress tracking & FASTA export.
8. **🧩 Annotation Aggregator** (`/annotation`) - Aggregates UniProt, InterPro, and Pfam with provenance badges `[UniProt]`, `[InterPro]`, `[Pfam]`.
9. **🔬 Ortholog Finder** (`/ortholog`) - Cross-species reciprocal BLAST sequence comparison & pairwise alignment.
10. **🔀 Paralog Detector** (`/paralog`) - Within-genome duplication detection, similarity analysis, and cluster mapping.
11. **📝 Functional Annotation** (`/functional`) - Gene Ontology terms (BP, MF, CC) with clearly labeled AI summaries.
12. **🦠 Disease Associations** (`/disease`) - DisGeNET, OMIM, and ClinVar association scores and evidence links.
13. **🔗 Cross-Database Linking** (`/crossreference`) - Interactive ID cross-mapper (Gene ID ↔ Protein ↔ UniProt ↔ NCBI ↔ PubChem CID ↔ PubMed PMID ↔ PDB).
14. **🕸️ Result Visualization** (`/visualization`) - Interactive drag-and-zoom biological network graph.
15. **🕘 Query History & Reproducibility** (`/history`) - Timestamped query ledger categorized by *Today, Yesterday, This Week, Older* with 1-tap exact replay.

### 🌟 Additional Key Modules
16. **🤖 Gemini Research Assistant** (`/gemini`) - Context-aware biological query summarizer and hypothesis generator.
17. **🧬 3D Structure Viewer** (`/structure3d`) - Custom interactive Canvas 3D molecular renderer for PDB proteins and PubChem compounds (Ribbon, Ball & Stick, Space Filling, Surface).

---

## 🛠️ Technology Stack

- **Android Frontend**: Kotlin, Jetpack Compose, Material 3, ViewModel, Coroutines, Flow, Room, Retrofit 2, OkHttp 3, Moshi, Navigation Compose.
- **Bioinformatics Engine (BioKt inspired)**: Needle-Wunsch & Smith-Waterman pairwise alignment, BLOSUM62/PAM250 scoring, GC content calculation, 6-frame translation, reciprocal orthology detector.
- **Backend**: Python 3.11, FastAPI, Pydantic, Uvicorn, httpx, Biopython.
- **Cloud & Auth**: Google Sign-In OAuth 2.0, Google Sheets API v4, Firebase FCM & Remote Config.

---

## 🚀 Running the App

### Android
Open the project in Android Studio or compile directly:
```bash
gradle :app:assembleDebug
```

### Backend
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```
