package com.example.features.paralog

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
import com.example.core.ui.SectionHeader
import com.example.domain.model.ParalogRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParalogDetectorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var geneSymbol by remember { mutableStateOf("TP53") }

    val paralogs = remember {
        listOf(
            ParalogRecord(
                paralogId = "PARA_1",
                geneSymbol = "TP53",
                duplicatedGene = "TP73 (Tumor Protein p73)",
                chromosomeLocation = "Chr 1p36.32",
                identityPercent = 63.5,
                duplicationType = "Whole-Genome Duplication (2R Hypothesis)",
                evolutionaryDistance = 0.42
            ),
            ParalogRecord(
                paralogId = "PARA_2",
                geneSymbol = "TP53",
                duplicatedGene = "TP63 (Tumor Protein p63)",
                chromosomeLocation = "Chr 3q28",
                identityPercent = 58.2,
                duplicationType = "Ancient Segmental Duplication",
                evolutionaryDistance = 0.48
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("10. 🔀 Paralog Detector", fontWeight = FontWeight.Bold) },
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
                    title = "Within-Genome Duplication Analysis",
                    subtitle = "Detect paralogous gene families, whole-genome duplication events, and sequence divergence"
                )
            }

            item {
                OutlinedTextField(
                    value = geneSymbol,
                    onValueChange = { geneSymbol = it },
                    label = { Text("Target Gene Symbol") },
                    modifier = Modifier.fillMaxWidth().testTag("paralog_gene_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PARALOG GENE CLUSTER SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The p53 family (TP53, TP63, TP73) arose from two successive whole-genome duplications (2R) early in vertebrate evolution, retaining conserved transactivation and oligomerization domains.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(paralogs) { paralog ->
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
                                text = paralog.duplicatedGene,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Badge {
                                Text("${paralog.identityPercent}% Identity")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Location: ${paralog.chromosomeLocation} • Distance: ${paralog.evolutionaryDistance}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Duplication Mechanism: ${paralog.duplicationType}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
