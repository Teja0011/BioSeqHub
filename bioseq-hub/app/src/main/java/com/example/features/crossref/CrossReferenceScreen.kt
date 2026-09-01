package com.example.features.crossref

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import com.example.domain.model.CrossReferenceRecord
import com.example.ui.theme.TertiaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossReferenceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchIdentifier by remember { mutableStateOf("TP53") }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    val crossRefs = remember {
        listOf(
            CrossReferenceRecord(
                crossReferenceId = "XREF_TP53",
                geneSymbol = "TP53",
                ncbiGeneId = "7157",
                uniprotAccession = "P04637",
                ncbiProteinAccession = "NP_000537.3",
                pubchemCid = "2244 (Aspirin Modulator)",
                pubmedPmids = listOf("25732183", "12524540", "2172295"),
                pdbStructures = listOf("1TUP", "1TSR", "2AC0", "3KMD"),
                ensemblId = "ENSG00000141510"
            ),
            CrossReferenceRecord(
                crossReferenceId = "XREF_BRCA1",
                geneSymbol = "BRCA1",
                ncbiGeneId = "672",
                uniprotAccession = "P38398",
                ncbiProteinAccession = "NP_009225.1",
                pubchemCid = "49867990 (Olaparib)",
                pubmedPmids = listOf("24336040", "11805828"),
                pdbStructures = listOf("1JNX", "4OFB", "1N5O"),
                ensemblId = "ENSG00000012048"
            ),
            CrossReferenceRecord(
                crossReferenceId = "XREF_EGFR",
                geneSymbol = "EGFR",
                ncbiGeneId = "1956",
                uniprotAccession = "P00533",
                ncbiProteinAccession = "NP_005219.2",
                pubchemCid = "123631 (Gefitinib)",
                pubmedPmids = listOf("15118073", "15118074"),
                pdbStructures = listOf("1M17", "2ITY", "4HJO"),
                ensemblId = "ENSG00000146648"
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("13. 🔗 Cross-Referencing", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeader(
                    title = "Universal Identifier Cross-Referencing",
                    subtitle = "Map identifiers across NCBI, UniProt, PubChem, PubMed, PDB, and Ensembl"
                )
            }

            // Search Filter
            item {
                OutlinedTextField(
                    value = searchIdentifier,
                    onValueChange = { searchIdentifier = it },
                    label = { Text("Search Gene Symbol or Database ID") },
                    modifier = Modifier.fillMaxWidth().testTag("xref_search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Status Message
            if (exportStatus != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TertiaryDark.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TertiaryDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = exportStatus ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Export Action
            item {
                Button(
                    onClick = {
                        exportStatus = "Synchronized cross-references to Google Sheets 'CrossReferencing' tab."
                    },
                    modifier = Modifier.fillMaxWidth().testTag("sync_xref_sheets_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync to Google Sheets (CrossReferencing Tab)")
                }
            }

            // Cross-Reference Cards
            items(crossRefs) { ref ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ref.geneSymbol,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Badge {
                                Text("Ensembl: ${ref.ensemblId}")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        XrefRow(label = "NCBI Gene ID", value = ref.ncbiGeneId, badge = "NCBI")
                        XrefRow(label = "UniProt Accession", value = ref.uniprotAccession, badge = "UniProt")
                        XrefRow(label = "NCBI Protein RefSeq", value = ref.ncbiProteinAccession, badge = "NCBI")
                        XrefRow(label = "PubChem Modulator", value = ref.pubchemCid ?: "N/A", badge = "PubChem")

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "3D PDB Structures:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            ref.pdbStructures.forEach { pdb ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = pdb,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun XrefRow(
    label: String,
    value: String,
    badge: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
        ProvenanceBadge(sourceName = badge)
    }
}
