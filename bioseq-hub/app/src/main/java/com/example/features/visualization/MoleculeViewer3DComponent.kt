package com.example.features.visualization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biokt.io.PdbAtom
import com.example.biokt.io.PdbIO
import com.example.biokt.io.PdbRecord
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

enum class MoleculeRenderStyle(val label: String) {
    BALL_AND_STICK("Ball & Stick"),
    RIBBON_BACKBONE("Backbone / C-alpha"),
    SPACE_FILLING("Space-Filling (CPK)"),
    SURFACE_WIREFRAME("Wireframe Mesh")
}

enum class AtomColoringMode(val label: String) {
    ELEMENT("By Element (CPK)"),
    CHAIN("By Chain (A/B/C)"),
    RESIDUE_TYPE("By Residue Hydrophobicity"),
    B_FACTOR("By B-Factor Temperature")
}

data class ProjectedAtom(
    val atom: PdbAtom,
    val projX: Float,
    val projY: Float,
    val projZ: Float, // for depth-sorting
    val radius: Float,
    val color: Color
)

object PresetPdbStructures {
    // Human Insulin Monomer (PDB: 1TRZ snippet with Chain A & B)
    val INSULIN_PDB = """
HEADER    HORMONE                                 15-JAN-93   1TRZ
TITLE     CRYSTAL STRUCTURE OF HUMAN INSULIN MONOMER
ATOM      1  N   GLY A   1      13.791   8.618   7.170  1.00 15.24           N
ATOM      2  CA  GLY A   1      14.739   7.653   6.574  1.00 16.12           C
ATOM      3  C   GLY A   1      14.072   6.309   6.370  1.00 14.88           C
ATOM      4  O   GLY A   1      12.871   6.208   6.604  1.00 15.89           O
ATOM      5  N   ILE A   2      14.862   5.283   5.940  1.00 13.91           N
ATOM      6  CA  ILE A   2      14.372   3.929   5.698  1.00 12.80           C
ATOM      7  C   ILE A   2      13.743   3.385   7.012  1.00 11.90           C
ATOM      8  O   ILE A   2      14.364   3.385   8.077  1.00 12.40           O
ATOM      9  CB  ILE A   2      15.545   2.998   5.148  1.00 13.20           C
ATOM     10  CG1 ILE A   2      16.155   3.529   3.847  1.00 14.50           C
ATOM     11  CG2 ILE A   2      15.068   1.564   4.945  1.00 13.80           C
ATOM     12  CD1 ILE A   2      17.262   2.646   3.284  1.00 15.60           C
ATOM     13  N   VAL A   3      12.508   2.924   6.902  1.00 10.90           N
ATOM     14  CA  VAL A   3      11.758   2.355   8.026  1.00 10.40           C
ATOM     15  C   VAL A   3      10.871   1.218   7.489  1.00 11.20           C
ATOM     16  O   VAL A   3      10.428   1.238   6.341  1.00 12.10           O
ATOM     17  CB  VAL A   3      10.865   3.447   8.685  1.00 10.10           C
ATOM     18  CG1 VAL A   3      10.158   2.898   9.932  1.00 10.80           C
ATOM     19  CG2 VAL A   3      11.733   4.646   9.083  1.00 11.30           C
ATOM     20  N   GLU A   4      10.613   0.231   8.328  1.00 11.90           N
ATOM     21  CA  GLU A   4       9.789  -0.916   7.965  1.00 13.40           C
ATOM     22  C   GLU A   4       8.339  -0.536   7.653  1.00 14.20           C
ATOM     23  O   GLU A   4       7.962  -0.420   6.485  1.00 15.10           O
ATOM     24  CB  GLU A   4       9.870  -1.956   9.091  1.00 14.80           C
ATOM     25  CG  GLU A   4      11.272  -2.520   9.336  1.00 17.50           C
ATOM     26  CD  GLU A   4      11.332  -3.563  10.443  1.00 20.10           C
ATOM     27  OE1 GLU A   4      10.366  -3.693  11.232  1.00 22.00           O
ATOM     28  OE2 GLU A   4      12.359  -4.270  10.518  1.00 21.40           O
ATOM     29  N   GLN A   5       7.533  -0.342   8.702  1.00 14.80           N
ATOM     30  CA  GLN A   5       6.113   0.009   8.549  1.00 15.90           C
ATOM     31  C   GLN A   5       5.918   1.468   8.948  1.00 16.40           C
ATOM     32  O   GLN A   5       4.800   1.979   8.889  1.00 17.20           O
ATOM     33  N   CYS A   6       6.989   2.138   9.356  1.00 16.20           N
ATOM     34  CA  CYS A   6       6.920   3.553   9.757  1.00 16.50           C
ATOM     35  C   CYS A   6       7.484   3.731  11.168  1.00 16.90           C
ATOM     36  O   CYS A   6       7.669   4.851  11.644  1.00 17.80           O
ATOM     37  CB  CYS A   6       7.712   4.464   8.799  1.00 17.10           C
ATOM     38  SG  CYS A   6       7.490   6.248   9.141  1.00 18.50           S
ATOM     39  N   CYS A   7       7.760   2.637  11.834  1.00 17.00           N
ATOM     40  CA  CYS A   7       8.293   2.709  13.203  1.00 17.40           C
ATOM     41  C   CYS A   7       7.310   2.088  14.200  1.00 17.90           C
ATOM     42  O   CYS A   7       7.632   1.986  15.389  1.00 18.80           O
ATOM     43  CB  CYS A   7       9.673   1.996  13.266  1.00 17.90           C
ATOM     44  SG  CYS A   7      10.871   2.822  12.203  1.00 19.50           S
ATOM     45  N   THR A   8       6.113   1.666  13.729  1.00 18.10           N
ATOM     46  CA  THR A   8       5.086   1.042  14.577  1.00 18.70           C
ATOM     47  C   THR A   8       4.179   2.083  15.241  1.00 19.30           C
ATOM     48  O   THR A   8       3.844   1.979  16.425  1.00 20.10           O
ATOM     49  N   SER A   9       3.791   3.090  14.475  1.00 19.40           N
ATOM     50  CA  SER A   9       2.898   4.137  14.992  1.00 20.20           C
ATOM     51  C   SER A   9       1.459   3.640  15.011  1.00 20.80           C
ATOM     52  O   SER A   9       0.540   4.341  15.422  1.00 21.60           O
ATOM     53  N   PHE B   1       1.350   7.653  20.574  1.00 22.12           N
ATOM     54  CA  PHE B   1       2.120   8.850  21.020  1.00 21.50           C
ATOM     55  C   PHE B   1       3.500   8.400  21.490  1.00 20.80           C
ATOM     56  O   PHE B   1       4.100   7.500  20.880  1.00 21.20           O
ATOM     57  CB  PHE B   1       1.380   9.670  22.100  1.00 22.80           C
ATOM     58  CG  PHE B   1       2.180  10.880  22.560  1.00 24.20           C
ATOM     59  CD1 PHE B   1       3.100  11.500  21.720  1.00 25.10           C
ATOM     60  CD2 PHE B   1       2.020  11.390  23.840  1.00 25.40           C
ATOM     61  N   VAL B   2       3.980   8.990  22.580  1.00 19.50           N
ATOM     62  CA  VAL B   2       5.300   8.620  23.120  1.00 18.70           C
ATOM     63  C   VAL B   2       6.350   9.690  22.810  1.00 18.20           C
ATOM     64  O   VAL B   2       6.040  10.880  22.750  1.00 18.90           O
ATOM     65  CB  VAL B   2       5.220   8.380  24.650  1.00 18.50           C
ATOM     66  N   ASN B   3       7.590   9.250  22.610  1.00 17.50           N
ATOM     67  CA  ASN B   3       8.720  10.150  22.340  1.00 17.10           C
ATOM     68  C   ASN B   3       9.140  10.030  20.870  1.00 16.80           C
ATOM     69  O   ASN B   3       9.610   8.980  20.430  1.00 17.40           O
ATOM     70  CB  ASN B   3       9.930   9.830  23.230  1.00 17.30           C
ATOM     71  CG  ASN B   3       9.630  10.020  24.710  1.00 18.10           C
ATOM     72  OD1 ASN B   3       8.680  10.700  25.070  1.00 19.20           O
ATOM     73  ND2 ASN B   3      10.450   9.420  25.570  1.00 18.80           N
ATOM     74  N   GLN B   4       8.980  11.110  20.100  1.00 16.20           N
ATOM     75  CA  GLN B   4       9.350  11.130  18.680  1.00 15.90           C
ATOM     76  C   GLN B   4      10.860  11.380  18.510  1.00 15.60           C
ATOM     77  O   GLN B   4      11.660  11.080  19.390  1.00 16.10           O
ATOM     78  CB  GLN B   4       8.560  12.250  17.980  1.00 16.10           C
ATOM     79  CG  GLN B   4       7.070  12.280  18.310  1.00 16.80           C
ATOM     80  CD  GLN B   4       6.330  13.430  17.650  1.00 18.20           C
ATOM     81  OE1 GLN B   4       6.880  14.520  17.440  1.00 19.50           O
ATOM     82  NE2 GLN B   4       5.060  13.200  17.300  1.00 18.90           N
ATOM     83  N   HIS B   5      11.230  11.950  17.370  1.00 15.10           N
ATOM     84  CA  HIS B   5      12.630  12.250  17.060  1.00 14.80           C
ATOM     85  C   HIS B   5      12.870  13.750  16.850  1.00 14.50           C
ATOM     86  O   HIS B   5      12.010  14.470  16.340  1.00 15.10           O
ATOM     87  CB  HIS B   5      13.060  11.450  15.820  1.00 15.20           C
ATOM     88  CG  HIS B   5      14.530  11.530  15.520  1.00 16.20           C
ATOM     89  ND1 HIS B   5      15.480  10.740  16.120  1.00 17.50           N
ATOM     90  CD2 HIS B   5      15.230  12.310  14.670  1.00 17.10           C
ATOM     91  CE1 HIS B   5      16.700  11.020  15.650  1.00 18.10           C
ATOM     92  NE2 HIS B   5      16.570  11.970  14.770  1.00 17.90           N
TER
END
    """.trimIndent()

