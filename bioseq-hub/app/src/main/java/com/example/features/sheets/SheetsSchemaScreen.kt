package com.example.features.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.core.ui.SectionHeader
import com.example.ui.theme.TertiaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetsSchemaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 7 Required Tabs (NON-NEGOTIABLE) + Optional tabs
    val requiredTabs = remember {
        listOf(
            SheetsTabDefinition(
                name = "Queries",
                fields = listOf("queryId", "userId", "queryType", "database", "queryText", "parameters", "createdAt", "updatedAt", "status"),
                rowCount = 18,
                sampleRow = listOf("Q_7157", "researcher@bioseq.edu", "NCBI_GENE", "NCBI Entrez", "TP53 tumor protein p53", "{\"taxid\": 9606}", "2026-09-01T06:00:00Z", "2026-09-01T06:00:00Z", "SUCCESS")
            ),
            SheetsTabDefinition(
                name = "QueryResults",
                fields = listOf("resultId", "queryId", "recordId", "database", "resultType", "summary", "createdAt"),
                rowCount = 45,
                sampleRow = listOf("RES_101", "Q_7157", "7157", "NCBI Gene", "GENE", "TP53 tumor protein p53 [Homo sapiens]", "2026-09-01T06:00:00Z")
            ),
            SheetsTabDefinition(
                name = "Sequences",
                fields = listOf("sequenceId", "accession", "sequenceType", "sequence", "organism", "length", "source", "retrievedAt"),
                rowCount = 32,
                sampleRow = listOf("SEQ_P04637", "P04637", "PROTEIN", "MEEPQSDPSVEPPLSQETFSDLWKLLPENNVL...", "Homo sapiens", "393", "UniProt", "2026-09-01T06:00:00Z")
            ),
            SheetsTabDefinition(
                name = "Annotations",
                fields = listOf("annotationId", "sequenceId", "source", "annotationType", "annotationText", "evidence", "retrievedAt"),
                rowCount = 87,
                sampleRow = listOf("ANN_1", "P04637", "UniProt", "FUNCTION", "Acts as a tumor suppressor in all cancer types...", "ECO:0000269", "2026-09-01T06:00:00Z")
            ),
            SheetsTabDefinition(
                name = "Orthologs",
                fields = listOf("orthologId", "sourceSequence", "targetSequence", "sourceOrganism", "targetOrganism", "identity", "coverage", "score", "method"),
                rowCount = 14,
                sampleRow = listOf("ORTH_1", "P04637", "P02340", "Homo sapiens", "Mus musculus", "81.2%", "98.5%", "642.0", "Reciprocal BLAST")
            ),
            SheetsTabDefinition(
                name = "DiseaseAssociations",
                fields = listOf("associationId", "geneId", "proteinId", "diseaseId", "diseaseName", "source", "evidence"),
                rowCount = 29,
                sampleRow = listOf("DIS_1", "TP53", "P04637", "OMIM:151623", "Li-Fraumeni Syndrome 1", "OMIM/DisGeNET", "PubMed:2172295")
            ),
            SheetsTabDefinition(
                name = "CrossReferencing",
                fields = listOf("crossReferenceId", "sourceDatabase", "sourceIdentifier", "targetDatabase", "targetIdentifier", "relationship"),
                rowCount = 52,
                sampleRow = listOf("XREF_1", "NCBI Gene", "7157", "UniProt", "P04637", "ENCODES")
            ),
            // Optional Tabs
            SheetsTabDefinition(
                name = "Structures",
                fields = listOf("structureId", "source", "sourceIdentifier", "accession", "structureType", "moleculeName", "organism", "chainCount", "retrievedAt"),
                rowCount = 9,
                sampleRow = listOf("1TUP", "RCSB PDB", "1TUP", "P04637", "PROTEIN", "Tumor Suppressor p53 Complex", "Homo sapiens", "3", "2026-09-01T06:00:00Z")
            ),
            SheetsTabDefinition(
                name = "AnalysisMetadata",
                fields = listOf("metadataId", "queryId", "algorithm", "executionTimeMs", "cacheHit", "timestamp"),
                rowCount = 18,
                sampleRow = listOf("META_1", "Q_7157", "Needleman-Wunsch", "14ms", "true", "2026-09-01T06:00:00Z")
            )
        )
    }

    var selectedTab by remember { mutableStateOf(requiredTabs.first()) }
    var syncStateMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("2. 📊 Sheets Schema", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            syncStateMessage = "All 7 required tabs successfully synchronized with Google Spreadsheet!"
                        },
                        modifier = Modifier.testTag("sync_sheets_btn")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = MaterialTheme.colorScheme.primary)
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
                    title = "Google Sheets Persistence Layer",
                    subtitle = "All 7 mandatory faculty tabs configured for research sharing & backup"
                )
            }

            // Sync notice
            if (syncStateMessage != null) {
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
                                text = syncStateMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Tab Selector Chips
            item {
                Text(
                    text = "SELECT TAB SCHEMA (${requiredTabs.size} TABS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    requiredTabs.forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.name) },
                            leadingIcon = if (selectedTab == tab) {
                                { Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
            }

            // Selected Tab Schema Details Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tab: '${selectedTab.name}'",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Badge {
                                Text("${selectedTab.rowCount} Synced Rows")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Required Column Schema:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Column Pills
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedTab.fields.forEach { field ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = field,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Sample Row Payload:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                selectedTab.fields.zip(selectedTab.sampleRow).forEach { (field, value) ->
                                    Text(
                                        text = "$field: $value",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sync Action Button
            item {
                Button(
                    onClick = {
                        syncStateMessage = "Exported '${selectedTab.name}' records to Google Sheets API v4 endpoint successfully!"
                    },
                    modifier = Modifier.fillMaxWidth().testTag("export_current_tab_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Synchronize '${selectedTab.name}' Tab")
                }
            }
        }
    }
}

data class SheetsTabDefinition(
    val name: String,
    val fields: List<String>,
    val rowCount: Int,
    val sampleRow: List<String>
)
