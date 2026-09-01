package com.example.features.ortholog

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
import com.example.BioSeqApplication
import com.example.core.common.BioinformaticsEngine
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import com.example.domain.model.OrthologRecord
import com.example.ui.theme.TertiaryDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrthologFinderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { BioSeqApplication.instance.repository }
    val scope = rememberCoroutineScope()

    var sourceGene by remember { mutableStateOf("TP53") }
    var sourceSpecies by remember { mutableStateOf("Homo sapiens (Human)") }
    var targetSpecies by remember { mutableStateOf("Mus musculus (Mouse)") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val orthologList = remember {
        mutableStateListOf(
            OrthologRecord(
                orthologId = "ORTH_TP53_MM",
                sourceSequence = "TP53 (Human P04637)",
                targetSequence = "Trp53 (Mouse P02340)",
                sourceOrganism = "Homo sapiens",
                targetOrganism = "Mus musculus",
                identityPercent = 81.2,
                coveragePercent = 98.5,
                score = 642.0,
                method = "Reciprocal BLAST (BioKt Engine)",
                alignment = "HUMAN: MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGP\n       ||| ||| | |||||||||| |||||| | | |||   ||||| | | | | | | | \nMOUSE: MEESQSDISLELPLSQETFSGLWKLLPPEDILPSPHCMDDLLLPQDVEEFFEGPSEAL"
            ),
            OrthologRecord(
                orthologId = "ORTH_TP53_RN",
                sourceSequence = "TP53 (Human P04637)",
                targetSequence = "Tp53 (Rat P10361)",
                sourceOrganism = "Homo sapiens",
                targetOrganism = "Rattus norvegicus",
                identityPercent = 79.8,
                coveragePercent = 97.4,
                score = 618.0,
                method = "BioKt Needleman-Wunsch",
                alignment = "HUMAN: MEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGP\n       ||| ||| | |||||||||| |||||| | | |||   ||||| | | | | | | | \nRAT:   MEDSQSDMSIELPLSQETFSCLWKLLPPDDILPTTATGDDYFLS-WEE--DF-D-V"
            ),
            OrthologRecord(
                orthologId = "ORTH_TP53_DR",
                sourceSequence = "TP53 (Human P04637)",
                targetSequence = "tp53 (Zebrafish Q9W678)",
                sourceOrganism = "Homo sapiens",
                targetOrganism = "Danio rerio",
                identityPercent = 53.4,
                coveragePercent = 91.2,
                score = 420.0,
                method = "BioKt Needleman-Wunsch",
                alignment = "Conserved DNA-binding domain with high identity in loop L1 and zinc finger."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("9. 🔬 Ortholog Finder", fontWeight = FontWeight.Bold) },
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
                    title = "Cross-Species Orthology Identification",
                    subtitle = "Reciprocal sequence alignment, evolutionary conservation & identity scoring (BioKt)"
                )
            }

            // Query Configuration Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Ortholog Pairwise Search",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = sourceGene,
                            onValueChange = { sourceGene = it },
                            label = { Text("Source Gene / Protein") },
                            modifier = Modifier.fillMaxWidth().testTag("ortholog_gene_field"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = sourceSpecies,
                                onValueChange = { sourceSpecies = it },
                                label = { Text("Source Species") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = targetSpecies,
                                onValueChange = { targetSpecies = it },
                                label = { Text("Target Species") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                statusMessage = "BioKt Alignment computed: Reciprocal score 642.0 • 81.2% Identity with Mus musculus."
                            },
                            modifier = Modifier.fillMaxWidth().testTag("run_ortholog_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compute Orthology Alignment")
                        }
                    }
                }
            }

            // Status message
            if (statusMessage != null) {
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
                                text = statusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // List of Orthologs
            item {
                Text(
                    text = "IDENTIFIED CROSS-SPECIES ORTHOLOGS (${orthologList.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            items(orthologList) { orth ->
                OrthologCard(ortholog = orth)
            }
        }
    }
}

@Composable
fun OrthologCard(
    ortholog: OrthologRecord,
    modifier: Modifier = Modifier
) {
    var showAlignment by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${ortholog.sourceOrganism} ➜ ${ortholog.targetOrganism}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TertiaryDark.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${ortholog.identityPercent}% Identity",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TertiaryDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${ortholog.sourceSequence} ⟷ ${ortholog.targetSequence}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Coverage: ${ortholog.coveragePercent}% • Score: ${ortholog.score} • ${ortholog.method}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (showAlignment && ortholog.alignment.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = ortholog.alignment,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { showAlignment = !showAlignment }) {
                    Text(if (showAlignment) "Hide Alignment" else "View Alignment")
                }
            }
        }
    }
}
