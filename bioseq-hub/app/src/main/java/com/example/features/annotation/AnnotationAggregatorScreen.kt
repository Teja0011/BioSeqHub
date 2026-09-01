package com.example.features.annotation

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
import com.example.BioSeqApplication
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import com.example.domain.model.SequenceAnnotation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationAggregatorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var queryTarget by remember { mutableStateOf("P04637") }
    var selectedFilter by remember { mutableStateOf("All Sources") }

    val filters = listOf("All Sources", "UniProt", "InterPro", "Pfam")

    val annotations = remember {
        listOf(
            SequenceAnnotation(
                annotationId = "ANN_1",
                sequenceId = "P04637",
                source = "UniProt",
                annotationType = "FUNCTION",
                annotationText = "Acts as a tumor suppressor in all types of cancers; induces growth arrest or apoptosis depending on physiological circumstances.",
                evidence = "ECO:0000269 (Experimental) • PubMed:12524540",
                startResidue = 1,
                endResidue = 393
            ),
            SequenceAnnotation(
                annotationId = "ANN_2",
                sequenceId = "P04637",
                source = "InterPro",
                annotationType = "DOMAIN",
                annotationText = "p53 DNA-binding domain (IPR011615). Coordinates Zn2+ ion essential for sequence-specific response element recognition.",
                evidence = "ECO:0000269 • PDB:1TUP",
                startResidue = 102,
                endResidue = 292
            ),
            SequenceAnnotation(
                annotationId = "ANN_3",
                sequenceId = "P04637",
                source = "Pfam",
                annotationType = "FAMILY",
                annotationText = "P53 tetramerisation domain (PF07710). Forms dimer-of-dimers configuration critical for cooperative transactivation.",
                evidence = "ECO:0000305 (Curated Pfam Profile)",
                startResidue = 325,
                endResidue = 356
            ),
            SequenceAnnotation(
                annotationId = "ANN_4",
                sequenceId = "P04637",
                source = "UniProt",
                annotationType = "PTM",
                annotationText = "Phosphorylation at Ser-15 and Ser-20 impairs interaction with MDM2 and promotes p53 accumulation upon DNA damage.",
                evidence = "ECO:0000269 • PubMed:9367988",
                startResidue = 15,
                endResidue = 20
            ),
            SequenceAnnotation(
                annotationId = "ANN_5",
                sequenceId = "P04637",
                source = "InterPro",
                annotationType = "INTERACTION",
                annotationText = "Interacts with MDM2, EP300, CREBBP, and ATM kinase. Negatively regulated by ubiquitin-mediated proteasomal degradation.",
                evidence = "ECO:0000269 • PubMed:8945484",
                startResidue = 1,
                endResidue = 50
            )
        )
    }

    val filteredAnnotations = annotations.filter {
        selectedFilter == "All Sources" || it.source.equals(selectedFilter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("8. 🧩 Annotation Aggregator", fontWeight = FontWeight.Bold) },
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
                    title = "Multi-Database Annotation Aggregation",
                    subtitle = "Unified integration from UniProt, InterPro, and Pfam with preserved source provenance"
                )
            }

            // Target selector
            item {
                OutlinedTextField(
                    value = queryTarget,
                    onValueChange = { queryTarget = it },
                    label = { Text("Target Protein / Accession") },
                    modifier = Modifier.fillMaxWidth().testTag("annotation_target_field"),
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
                    filters.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            // Aggregated Summary Stats
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Aggregated Annotations",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Deduplicated & Provenance Tagged",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Badge {
                            Text("${filteredAnnotations.size} Active")
                        }
                    }
                }
            }

            // Annotation Cards
            items(filteredAnnotations) { ann ->
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
                            ProvenanceBadge(sourceName = ann.source)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = ann.annotationType,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = ann.annotationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (ann.startResidue != null && ann.endResidue != null) {
                            Text(
                                text = "Residues: ${ann.startResidue} - ${ann.endResidue}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Evidence: ${ann.evidence}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
