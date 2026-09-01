package com.example.features.functional

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
import com.example.core.ui.AiDisclaimerBadge
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import com.example.domain.model.GeneOntologyTerm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FunctionalAnnotationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var queryGene by remember { mutableStateOf("TP53") }
    var selectedCategory by remember { mutableStateOf("All Categories") }

    val categories = listOf("All Categories", "Biological Process", "Molecular Function", "Cellular Component")

    val goTerms = remember {
        listOf(
            GeneOntologyTerm(
                goId = "GO:0006915",
                termName = "Apoptotic Process",
                category = "Biological Process",
                evidenceCode = "EXP (Inferred from Experiment)",
                definition = "A programmed cell death process executed by caspase proteases in response to genotoxic stress."
            ),
            GeneOntologyTerm(
                goId = "GO:0000077",
                termName = "DNA Damage Checkpoint Signaling",
                category = "Biological Process",
                evidenceCode = "IDA (Inferred from Direct Assay)",
                definition = "Signaling cascade that arrests cell cycle progression to facilitate nucleotide excision repair."
            ),
            GeneOntologyTerm(
                goId = "GO:0003677",
                termName = "DNA Binding",
                category = "Molecular Function",
                evidenceCode = "IDA (Direct Assay)",
                definition = "Interacts selectively and non-covalently with sequence-specific p53 response elements (PuPuPuC(A/T))."
            ),
            GeneOntologyTerm(
                goId = "GO:0003700",
                termName = "Sequence-Specific DNA-Binding Transcription Factor",
                category = "Molecular Function",
                evidenceCode = "EXP (Experimental)",
                definition = "Activates transcription of downstream tumor suppressor targets including CDKN1A (p21), BAX, and GADD45."
            ),
            GeneOntologyTerm(
                goId = "GO:0005634",
                termName = "Nucleus",
                category = "Cellular Component",
                evidenceCode = "TAS (Traceable Author Statement)",
                definition = "Subcellular compartment containing the genetic material where transcriptional activation occurs."
            ),
            GeneOntologyTerm(
                goId = "GO:0005739",
                termName = "Mitochondrion",
                category = "Cellular Component",
                evidenceCode = "IDA (Direct Assay)",
                definition = "Translocates to outer mitochondrial membrane during stress to induce cytochrome c release."
            )
        )
    }

    val filteredTerms = goTerms.filter {
        selectedCategory == "All Categories" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("11. 🏷️ Functional Annotation", fontWeight = FontWeight.Bold) },
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
                    title = "Gene Ontology & Functional Profiling",
                    subtitle = "Curated GO Terms (Biological Process, Molecular Function, Cellular Component) with AI synthesis"
                )
            }

            // Target selector
            item {
                OutlinedTextField(
                    value = queryGene,
                    onValueChange = { queryGene = it },
                    label = { Text("Target Gene Symbol") },
                    modifier = Modifier.fillMaxWidth().testTag("functional_gene_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // AI Synthesis Section
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        AiDisclaimerBadge()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Functional Role Synthesis: TP53",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "TP53 is a pivotal stress sensor that coordinates cellular responses to genome damage, oncogene activation, and hypoxia. By transcriptionally regulating cell cycle arrest genes (such as CDKN1A) and pro-apoptotic factors (such as BAX and BBC3), it acts as the primary barrier against malignant transformation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Filter Chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            // GO Terms List
            items(filteredTerms) { term ->
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
                                text = term.goId,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            ProvenanceBadge(sourceName = "UniProt")
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = term.termName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Category: ${term.category} • Evidence: ${term.evidenceCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = term.definition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
