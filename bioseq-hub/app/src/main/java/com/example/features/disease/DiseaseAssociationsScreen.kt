package com.example.features.disease

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import com.example.domain.model.DiseaseAssociation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseAssociationsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var geneSymbol by remember { mutableStateOf("TP53") }
    var selectedSource by remember { mutableStateOf("All") }

    val sources = listOf("All", "DisGeNET", "OMIM", "ClinVar")

    val diseaseList = remember {
        listOf(
            DiseaseAssociation(
                associationId = "DIS_1",
                geneId = "TP53",
                proteinId = "P04637",
                diseaseId = "OMIM:151623",
                diseaseName = "Li-Fraumeni Syndrome 1 (LFS1)",
                source = "OMIM",
                score = 0.94,
                evidencePublications = listOf("PubMed:2172295", "PubMed:8945484")
            ),
            DiseaseAssociation(
                associationId = "DIS_2",
                geneId = "TP53",
                proteinId = "P04637",
                diseaseId = "C0006826",
                diseaseName = "Malignant Neoplasm of Breast",
                source = "DisGeNET",
                score = 0.89,
                evidencePublications = listOf("PubMed:12524540", "PubMed:25732183")
            ),
            DiseaseAssociation(
                associationId = "DIS_3",
                geneId = "TP53",
                proteinId = "P04637",
                diseaseId = "VCV000012351",
                diseaseName = "Adrenocortical Carcinoma, Hereditary",
                source = "ClinVar",
                score = 0.85,
                evidencePublications = listOf("PubMed:11598179")
            ),
            DiseaseAssociation(
                associationId = "DIS_4",
                geneId = "TP53",
                proteinId = "P04637",
                diseaseId = "C0029925",
                diseaseName = "Osteosarcoma",
                source = "DisGeNET",
                score = 0.82,
                evidencePublications = listOf("PubMed:15199141")
            ),
            DiseaseAssociation(
                associationId = "DIS_5",
                geneId = "TP53",
                proteinId = "P04637",
                diseaseId = "OMIM:609265",
                diseaseName = "Choroid Plexus Carcinoma",
                source = "OMIM",
                score = 0.78,
                evidencePublications = listOf("PubMed:16885370")
            )
        )
    }

    val filteredList = diseaseList.filter {
        selectedSource == "All" || it.source.equals(selectedSource, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("12. 🏥 Disease Associations", fontWeight = FontWeight.Bold) },
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
                    title = "Genomics Disease Association Engine",
                    subtitle = "DisGeNET, OMIM, and ClinVar curated pathogenic links with association scoring"
                )
            }

            // Target selector
            item {
                OutlinedTextField(
                    value = geneSymbol,
                    onValueChange = { geneSymbol = it },
                    label = { Text("Gene Target Symbol") },
                    modifier = Modifier.fillMaxWidth().testTag("disease_gene_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Source Filter Chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sources.forEach { src ->
                        FilterChip(
                            selected = selectedSource == src,
                            onClick = { selectedSource = src },
                            label = { Text(src) }
                        )
                    }
                }
            }

            // Disease Cards
            items(filteredList) { dis ->
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
                            ProvenanceBadge(sourceName = dis.source)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Score: ${String.format("%.2f", dis.score)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = dis.diseaseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Disease ID: ${dis.diseaseId} • Target: ${dis.geneId} (${dis.proteinId})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Evidence Publications:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            dis.evidencePublications.forEach { pub ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = pub,
                                        style = MaterialTheme.typography.labelSmall,
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
