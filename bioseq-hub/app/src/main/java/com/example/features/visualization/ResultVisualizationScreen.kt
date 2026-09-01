package com.example.features.visualization

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.SectionHeader
import com.example.ui.theme.*

data class NetworkNode(
    val id: String,
    val label: String,
    val type: String,
    val color: Color,
    var x: Float,
    var y: Float
)

data class NetworkEdge(
    val fromId: String,
    val toId: String,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultVisualizationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sequence & Motifs, 1: Network Graph
    var selectedNode by remember { mutableStateOf<NetworkNode?>(null) }
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val nodes = remember {
        mutableStateListOf(
            NetworkNode("TP53", "TP53\n(Hub)", "Tumor Suppressor", Color(0xFF00E5FF), 250f, 220f),
            NetworkNode("MDM2", "MDM2", "E3 Ubiquitin Ligase", Color(0xFFFF5252), 120f, 100f),
            NetworkNode("ATM", "ATM", "DNA Damage Kinase", Color(0xFFFFD600), 380f, 100f),
            NetworkNode("CDKN1A", "p21\n(CDKN1A)", "Cell Cycle Arrest", Color(0xFF00E676), 100f, 340f),
            NetworkNode("BAX", "BAX", "Apoptosis Induction", Color(0xFFFFAB00), 250f, 370f),
            NetworkNode("BRCA1", "BRCA1", "Homologous Recombination", Color(0xFFE040FB), 400f, 320f)
        )
    }

    val edges = remember {
        listOf(
            NetworkEdge("TP53", "MDM2", "Regulates / Degraded by"),
            NetworkEdge("ATM", "TP53", "Phosphorylates Ser15"),
            NetworkEdge("TP53", "CDKN1A", "Transactivates"),
            NetworkEdge("TP53", "BAX", "Transactivates"),
            NetworkEdge("BRCA1", "TP53", "Co-transactivates")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("14. 📊 Result Visualization", fontWeight = FontWeight.Bold, color = HighDensityNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HighDensityNavy)
                    }
                },
                actions = {
                    if (selectedTab == 1) {
                        IconButton(onClick = {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset Zoom", tint = HighDensityNavy)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HighDensityNavBg
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(HighDensityCanvas)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = HighDensityNavBg,
                contentColor = HighDensityNavy
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sequence & Motifs", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3D Molecule (PDB)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Network Graph", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> {
                    // High-performance sequence visualizer component
                    SequenceVisualizerComponent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Interactive 3D PDB Molecule Viewer Component
                    MoleculeViewer3DComponent(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // 2D Network Graph View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "Interactive Biological Network Graph",
                        subtitle = "Drag nodes, inspect protein-protein interactions, and explore pathway clusters"
                    )

                    // 2D Network Canvas Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            panOffset += dragAmount
                                        }
                                    }
                                    .testTag("network_canvas")
                            ) {
                                val centerX = size.width / 2
                                val centerY = size.height / 2

                                // Draw Edges
                                edges.forEach { edge ->
                                    val fromNode = nodes.find { it.id == edge.fromId }
                                    val toNode = nodes.find { it.id == edge.toId }
                                    if (fromNode != null && toNode != null) {
                                        val p1 = Offset(fromNode.x * zoomScale + panOffset.x, fromNode.y * zoomScale + panOffset.y)
                                        val p2 = Offset(toNode.x * zoomScale + panOffset.x, toNode.y * zoomScale + panOffset.y)

                                        drawLine(
                                            color = Color(0xFF475569),
                                            start = p1,
                                            end = p2,
                                            strokeWidth = 2.5f * zoomScale
                                        )
                                    }
                                }

                                // Draw Nodes
                                nodes.forEach { node ->
                                    val pos = Offset(node.x * zoomScale + panOffset.x, node.y * zoomScale + panOffset.y)
                                    val isSelected = selectedNode?.id == node.id

                                    // Glow effect
                                    drawCircle(
                                        color = node.color.copy(alpha = 0.25f),
                                        radius = 28f * zoomScale,
                                        center = pos
                                    )

                                    // Inner Node
                                    drawCircle(
                                        color = node.color,
                                        radius = 18f * zoomScale,
                                        center = pos
                                    )

                                    if (isSelected) {
                                        drawCircle(
                                            color = Color.White,
                                            radius = 22f * zoomScale,
                                            center = pos,
                                            style = Stroke(width = 3f)
                                        )
                                    }
                                }
                            }

                            // Interactive Node Buttons Overlay
                            nodes.forEach { node ->
                                Box(
                                    modifier = Modifier
                                        .offset(x = (node.x - 30).dp, y = (node.y - 30).dp)
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .clickable { selectedNode = node }
                                )
                            }

                            // Legend
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xAA1E293B),
                                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                            ) {
                                Text(
                                    text = "Interactive Canvas • Tap nodes to inspect",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Node Inspector Panel
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val inspected = selectedNode ?: nodes.first()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(inspected.color)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Selected Target: ${inspected.id}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Badge {
                                    Text(inspected.type)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Connected Pathway Interactions: ${edges.filter { it.fromId == inspected.id || it.toId == inspected.id }.size} regulatory links",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                }
            }
        }
    }
}
