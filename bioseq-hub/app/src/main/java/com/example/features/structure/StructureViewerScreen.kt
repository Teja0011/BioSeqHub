package com.example.features.structure

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.ProvenanceBadge
import com.example.core.ui.SectionHeader
import kotlin.math.cos
import kotlin.math.sin

data class Atom3D(
    val atomId: Int,
    val atomName: String,
    val resName: String,
    val chain: String,
    val resSeq: Int,
    var x: Float,
    var y: Float,
    var z: Float,
    val color: Color,
    val radius: Float = 6f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructureViewerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStructureId by remember { mutableStateOf("1TUP") }
    var selectedRenderMode by remember { mutableStateOf("Cartoon Ribbon") }
    var selectedChain by remember { mutableStateOf("All Chains") }
    var rotationX by remember { mutableStateOf(0f) }
    var rotationY by remember { mutableStateOf(0f) }
    var zoomScale by remember { mutableStateOf(1.2f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var activeTab by remember { mutableStateOf(0) } // 0: 3D View, 1: PDB Coordinates

    val renderModes = listOf("Cartoon Ribbon", "Ball & Stick", "Space Filling", "Solvent Surface")
    val chains = listOf("All Chains", "Chain A (p53)", "Chain B (p53)", "Chain C (DNA)", "Chain D (DNA)")

    val structures = listOf(
        "1TUP" to "1TUP • Tumor Suppressor p53 Complex (2.2 Å)",
        "1JNX" to "1JNX • BRCA1 BRCT Tandem Domain (2.5 Å)",
        "1M17" to "1M17 • EGFR Kinase Domain in Complex with Erlotinib (2.6 Å)",
        "CID_2244" to "CID 2244 • Aspirin Small Molecule (PubChem 3D)"
    )

    // Generate 3D alpha helix / beta sheet backbone atoms for visualization
    val atomsList = remember(selectedStructureId) {
        val list = mutableListOf<Atom3D>()
        val count = 48
        val radiusHelix = 40f
        val pitch = 5f

        for (i in 0 until count) {
            val theta = i * 0.45f
            val x = radiusHelix * cos(theta)
            val y = radiusHelix * sin(theta)
            val z = (i - count / 2) * pitch

            val resColor = when (i % 4) {
                0 -> Color(0xFF00E5FF) // Cyan
                1 -> Color(0xFF00E676) // Green
                2 -> Color(0xFFFF5252) // Red
                else -> Color(0xFFFFD600) // Yellow
            }

            list.add(
                Atom3D(
                    atomId = i + 1,
                    atomName = "CA",
                    resName = when (i % 6) {
                        0 -> "ARG"; 1 -> "CYS"; 2 -> "HIS"; 3 -> "LEU"; 4 -> "GLU"; else -> "PRO"
                    },
                    chain = if (i < 24) "A" else "B",
                    resSeq = 100 + i,
                    x = x,
                    y = y,
                    z = z,
                    color = resColor,
                    radius = if (selectedRenderMode == "Space Filling") 14f else 7f
                )
            )
        }

        // Add Zn2+ ion in center
        list.add(
            Atom3D(
                atomId = count + 1,
                atomName = "ZN",
                resName = "ZN",
                chain = "A",
                resSeq = 400,
                x = 0f,
                y = 0f,
                z = 0f,
                color = Color(0xFFE040FB),
                radius = 16f
            )
        )
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("17. 🧬 3D Structure Viewer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        rotationX = 0f
                        rotationY = 0f
                        zoomScale = 1.2f
                        panOffset = Offset.Zero
                    }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset View")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(
                title = "Macromolecular 3D Structure Engine",
                subtitle = "Interactive 3D atomic coordinates, ribbon models, and coordination centers"
            )

            // Structure Selector Dropdown / Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                structures.forEach { (id, label) ->
                    FilterChip(
                        selected = selectedStructureId == id,
                        onClick = { selectedStructureId = id },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }

            // View Tabs (3D Canvas vs Coordinates)
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Interactive 3D Model") }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("PDB Coordinates (${atomsList.size} Atoms)") }
                )
            }

            if (activeTab == 0) {
                // 3D Canvas View
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070D1E)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        rotationY += dragAmount.x * 0.015f
                                        rotationX -= dragAmount.y * 0.015f
                                    }
                                }
                                .testTag("canvas_3d_viewer")
                        ) {
                            val centerX = size.width / 2 + panOffset.x
                            val centerY = size.height / 2 + panOffset.y

                            val radX = rotationX
                            val radY = rotationY

                            val projectedPoints = atomsList.map { atom ->
                                // 3D Rotation Matrix (Y-axis and X-axis)
                                val x1 = atom.x * cos(radY) + atom.z * sin(radY)
                                val z1 = -atom.x * sin(radY) + atom.z * cos(radY)

                                val y2 = atom.y * cos(radX) - z1 * sin(radX)
                                val z2 = atom.y * sin(radX) + z1 * cos(radX)

                                val scale = (zoomScale * 350f) / (350f + z2)
                                val px = centerX + x1 * scale
                                val py = centerY + y2 * scale

                                Triple(Offset(px, py), z2, atom)
                            }.sortedBy { it.second } // Painter's algorithm (Z-sort)

                            // Draw Bonds / Backbone Ribbon
                            for (i in 0 until projectedPoints.size - 2) {
                                val p1 = projectedPoints[i]
                                val p2 = projectedPoints[i + 1]
                                if (p1.third.atomName != "ZN" && p2.third.atomName != "ZN") {
                                    drawLine(
                                        color = if (selectedRenderMode == "Cartoon Ribbon") p1.third.color.copy(alpha = 0.8f) else Color(0xFF64748B),
                                        start = p1.first,
                                        end = p2.first,
                                        strokeWidth = if (selectedRenderMode == "Cartoon Ribbon") 8f * zoomScale else 3f * zoomScale
                                    )
                                }
                            }

                            // Draw Atoms / Spheres
                            projectedPoints.forEach { (pos, z, atom) ->
                                val atomRadius = atom.radius * zoomScale

                                // Shadow / Depth shading
                                drawCircle(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    radius = atomRadius + 2f,
                                    center = pos + Offset(2f, 2f)
                                )

                                drawCircle(
                                    color = atom.color,
                                    radius = atomRadius,
                                    center = pos
                                )

                                if (atom.atomName == "ZN") {
                                    drawCircle(
                                        color = Color.White,
                                        radius = atomRadius * 1.3f,
                                        center = pos,
                                        style = Stroke(width = 2.5f)
                                    )
                                }
                            }
                        }

                        // Floating Canvas HUD Controls
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x990F172A),
                            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(3f) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.5f) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x990F172A),
                            modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                        ) {
                            Text(
                                text = "$selectedStructureId • Drag to rotate 360°",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Render Mode Chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    renderModes.forEach { mode ->
                        FilterChip(
                            selected = selectedRenderMode == mode,
                            onClick = { selectedRenderMode = mode },
                            label = { Text(mode, fontSize = 12.sp) }
                        )
                    }
                }
            } else {
                // PDB Coordinates Table
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(atomsList) { atom ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ATOM ${atom.atomId.toString().padStart(4)} ${atom.atomName} ${atom.resName} ${atom.chain}${atom.resSeq}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "X:${String.format("%.1f", atom.x)} Y:${String.format("%.1f", atom.y)} Z:${String.format("%.1f", atom.z)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
