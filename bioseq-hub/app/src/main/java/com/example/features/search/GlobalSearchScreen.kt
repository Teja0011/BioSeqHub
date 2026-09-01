package com.example.features.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.Screen
import com.example.core.ui.ProvenanceBadge
import com.example.domain.model.BiologicalSequence
import com.example.domain.model.DiseaseAssociation
import com.example.domain.model.Molecular3DStructure
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("TP53") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDatabase by remember { mutableStateOf("All") }

    val categories = listOf("All", "Genes", "Proteins", "Sequences", "Diseases", "Compounds", "Publications")
    val databases = listOf("All", "NCBI", "UniProt", "PubChem", "PubMed", "DisGeNET")

    val demoResults = remember {
        listOf(
            SearchResult(
                id = "P04637",
                title = "Cellular tumor antigen p53 (TP53)",
                category = "Proteins",
                database = "UniProt",
                organism = "Homo sapiens",
                details = "Master tumor suppressor protein regulating cell cycle arrest, apoptosis, and genomic integrity.",
                has3D = true,
                hasAnnotations = true,
                route = Screen.Structure3D.route
            ),
            SearchResult(
                id = "7157",
                title = "TP53 tumor protein p53",
                category = "Genes",
                database = "NCBI",
                organism = "Homo sapiens",
                details = "Chromosome 17: 7,668,402-7,687,550. Encodes p53 transcription factor.",
                has3D = true,
                hasAnnotations = true,
                route = Screen.NcbiEntrez.route
            ),
            SearchResult(
                id = "CID_2244",
                title = "Aspirin (Acetylsalicylic Acid)",
                category = "Compounds",
                database = "PubChem",
                organism = "Synthetic",
                details = "C9H8O4 • MW: 180.16 g/mol • COX-1 and COX-2 inhibitor.",
                has3D = true,
                hasAnnotations = false,
                route = Screen.Structure3D.route
            ),
            SearchResult(
                id = "OMIM:151623",
                title = "Li-Fraumeni Syndrome 1 (LFS1)",
                category = "Diseases",
                database = "DisGeNET",
                organism = "Homo sapiens",
                details = "Autosomal dominant hereditary cancer syndrome linked to TP53 germline mutations.",
                has3D = false,
                hasAnnotations = true,
                route = Screen.DiseaseAssociations.route
            ),
            SearchResult(
                id = "PMID:25732183",
                title = "p53: The Most Frequently Altered Gene in Human Cancers",
                category = "Publications",
                database = "PubMed",
                organism = "Homo sapiens",
                details = "Comprehensive molecular review of TP53 somatic mutation spectra across 32 tumor types.",
                has3D = false,
                hasAnnotations = false,
                route = Screen.CrossReference.route
            )
        )
    }

    val filteredResults = demoResults.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.details.contains(searchQuery, ignoreCase = true) ||
                item.id.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesDatabase = selectedDatabase == "All" || item.database.equals(selectedDatabase, ignoreCase = true)

        matchesQuery && matchesCategory && matchesDatabase
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Global Research Search", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("global_search_input"),
                placeholder = { Text("Search genes, proteins, diseases, compounds...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            // Database Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(databases) { db ->
                    FilterChip(
                        selected = selectedDatabase == db,
                        onClick = { selectedDatabase = db },
                        label = { Text(db) },
                        leadingIcon = if (selectedDatabase == db) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Results List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Matching Biological Records (${filteredResults.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (filteredResults.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No matching records found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try adjusting your query or selecting 'All' filters",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredResults) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onNavigateToRoute(result.route) },
                            onAskGemini = { onNavigateToRoute(Screen.GeminiAssistant.route) }
                        )
                    }
                }
            }
        }
    }
}

data class SearchResult(
    val id: String,
    val title: String,
    val category: String,
    val database: String,
    val organism: String,
    val details: String,
    val has3D: Boolean,
    val hasAnnotations: Boolean,
    val route: String
)

@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit,
    onAskGemini: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvenanceBadge(sourceName = result.database)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = result.organism,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Identifier: ${result.id} • ${result.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = result.details,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (result.has3D) {
                        SuggestionChip(
                            onClick = onClick,
                            label = { Text("✓ 3D Model", fontSize = 11.sp) }
                        )
                    }
                    if (result.hasAnnotations) {
                        SuggestionChip(
                            onClick = onClick,
                            label = { Text("✓ Annotations", fontSize = 11.sp) }
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onAskGemini,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🤖 AI Analyze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
