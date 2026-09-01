package com.example.core.navigation

sealed class Screen(val route: String, val title: String, val iconEmoji: String) {
    // Primary Tab Destinations
    object Home : Screen("home", "BioSeq Hub", "🧬")
    object Search : Screen("search", "Global Search", "🔍")
    object HistoryTab : Screen("history_tab", "History", "🕘")
    object Profile : Screen("profile", "Researcher Profile", "👤")

    // The 15 Mandatory Faculty Features
    object Login : Screen("login", "Login Portal", "🔐")
    object GoogleSignIn : Screen("auth", "Google Sign-In", "🔐")
    object SheetsSchema : Screen("sheets", "Sheets Schema", "📊")
    object RoomDatabase : Screen("room", "Room Database", "💾")
    object GoogleApis : Screen("googleapis", "Google APIs", "☁️")
    object NcbiEntrez : Screen("ncbi", "NCBI Entrez", "🧬")
    object UniprotSparql : Screen("uniprot", "UniProt SPARQL", "🧬")
    object BatchRetrieval : Screen("batch", "Batch Sequence Retrieval", "📦")
    object AnnotationAggregator : Screen("annotation", "Annotation Aggregator", "🧩")
    object OrthologFinder : Screen("ortholog", "Ortholog Finder", "🔬")
    object ParalogDetector : Screen("paralog", "Paralog Detector", "🔀")
    object FunctionalAnnotation : Screen("functional", "Functional Annotation", "📝")
    object DiseaseAssociations : Screen("disease", "Disease Associations", "🦠")
    object CrossReference : Screen("crossreference", "Cross-Database Linking", "🔗")
    object ResultVisualization : Screen("visualization", "Result Visualization", "🕸️")
    object QueryHistory : Screen("history", "Query History & Reproducibility", "🕘")

    // Additional Features
    object BioKtLaboratory : Screen("biokt", "BioKt Suite", "🧬")
    object GeminiAssistant : Screen("gemini", "Gemini Research Assistant", "🤖")
    object Structure3D : Screen("structure3d", "3D Structure Viewer", "🧬")
    object Settings : Screen("settings", "Settings & Sync", "⚙️")
}

data class FeatureCardItem(
    val id: String,
    val title: String,
    val description: String,
    val route: String,
    val emoji: String,
    val tag: String,
    val isMandatory: Boolean = true
)