    // Zinc Finger Motif Beta-Hairpin / Alpha-Helix Domain
    val ZINC_FINGER_PDB = """
HEADER    DNA BINDING PROTEIN                     20-FEB-98   1ZAA
TITLE     STRUCTURE OF ZINC FINGER DOMAIN (ZIF268)
ATOM      1  N   TYR A   1       2.512  14.621  10.120  1.00 20.00           N
ATOM      2  CA  TYR A   1       3.450  13.510  10.420  1.00 19.50           C
ATOM      3  C   TYR A   1       4.890  14.020  10.510  1.00 18.80           C
ATOM      4  O   TYR A   1       5.210  15.110   9.990  1.00 19.10           O
ATOM      5  CB  TYR A   1       3.060  12.780  11.710  1.00 20.50           C
ATOM      6  N   GLU A   2       5.740  13.240  11.180  1.00 17.80           N
ATOM      7  CA  GLU A   2       7.150  13.610  11.350  1.00 17.20           C
ATOM      8  C   GLU A   2       7.960  12.510  12.040  1.00 16.50           C
ATOM      9  O   GLU A   2       7.480  11.410  12.330  1.00 16.90           O
ATOM     10  CB  GLU A   2       7.790  13.980   9.990  1.00 17.50           C
ATOM     11  N   CYS A   3       9.200  12.820  12.310  1.00 15.60           N
ATOM     12  CA  CYS A   3      10.080  11.890  12.980  1.00 14.90           C
ATOM     13  C   CYS A   3       9.940  10.460  12.440  1.00 14.30           C
ATOM     14  O   CYS A   3       9.540   9.550  13.160  1.00 14.80           O
ATOM     15  CB  CYS A   3      11.530  12.380  12.860  1.00 15.20           C
ATOM     16  SG  CYS A   3      12.650  11.270  13.780  1.00 16.50           S
HETATM   17 ZN   ZN  A  50      14.200  10.100  14.500  1.00 12.00          ZN
ATOM     18  N   HIS A   4      10.270  10.270  11.160  1.00 13.80           N
ATOM     19  CA  HIS A   4      10.180   8.960  10.510  1.00 13.20           C
ATOM     20  C   HIS A   4      11.450   8.130  10.740  1.00 12.80           C
ATOM     21  O   HIS A   4      12.540   8.680  10.920  1.00 13.30           O
ATOM     22  CB  HIS A   4       9.880   9.100   9.010  1.00 13.50           C
ATOM     23  CG  HIS A   4       8.500   9.630   8.720  1.00 14.10           C
ATOM     24  ND1 HIS A   4       7.360   8.880   8.880  1.00 15.20           N
ATOM     25  CD2 HIS A   4       8.080  10.840   8.260  1.00 14.80           C
ATOM     26  CE1 HIS A   4       6.300   9.610   8.530  1.00 15.80           C
ATOM     27  NE2 HIS A   4       6.710  10.810   8.140  1.00 15.50           N
ATOM     28  N   ARG A   5      11.290   6.810  10.730  1.00 12.20           N
ATOM     29  CA  ARG A   5      12.410   5.900  10.940  1.00 11.80           C
ATOM     30  C   ARG A   5      13.060   5.500   9.620  1.00 11.40           C
ATOM     31  O   ARG A   5      12.390   5.320   8.600  1.00 11.90           O
ATOM     32  CB  ARG A   5      11.930   4.630  11.660  1.00 12.10           C
TER
END
    """.trimIndent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoleculeViewer3DComponent(
    modifier: Modifier = Modifier,
    initialPdbContent: String = PresetPdbStructures.INSULIN_PDB,
    initialPdbId: String = "1TRZ"
) {
    var rawPdb by remember { mutableStateOf(initialPdbContent) }
    var pdbId by remember { mutableStateOf(initialPdbId) }
    var renderStyle by remember { mutableStateOf(MoleculeRenderStyle.BALL_AND_STICK) }
    var colorMode by remember { mutableStateOf(AtomColoringMode.ELEMENT) }

    // 3D Viewport State
    var rotX by remember { mutableFloatStateOf(25f) } // pitch in degrees
    var rotY by remember { mutableFloatStateOf(-35f) } // yaw in degrees
    var rotZ by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    var selectedAtom by remember { mutableStateOf<PdbAtom?>(null) }
    var isAutoRotating by remember { mutableStateOf(false) }

    // Parse PDB
    val pdbRecord = remember(rawPdb, pdbId) {
        PdbIO.parse(rawPdb, pdbId)
    }

    // Calculate Center of Mass
    val (centerX, centerY, centerZ, boundingRadius) = remember(pdbRecord) {
        if (pdbRecord.atoms.isEmpty()) {
            listOf(0.0, 0.0, 0.0, 10.0)
        } else {
            val avgX = pdbRecord.atoms.map { it.x }.average()
            val avgY = pdbRecord.atoms.map { it.y }.average()
            val avgZ = pdbRecord.atoms.map { it.z }.average()

            var maxDist = 0.0
            for (atom in pdbRecord.atoms) {
                val dx = atom.x - avgX
                val dy = atom.y - avgY
                val dz = atom.z - avgZ
                val dist = sqrt(dx * dx + dy * dy + dz * dz)
                if (dist > maxDist) maxDist = dist
            }
            listOf(avgX, avgY, avgZ, max(maxDist, 5.0))
        }
    }

    // Auto-rotation animation loop
    LaunchedEffect(isAutoRotating) {
        while (isAutoRotating) {
            rotY = (rotY + 1.2f) % 360f
            kotlinx.coroutines.delay(16) // ~60 FPS
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Molecule Info & Control Bar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "3D PDB Molecular Viewer",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF3B82F6)
                            ) {
                                Text(
                                    text = pdbRecord.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${pdbRecord.atoms.size} Atoms • ${pdbRecord.residueCount} Residues • Chains: ${pdbRecord.chains.joinToString(", ")}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Preset Structure Chooser
                    var showPresetMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showPresetMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Structures", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showPresetMenu,
                            onDismissRequest = { showPresetMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Human Insulin Monomer (1TRZ)") },
                                onClick = {
                                    rawPdb = PresetPdbStructures.INSULIN_PDB
                                    pdbId = "1TRZ"
                                    rotX = 25f
                                    rotY = -35f
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                    showPresetMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Zinc Finger Domain Zif268 (1ZAA)") },
                                onClick = {
                                    rawPdb = PresetPdbStructures.ZINC_FINGER_PDB
                                    pdbId = "1ZAA"
                                    rotX = 30f
                                    rotY = 45f
                                    zoomScale = 1.2f
                                    panOffset = Offset.Zero
                                    showPresetMenu = false
                                }
                            )
                        }
                    }
                }

                // Render Styles Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MoleculeRenderStyle.values().forEach { style ->
                        FilterChip(
                            selected = renderStyle == style,
                            onClick = { renderStyle = style },
                            label = { Text(style.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3B82F6),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Coloring Mode & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Color scheme chips
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AtomColoringMode.values().forEach { mode ->
                            FilterChip(
                                selected = colorMode == mode,
                                onClick = { colorMode = mode },
                                label = { Text(mode.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF6366F1),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Rotation / Reset controls
                    IconButton(
                        onClick = { isAutoRotating = !isAutoRotating },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isAutoRotating) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Auto Rotate",
                            tint = if (isAutoRotating) Color(0xFF10B981) else Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            rotX = 25f
                            rotY = -35f
                            rotZ = 0f
                            zoomScale = 1.0f
                            panOffset = Offset.Zero
                            selectedAtom = null
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Camera", tint = Color.White)
                    }
                }
            }
        }

        // Main 3D Canvas Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF030712))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    // Multi-touch gestures (Pinch to zoom + Pan)
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.3f, 4.5f)
                        panOffset += pan
                    }
                }
                .pointerInput(Unit) {
                    // Single finger drag to rotate around 3D axes
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        rotY = (rotY + dragAmount.x * 0.5f) % 360f
                        rotX = (rotX - dragAmount.y * 0.5f).coerceIn(-89f, 89f)
                    }
                }
                .testTag("molecule_3d_viewport")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (pdbRecord.atoms.isEmpty()) return@Canvas

                val canvasWidth = size.width
                val canvasHeight = size.height
                val midX = canvasWidth / 2f + panOffset.x
                val midY = canvasHeight / 2f + panOffset.y

                // Base scale mapping physical Angstroms to pixels
                val fitScale = (minOf(canvasWidth, canvasHeight) / (boundingRadius * 2.8f)).toFloat() * zoomScale

                // Trig conversions for Euler rotation
                val radX = Math.toRadians(rotX.toDouble())
                val radY = Math.toRadians(rotY.toDouble())
                val cosX = cos(radX).toFloat()
                val sinX = sin(radX).toFloat()
                val cosY = cos(radY).toFloat()
                val sinY = sin(radY).toFloat()

                // Project 3D Atoms to 2D Screen
                val projected = pdbRecord.atoms.map { atom ->
                    // 1. Center coordinates at (0, 0, 0)
                    val cx = (atom.x - centerX).toFloat()
                    val cy = (atom.y - centerY).toFloat()
                    val cz = (atom.z - centerZ).toFloat()

                    // 2. Rotate around Y axis (Yaw)
                    val x1 = cx * cosY + cz * sinY
                    val y1 = cy
                    val z1 = -cx * sinY + cz * cosY

                    // 3. Rotate around X axis (Pitch)
                    val x2 = x1
                    val y2 = y1 * cosX - z1 * sinX
                    val z2 = y1 * sinX + z1 * cosX

                    // 4. Perspective Projection & Scale
                    val depthFactor = 1f + (z2 / (boundingRadius * 4f).toFloat())
                    val screenX = midX + x2 * fitScale
                    val screenY = midY - y2 * fitScale // invert Y for screen coords

                    val baseRadius = when (renderStyle) {
                        MoleculeRenderStyle.BALL_AND_STICK -> getElementRadius(atom.element) * 4f * fitScale / 25f
                        MoleculeRenderStyle.RIBBON_BACKBONE -> 5f * zoomScale
                        MoleculeRenderStyle.SPACE_FILLING -> getVdwRadius(atom.element) * 7.5f * fitScale / 25f
                        MoleculeRenderStyle.SURFACE_WIREFRAME -> 3.5f * zoomScale
                    }.coerceIn(2f, 40f)

                    val atomColor = getAtomColor(atom, colorMode)

                    ProjectedAtom(
                        atom = atom,
                        projX = screenX,
                        projY = screenY,
                        projZ = z2,
                        radius = baseRadius * depthFactor,
                        color = atomColor
                    )
                }

                // 5. Draw Molecular Bonds / Backbone Connectors
                if (renderStyle == MoleculeRenderStyle.BALL_AND_STICK ||
                    renderStyle == MoleculeRenderStyle.RIBBON_BACKBONE ||
                    renderStyle == MoleculeRenderStyle.SURFACE_WIREFRAME
                ) {
                    val isBackboneOnly = renderStyle == MoleculeRenderStyle.RIBBON_BACKBONE

                    // Draw consecutive covalent links within the same residue or sequential CA-CA links
                    for (i in 0 until projected.size - 1) {
                        val a1 = projected[i]
                        val a2 = projected[i + 1]

                        val sameChain = a1.atom.chainId == a2.atom.chainId
                        if (!sameChain) continue

                        val dx = (a1.atom.x - a2.atom.x).toFloat()
                        val dy = (a1.atom.y - a2.atom.y).toFloat()
                        val dz = (a1.atom.z - a2.atom.z).toFloat()
                        val dist = sqrt(dx * dx + dy * dy + dz * dz)

                        val isConnected = if (isBackboneOnly) {
                            (a1.atom.name == "CA" && a2.atom.name == "CA" && dist < 4.5f) ||
                            (a1.atom.name in listOf("N", "CA", "C") && a2.atom.name in listOf("N", "CA", "C") && dist < 2.0f)
                        } else {
                            dist < 2.1f // Standard covalent bond cutoff (~1.5 Å for C-C, C-N, C-O)
                        }

                        if (isConnected) {
                            val bondZ = (a1.projZ + a2.projZ) / 2f
                            val bondAlpha = (0.5f + (bondZ / (boundingRadius * 3f).toFloat()) * 0.4f).coerceIn(0.2f, 1f)

                            val strokeW = when (renderStyle) {
                                MoleculeRenderStyle.RIBBON_BACKBONE -> 4f * zoomScale
                                MoleculeRenderStyle.SURFACE_WIREFRAME -> 1.5f * zoomScale
                                else -> 2.5f * zoomScale
                            }

                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(a1.color.copy(alpha = bondAlpha), a2.color.copy(alpha = bondAlpha)),
                                    start = Offset(a1.projX, a1.projY),
                                    end = Offset(a2.projX, a2.projY)
                                ),
                                start = Offset(a1.projX, a1.projY),
                                end = Offset(a2.projX, a2.projY),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // 6. Draw Depth-Sorted Atoms (Back-to-front rendering)
                val sortedAtoms = projected.sortedBy { it.projZ }

                for (pAtom in sortedAtoms) {
                    val isSelected = selectedAtom?.serial == pAtom.atom.serial

                    // Atmospheric depth lighting
                    val depthFactor = (0.45f + (pAtom.projZ / (boundingRadius * 2.5f).toFloat()) * 0.5f).coerceIn(0.25f, 1.0f)
                    val litColor = pAtom.color.copy(
                        red = (pAtom.color.red * depthFactor).coerceIn(0f, 1f),
                        green = (pAtom.color.green * depthFactor).coerceIn(0f, 1f),
                        blue = (pAtom.color.blue * depthFactor).coerceIn(0f, 1f)
                    )

                    // Draw Atom Sphere
                    drawCircle(
                        color = litColor,
                        radius = pAtom.radius,
                        center = Offset(pAtom.projX, pAtom.projY)
                    )

                    // Draw 3D Spherical Specular Highlight
                    if (pAtom.radius > 3f) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.55f),
                            radius = pAtom.radius * 0.35f,
                            center = Offset(pAtom.projX - pAtom.radius * 0.25f, pAtom.projY - pAtom.radius * 0.25f)
                        )
                    }

                    // Selected Halo
                    if (isSelected) {
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = pAtom.radius + 4.dp.toPx(),
                            center = Offset(pAtom.projX, pAtom.projY),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }
                }
            }

            // HUD Overlay (Orientation Axes Indicator)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x990F172A),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pitch: ${rotX.toInt()}° | Yaw: ${rotY.toInt()}° | Zoom: ${String.format("%.1fx", zoomScale)}",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Touch gesture hints
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0x990F172A),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
            ) {
                Text(
                    text = "🖐️ 1-Finger: Rotate • ✌️ 2-Finger: Pinch to Zoom & Pan",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Bottom Selected Residue / Atom Inspector Card
        Surface(
            color = Color(0xFF1E293B),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                val atom = selectedAtom ?: pdbRecord.atoms.firstOrNull()
                if (atom != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(getAtomColor(atom, AtomColoringMode.ELEMENT)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = atom.element,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${atom.resName} ${atom.resSeq} (Chain ${atom.chainId}) • Atom ${atom.name} (#${atom.serial})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Coord: (${String.format("%.2f", atom.x)}, ${String.format("%.2f", atom.y)}, ${String.format("%.2f", atom.z)}) Å • B-Factor: ${atom.tempFactor}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Cycle through atoms button
                        IconButton(
                            onClick = {
                                val curIdx = pdbRecord.atoms.indexOf(atom)
                                val nextIdx = (curIdx + 1) % pdbRecord.atoms.size
                                selectedAtom = pdbRecord.atoms[nextIdx]
                            }
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next Atom", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// CPK Element Colors
private fun getAtomColor(atom: PdbAtom, mode: AtomColoringMode): Color {
    return when (mode) {
        AtomColoringMode.ELEMENT -> {
            when (atom.element.uppercase()) {
                "C" -> Color(0xFF64748B) // Carbon -> Slate Grey
                "N" -> Color(0xFF3B82F6) // Nitrogen -> Blue
                "O" -> Color(0xFFEF4444) // Oxygen -> Red
                "S" -> Color(0xFFFBBF24) // Sulfur -> Amber / Yellow
                "P" -> Color(0xFFF97316) // Phosphorus -> Orange
                "H" -> Color(0xFFF1F5F9) // Hydrogen -> White / Silver
                "ZN", "FE", "MG", "CA" -> Color(0xFF10B981) // Metal ions -> Emerald
                else -> Color(0xFF8B5CF6) // Other -> Purple
            }
        }
        AtomColoringMode.CHAIN -> {
            when (atom.chainId) {
                "A" -> Color(0xFF3B82F6) // Chain A -> Blue
                "B" -> Color(0xFF10B981) // Chain B -> Green
                "C" -> Color(0xFFF59E0B) // Chain C -> Orange
                "D" -> Color(0xFFEC4899) // Chain D -> Pink
                else -> Color(0xFF8B5CF6)
            }
        }
        AtomColoringMode.RESIDUE_TYPE -> {
            when (atom.resName.uppercase()) {
                // Hydrophobic
                "ALA", "VAL", "LEU", "ILE", "MET", "PHE", "TRP", "PRO" -> Color(0xFF10B981)
                // Polar Charged Positive
                "ARG", "LYS", "HIS" -> Color(0xFF3B82F6)
                // Polar Charged Negative
                "ASP", "GLU" -> Color(0xFFEF4444)
                // Polar Uncharged
                "SER", "THR", "ASN", "GLN", "TYR", "CYS" -> Color(0xFFF59E0B)
                else -> Color(0xFF94A3B8)
            }
        }
        AtomColoringMode.B_FACTOR -> {
            val b = atom.tempFactor.coerceIn(0.0, 50.0) / 50.0
            Color(
                red = b.toFloat(),
                green = (1.0 - b).toFloat() * 0.7f,
                blue = (1.0 - b).toFloat(),
                alpha = 1.0f
            )
        }
    }
}

private fun getElementRadius(element: String): Float {
    return when (element.uppercase()) {
        "H" -> 1.0f
        "C" -> 1.7f
        "N" -> 1.5f
        "O" -> 1.4f
        "P" -> 1.8f
        "S" -> 1.8f
        "ZN", "FE", "MG" -> 2.0f
        else -> 1.6f
    }
}

private fun getVdwRadius(element: String): Float {
    return when (element.uppercase()) {
        "H" -> 1.2f
        "C" -> 1.7f
        "N" -> 1.55f
        "O" -> 1.52f
        "P" -> 1.8f
        "S" -> 1.8f
        "ZN", "FE", "MG" -> 2.2f
        else -> 1.7f
    }
}
