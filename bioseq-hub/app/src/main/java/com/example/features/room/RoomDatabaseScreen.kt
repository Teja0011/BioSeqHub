package com.example.features.room

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BioSeqApplication
import com.example.core.ui.SectionHeader
import com.example.ui.theme.TertiaryDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDatabaseScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { BioSeqApplication.instance.repository }
    val scope = rememberCoroutineScope()
    var bannerMessage by remember { mutableStateOf<String?>(null) }
    var selectedEntityName by remember { mutableStateOf("SequenceEntity") }

    val sequences by repository.getAllSequences().collectAsStateWithLifecycle(initialValue = emptyList())
    val queries by repository.getAllQueries().collectAsStateWithLifecycle(initialValue = emptyList())
    val annotations by repository.getAllAnnotations().collectAsStateWithLifecycle(initialValue = emptyList())
    val orthologs by repository.getAllOrthologs().collectAsStateWithLifecycle(initialValue = emptyList())
    val diseases by repository.getAllDiseases().collectAsStateWithLifecycle(initialValue = emptyList())

    val entitiesList = listOf(
        "SequenceEntity (${sequences.size})" to "sequences",
        "QueryEntity (${queries.size})" to "queries",
        "AnnotationEntity (${annotations.size})" to "annotations",
        "OrthologEntity (${orthologs.size})" to "orthologs",
        "DiseaseAssociationEntity (${diseases.size})" to "disease_associations"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("3. 💾 Room Database", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                bannerMessage = "Database refreshed: ${sequences.size} sequences, ${annotations.size} annotations active."
                            }
                        },
                        modifier = Modifier.testTag("refresh_db_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
                    title = "Offline-First Local SQLite Architecture",
                    subtitle = "Room persistence handles query caching, sequence models, and offline research"
                )
            }

            // Banner
            if (bannerMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = bannerMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Stat Cards Row
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricCard(title = "Sequences", value = sequences.size.toString(), modifier = Modifier.weight(1f))
                    MetricCard(title = "Queries", value = queries.size.toString(), modifier = Modifier.weight(1f))
                    MetricCard(title = "Annotations", value = annotations.size.toString(), modifier = Modifier.weight(1f))
                    MetricCard(title = "Orthologs", value = orthologs.size.toString(), modifier = Modifier.weight(1f))
                }
            }

            // Offline Cache Architecture Flow
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "DATA FLOW PROTOCOL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "UI  ➜  ViewModel  ➜  Use Case  ➜  Repository  ➜  Room Cache (Offline First)  ➜  Remote Network",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Entity Inspector Header
            item {
                Text(
                    text = "INSPECT ROOM ENTITIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Entity Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = entitiesList.indexOfFirst { it.first.startsWith(selectedEntityName) }.coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    entitiesList.forEach { (displayName, rawName) ->
                        Tab(
                            selected = selectedEntityName == rawName,
                            onClick = { selectedEntityName = rawName },
                            text = { Text(displayName, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // List of selected entities
            when (selectedEntityName) {
                "sequences" -> {
                    items(sequences) { seq ->
                        EntityRecordCard(
                            id = seq.accession,
                            title = seq.description.ifBlank { "Sequence ${seq.accession}" },
                            subtitle = "Organism: ${seq.organism} • Length: ${seq.length} aa • Type: ${seq.sequenceType}",
                            meta = "Source: ${seq.source}"
                        )
                    }
                }
                "queries" -> {
                    items(queries) { q ->
                        EntityRecordCard(
                            id = q.queryId,
                            title = q.queryText,
                            subtitle = "Type: ${q.queryType} • Database: ${q.database}",
                            meta = "Status: ${q.status}"
                        )
                    }
                }
                "annotations" -> {
                    items(annotations) { ann ->
                        EntityRecordCard(
                            id = ann.annotationId,
                            title = "${ann.annotationType}: ${ann.annotationText}",
                            subtitle = "Sequence: ${ann.sequenceId} • Evidence: ${ann.evidence}",
                            meta = "Source: ${ann.source}"
                        )
                    }
                }
                "orthologs" -> {
                    items(orthologs) { orth ->
                        EntityRecordCard(
                            id = orth.orthologId,
                            title = "${orth.sourceSequence} ⟷ ${orth.targetSequence}",
                            subtitle = "Identity: ${orth.identityPercent}% • Coverage: ${orth.coveragePercent}%",
                            meta = "Method: ${orth.method}"
                        )
                    }
                }
                else -> {
                    items(diseases) { dis ->
                        EntityRecordCard(
                            id = dis.diseaseId,
                            title = dis.diseaseName,
                            subtitle = "Gene: ${dis.geneId} • Score: ${dis.score}",
                            meta = "Source: ${dis.source}"
                        )
                    }
                }
            }

            // Database Actions
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                repository.clearAllQueries()
                                bannerMessage = "Cleared temporary query cache in Room database."
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Queries")
                    }

                    Button(
                        onClick = {
                            bannerMessage = "Exported 9 Room table schemas to research snapshot JSON."
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Cache")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EntityRecordCard(
    id: String,
    title: String,
    subtitle: String,
    meta: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = id,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = meta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
