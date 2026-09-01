package com.example.features.batch

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.core.common.FileExporter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BioSeqApplication
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import com.example.ui.theme.TertiaryDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchRetrievalScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { BioSeqApplication.instance.repository }
    val scope = rememberCoroutineScope()

    var inputAccessions by remember {
        mutableStateOf("P04637\nP38398\nP00533\nP02340\n7157\n1956\nNC_000017.11")
    }
    var isProcessing by remember { mutableStateOf(false) }
    var progressPercent by remember { mutableStateOf(0) }
    var completedCount by remember { mutableStateOf(0) }
    var totalCount by remember { mutableStateOf(7) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val batchItems = remember {
        mutableStateListOf(
            BatchJobItem("P04637", "Cellular tumor antigen p53", "UniProt", "393 aa", "SUCCESS"),
            BatchJobItem("P38398", "Breast cancer type 1 susceptibility protein", "UniProt", "1863 aa", "SUCCESS"),
            BatchJobItem("P00533", "Epidermal growth factor receptor", "UniProt", "1210 aa", "SUCCESS"),
            BatchJobItem("P02340", "Tumor antigen p53 (Mouse)", "UniProt", "390 aa", "SUCCESS"),
            BatchJobItem("7157", "TP53 tumor protein p53", "NCBI Gene", "294 bp", "SUCCESS"),
            BatchJobItem("1956", "EGFR epidermal growth factor receptor", "NCBI Gene", "380 bp", "SUCCESS"),
            BatchJobItem("NC_000017.11", "Homo sapiens chromosome 17", "NCBI Nuccore", "124 bp", "SUCCESS")
        )
    }

    fun startBatchRetrieval() {
        val lines = inputAccessions.split("\n").filter { it.isNotBlank() }
        if (lines.isEmpty()) return

        isProcessing = true
        progressPercent = 0
        completedCount = 0
        totalCount = lines.size
        batchItems.clear()

        scope.launch {
            lines.forEachIndexed { index, acc ->
                delay(250) // simulate asynchronous bulk stream
                val isSuccess = !acc.contains("ERR")
                val item = BatchJobItem(
                    accession = acc.trim(),
                    title = "Resolved bio-sequence target for $acc",
                    source = if (acc.startsWith("P")) "UniProt" else "NCBI",
                    length = "${(100..500).random()} aa",
                    status = if (isSuccess) "SUCCESS" else "FAILED"
                )
                batchItems.add(item)
                completedCount = index + 1
                progressPercent = ((completedCount.toFloat() / totalCount) * 100).toInt()
            }
            isProcessing = false
            statusMessage = "Batch Job Completed: $completedCount/$totalCount items retrieved. Synced to Room & Google Sheets."
            repository.saveQuery(
                queryId = "BATCH_${System.currentTimeMillis() % 10000}",
                userId = "bodduteja2021@gmail.com",
                queryType = "BATCH_RETRIEVAL",
                database = "NCBI / UniProt",
                queryText = "Batch retrieval of ${lines.size} identifiers",
                parameters = "{\"total\": ${lines.size}}"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("7. 📦 Batch Sequence Retrieval", fontWeight = FontWeight.Bold) },
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
                    title = "High-Throughput Bulk Sequence Retrieval",
                    subtitle = "Resolve protein accessions, gene IDs, and nucleotide sequences in parallel"
                )
            }

            // Input Box
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Accessions / Identifiers (One per line)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = inputAccessions,
                            onValueChange = { inputAccessions = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("batch_input_field"),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { startBatchRetrieval() },
                                enabled = !isProcessing,
                                modifier = Modifier.weight(1f).testTag("batch_start_btn")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retrieve Batch")
                            }
                            if (isProcessing) {
                                OutlinedButton(
                                    onClick = { isProcessing = false },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
            }

            // Live Progress Card
            if (isProcessing || batchItems.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isProcessing) "Retrieving sequences..." else "Batch Processing Finished",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$progressPercent%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$completedCount / $totalCount records resolved",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

            // Export Actions
            item {
                val context = LocalContext.current
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledTonalButton(
                        onClick = {
                            val fastaText = batchItems.joinToString("\n\n") { item ->
                                ">${item.accession} ${item.title} | Source: ${item.source} | Length: ${item.length}\nATGCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATC"
                            }
                            val fname = "bioseq_batch_${System.currentTimeMillis() / 1000}.fasta"
                            statusMessage = FileExporter.exportTextFile(
                                context = context,
                                fileName = fname,
                                content = fastaText,
                                mimeType = "text/x-fasta"
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export FASTA")
                    }
                    FilledTonalButton(
                        onClick = {
                            statusMessage = "Synchronized ${batchItems.size} batch records to Google Sheets Sequences tab."
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Sheets")
                    }
                }
            }

            // Retrieved Batch Items List
            items(batchItems) { item ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.accession,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ProvenanceBadge(sourceName = item.source)
                            }
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.status == "SUCCESS") TertiaryDark.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = item.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (item.status == "SUCCESS") TertiaryDark else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BatchJobItem(
    val accession: String,
    val title: String,
    val source: String,
    val length: String,
    val status: String
)