object FeatureRegistry {
    val mandatoryFacultyFeatures = listOf(
        FeatureCardItem(
            id = "1",
            title = "1. 🔐 Google Sign-In",
            description = "OAuth 2.0 research authorization, Drive & Sheets cloud credentials",
            route = Screen.GoogleSignIn.route,
            emoji = "🔐",
            tag = "AUTH"
        ),
        FeatureCardItem(
            id = "2",
            title = "2. 📊 Sheets Schema",
            description = "7 required cloud tabs: Queries, Results, Sequences, Annotations, Orthologs, Diseases, CrossRef",
            route = Screen.SheetsSchema.route,
            emoji = "📊",
            tag = "SHEETS"
        ),
        FeatureCardItem(
            id = "3",
            title = "3. 💾 Room Database",
            description = "Local offline-first SQLite cache, entity inspection, and cache hit metrics",
            route = Screen.RoomDatabase.route,
            emoji = "💾",
            tag = "CACHE"
        ),
        FeatureCardItem(
            id = "4",
            title = "4. ☁️ Google APIs",
            description = "Collaborative research sync, Google Drive workspace, and cloud replication",
            route = Screen.GoogleApis.route,
            emoji = "☁️",
            tag = "CLOUD"
        ),
        FeatureCardItem(
            id = "5",
            title = "5. 🧬 NCBI Entrez",
            description = "Live eUtils search for PubMed, Gene, Nucleotide, and Protein accessions",
            route = Screen.NcbiEntrez.route,
            emoji = "🧬",
            tag = "NCBI"
        ),
        FeatureCardItem(
            id = "6",
            title = "6. 🧬 UniProt SPARQL",
            description = "SPARQL query editor, semantic SPARQL templates, and RDF triple endpoints",
            route = Screen.UniprotSparql.route,
            emoji = "🧬",
            tag = "SPARQL"
        ),
        FeatureCardItem(
            id = "7",
            title = "7. 📦 Batch Sequence Retrieval",
            description = "Bulk accession resolver, progress tracker, retry failures, and FASTA export",
            route = Screen.BatchRetrieval.route,
            emoji = "📦",
            tag = "BATCH"
        ),
        FeatureCardItem(
            id = "8",
            title = "8. 🧩 Annotation Aggregator",
            description = "Aggregated UniProt, InterPro, and Pfam domains with source provenance badges",
            route = Screen.AnnotationAggregator.route,
            emoji = "🧩",
            tag = "PROVENANCE"
        ),
        FeatureCardItem(
            id = "9",
            title = "9. 🔬 Ortholog Finder",
            description = "Cross-species reciprocal sequence alignment (BioKt), coverage & identity %",
            route = Screen.OrthologFinder.route,
            emoji = "🔬",
            tag = "ORTHOLOG"
        ),
        FeatureCardItem(
            id = "10",
            title = "10. 🔀 Paralog Detector",
            description = "Within-genome duplication detection, similarity analysis, and cluster mapping",
            route = Screen.ParalogDetector.route,
            emoji = "🔀",
            tag = "PARALOG"
        ),
        FeatureCardItem(
            id = "11",
            title = "11. 📝 Functional Annotation",
            description = "Gene Ontology (BP, MF, CC), catalytic activity, and clearly labeled AI summaries",
            route = Screen.FunctionalAnnotation.route,
            emoji = "📝",
            tag = "GO_TERMS"
        ),
        FeatureCardItem(
            id = "12",
            title = "12. 🦠 Disease Associations",
            description = "DisGeNET, OMIM, and ClinVar phenotype-genotype associations & evidence",
            route = Screen.DiseaseAssociations.route,
            emoji = "🦠",
            tag = "DISEASE"
        ),
        FeatureCardItem(
            id = "13",
            title = "13. 🔗 Cross-Database Linking",
            description = "Interactive cross-identifier mapper (Gene ↔ UniProt ↔ NCBI ↔ PubChem ↔ PDB)",
            route = Screen.CrossReference.route,
            emoji = "🔗",
            tag = "XREF"
        ),
        FeatureCardItem(
            id = "14",
            title = "14. 🕸️ Result Visualization",
            description = "Interactive 2D biological network graph with draggable nodes, zoom, pan & filters",
            route = Screen.ResultVisualization.route,
            emoji = "🕸️",
            tag = "NETWORK"
        ),
        FeatureCardItem(
            id = "15",
            title = "15. 🕘 Query History & Reproducibility",
            description = "Time-categorized query ledger (Today/Yesterday/Week) with 1-tap exact replay",
            route = Screen.QueryHistory.route,
            emoji = "🕘",
            tag = "REPRODUCIBLE"
        )
    )

    val additionalFeatures = listOf(
        FeatureCardItem(
            id = "16",
            title = "16. 🧬 BioKt Bioinformatics Suite",
            description = "High-performance sequence toolkit, FASTA/FASTQ IO, MSA, ORF finder, primers & phylogeny",
            route = Screen.BioKtLaboratory.route,
            emoji = "🧬",
            tag = "BIOKT",
            isMandatory = false
        ),
        FeatureCardItem(
            id = "17",
            title = "17. 🤖 Gemini Research Assistant",
            description = "Context-aware AI genomics hypothesis generator, sequence explainer & summarizer",
            route = Screen.GeminiAssistant.route,
            emoji = "🤖",
            tag = "GEMINI_AI",
            isMandatory = false
        ),
        FeatureCardItem(
            id = "18",
            title = "18. 🧬 3D Structure Viewer",
            description = "Interactive 3D molecular renderer for PDB proteins & PubChem 3D conformers",
            route = Screen.Structure3D.route,
            emoji = "🧬",
            tag = "3D_VIEW",
            isMandatory = false
        )
    )

    val allFeatures = mandatoryFacultyFeatures + additionalFeatures
}
