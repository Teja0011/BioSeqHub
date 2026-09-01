package com.example.features.visualization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biokt.sequence.CodonTable
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

enum class FeatureType(val label: String, val color: Color) {
    EXON("Exon", Color(0xFF10B981)),
    INTRON("Intron", Color(0xFF94A3B8)),
    PROMOTER("Promoter", Color(0xFF8B5CF6)),
    CDS("Coding Region (CDS)", Color(0xFF3B82F6)),
    UTR_5("5' UTR", Color(0xFFF59E0B)),
    UTR_3("3' UTR", Color(0xFFEC4899)),
    TATA_BOX("TATA Box", Color(0xFFEF4444)),
    KOZAK("Kozak Sequence", Color(0xFF06B6D4)),
    RE_SITE("Restriction Cut Site", Color(0xFFF97316)),
    CPG_ISLAND("CpG Island", Color(0xFF14B8A6))
}

data class SequenceFeature(
    val id: String,
    val name: String,
    val type: FeatureType,
    val start: Int, // 1-based inclusive
    val end: Int,   // 1-based inclusive
    val description: String
)

data class MotifMatch(
    val motifName: String,
    val pattern: String,
    val start: Int, // 1-based
    val end: Int,   // 1-based
    val matchedSeq: String,
    val color: Color
)

enum class ColorMode {
    NUCLEOTIDE_BASE,
    FEATURE_OVERLAY,
    GC_HEATMAP,
    MONOCHROME
}

object PresetGenomicData {
    val TP53_EXON_REGION = """
        ATGGAGGAGCCGCAGTCAGATCCTAGCGTCGAGCCCCCTCTGAGTCAGGAAACATTTTCAGACCTATGGAAACTACTTCCTGAAAACAACGTTCTGTCCCCCTTGCCGTCCCAAGCAATGGATGATTTGATGCTGTCCCCGGACGATATTGAACAATGGTTCACTGAAGACCCAGGTCCAGATGAAGCTCCCAGAATGCCAGAGGCTGCTCCCCGCGTGGCCCCTGCACCAGCAGCTCCTACACCGGCGGCCCCTGCACCAGCCCCCTCCTGGCCCCTGTCATCTTCTGTCCCTTCCCAGAAAACCTACCAGGGCAGCTACGGTTTCCGTCTGGGCTTCTTGCATTCTGGGACAGCCAAGTCTGTGACTTGCACGTACTCCCCTGCCCTCAACAAGATGTTTTGCCAACTGGCCAAGACCTGCCCTGTGCAGCTGTGGGTTGATTCCACACCCCCGCCCGGCACCCGCGTCCGCGCCATGGCCATCTACAAGCAGTCACAGCACATGACGGAGGTTGTGAGGCGCTGCCCCCACCATGAGCGCTGCTCAGATAGCGATGGTCTGGCCCCTCCTCAGCATCTTATCCGAGTGGAAGGAAATTTGCGTGTGGAGTATTTGGATGACAGAAACACTTTTCGACATAGTGTGGTGGTGCCCTATGAGCCGCCTGAGGTTGGCTCTGACTGTACCACCATCCACTACAACTACATGTGTAACAGTTCCTGCATGGGCGGCATGAACCGGAGGCCCATCCTCACCATCATCACACTGGAAGACTCCAGTGGTAATCTACTGGGACGGAACAGCTTTGAGGTGCGTGTTTGTGCCTGTCCTGGGAGAGACCGGCGCACAGAGGAAGAGAATCTCCGCAAGAAAGGGGAGCCTCACCACGAGCTGCCCCCAGGGAGCACTAAGCGAGCACTGCCCAACAACACCAGCTCCTCTCCCCAGCCAAAGAAGAAACCACTGGATGGAGAATATTTCACCCTTCAGATCCGTGGGCGTGAGCGCTTCGAGATGTTCCGAGAGCTGAATGAGGCCTTGGAACTCAAGGATGCCCAGGCTGGGAAGGAGCCAGGGGGGAGCAGGGCTCACTCCAGCCACCTGAAGTCCAAAAAGGGTCAGTCTACCTCCCGCCATAAAAAACTCATGTTCAAGACAGAAGGGCCTGACTCAGACTGA
    """.trimIndent().replace("\n", "").replace(" ", "").uppercase()

    val TP53_FEATURES = listOf(
        SequenceFeature("F1", "5' UTR Segment", FeatureType.UTR_5, 1, 15, "Transcription initiation buffer"),
        SequenceFeature("F2", "TATA Box Motif", FeatureType.TATA_BOX, 40, 46, "Core promoter binding region (TATAAA)"),
        SequenceFeature("F3", "Kozak Sequence", FeatureType.KOZAK, 1, 10, "Ribosome translation initiation signal (GCCACCATGG)"),
        SequenceFeature("F4", "Exon 4 (Transactivation Domain)", FeatureType.EXON, 1, 280, "P53 N-terminal transactivation domain (TAD1 & TAD2)"),
        SequenceFeature("F5", "Exon 5 (DNA Binding Core)", FeatureType.EXON, 281, 600, "Sequence-specific DNA binding domain"),
        SequenceFeature("F6", "Exon 6 (DNA Binding Core Part 2)", FeatureType.EXON, 601, 850, "Contains hotspot mutation residues R248 & R273"),
        SequenceFeature("F7", "Exon 7 (Tetramerization Domain)", FeatureType.EXON, 851, 1179, "Oligomerization domain mediating tetramer formation"),
        SequenceFeature("F8", "Stop Codon (TGA)", FeatureType.UTR_3, 1180, 1182, "Translation termination signal"),
        SequenceFeature("F9", "EcoRI Cut Site", FeatureType.RE_SITE, 310, 315, "Restriction endonuclease recognition site (GAATTC)"),
        SequenceFeature("F10", "CpG Island Methylation Region", FeatureType.CPG_ISLAND, 180, 340, "High GC density promoter methylation zone")
    )

    val SARS_COV2_RBD = """
        AATGTTACAAATTTTTAGCCCAACTGCTTAATAGTACTTTTTCTTGTTTACTCTCTTTGTGTGAATAGTTTTTGGCATTTTTATTCTTTTTCTTTTTCTCTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
    """.trimIndent().replace("\n", "").replace(" ", "").uppercase()

    val GFP_SEQUENCE = """
        ATGGTGAGCAAGGGCGAGGAGCTGTTCACCGGGGTGGTGCCCATCCTGGTCGAGCTGGACGGCGACGTAAACGGCCACAAGTTCAGCGTGTCCGGCGAGGGCGAGGGCGATGCCACCTACGGCAAGCTGACCCTGAAGTTCATCTGCACCACCGGCAAGCTGCCCGTGCCCTGGCCCACCCTCGTGACCACCCTGACCTACGGCGTGCAGTGCTTCAGCCGCTACCCCGACCACATGAAGCAGCACGACTTCTTCAAGTCCGCCATGCCCGAAGGCTACGTCCAGGAGCGCACCATCTTCTTCAAGGACGACGGCAACTACAAGACCCGCGCCGAGGTGAAGTTCGAGGGCGACACCCTGGTGAACCGCATCGAGCTGAAGGGCATCGACTTCAAGGAGGACGGCAACATCCTGGGGCACAAGCTGGAGTACAACTACAACAGCCACAACGTCTATATCATGGCCGACAAGCAGAAGAACGGCATCAAGGTGAACTTCAAGATCCGCCACAACATCGAGGACGGCAGCGTGCAGCTCGCCGACCACTACCAGCAGAACACCCCCATCGGCGACGGCCCCGTGCTGCTGCCCGACAACCACTACCTGAGCACCCAGTCCGCCCTGAGCAAAGACCCCAACGAGAAGCGCGATCACATGGTCCTGCTGGAGTTCGTGACCGCCGCCGGGATCACTCTCGGCATGGACGAGCTGTACAAGTAA
    """.trimIndent().replace("\n", "").replace(" ", "").uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequenceVisualizerComponent(
    modifier: Modifier = Modifier,
    initialSequence: String = PresetGenomicData.TP53_EXON_REGION,
    initialFeatures: List<SequenceFeature> = PresetGenomicData.TP53_FEATURES,
    onExportFasta: ((String) -> Unit)? = null
) {
    var rawSequence by remember { mutableStateOf(initialSequence) }
    var features by remember { mutableStateOf(initialFeatures) }
    var basesPerRow by remember { mutableIntStateOf(30) }
    var colorMode by remember { mutableStateOf(ColorMode.NUCLEOTIDE_BASE) }
    var showComplement by remember { mutableStateOf(true) }
    var showRnaTranscript by remember { mutableStateOf(false) }
    var showAminoAcids by remember { mutableStateOf(true) }
    var showFeaturesOverlay by remember { mutableStateOf(true) }

    // Search & Motifs
    var searchQuery by remember { mutableStateOf("") }
    var activeMotifFilter by remember { mutableStateOf<String?>(null) }
    var selectedBaseIndex by remember { mutableStateOf<Int?>(null) } // 1-based

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Clean sequence
    val cleanSeq = remember(rawSequence) {
        rawSequence.uppercase().filter { it in "ATGCUN" }
    }

    // Detected Motifs
    val detectedMotifs = remember(cleanSeq, searchQuery, activeMotifFilter) {
        val list = mutableListOf<MotifMatch>()
        
        // 1. TATA Box
        findMotifMatches(cleanSeq, "TATAAA", "TATA Box", Color(0xFFEF4444), list)
        findMotifMatches(cleanSeq, "TATAAT", "Pribnow Box", Color(0xFFF43F5E), list)
        // 2. Start Codons
        findMotifMatches(cleanSeq, "ATG", "Start Codon (ATG)", Color(0xFF10B981), list)
        // 3. Stop Codons
        findMotifMatches(cleanSeq, "TAA", "Stop Codon (TAA)", Color(0xFFDC2626), list)
        findMotifMatches(cleanSeq, "TAG", "Stop Codon (TAG)", Color(0xFFDC2626), list)
        findMotifMatches(cleanSeq, "TGA", "Stop Codon (TGA)", Color(0xFFDC2626), list)
        // 4. Restriction Sites
        findMotifMatches(cleanSeq, "GAATTC", "EcoRI Site", Color(0xFFF97316), list)
        findMotifMatches(cleanSeq, "GGATCC", "BamHI Site", Color(0xFFD97706), list)
        findMotifMatches(cleanSeq, "AAGCTT", "HindIII Site", Color(0xFFB45309), list)
        // 5. E-Box
        findMotifMatches(cleanSeq, "CACGTG", "E-Box (MYC)", Color(0xFF8B5CF6), list)
        // 6. CpG dinucleotide clusters
        findMotifMatches(cleanSeq, "CGCGCG", "CpG Island Core", Color(0xFF14B8A6), list)

        // Custom Search Query
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().uppercase()
            findMotifMatches(cleanSeq, q, "Search Match: $q", Color(0xFFFF0055), list)
        }

        if (activeMotifFilter != null) {
            list.filter { it.motifName == activeMotifFilter }
        } else {
            list
        }
    }

    // Chunks for virtualized scrolling
    val rowsCount = remember(cleanSeq.length, basesPerRow) {
        if (cleanSeq.isEmpty()) 0 else (cleanSeq.length + basesPerRow - 1) / basesPerRow
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Controls & Stats Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = HighDensityCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header & Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("DNA/RNA Sequence Visualizer", fontWeight = FontWeight.Bold, color = HighDensityNavy, fontSize = 15.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HighDensityPeriwinkle
                            ) {
                                Text(
                                    text = "${cleanSeq.length} bp",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HighDensityNavy,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val gc = if (cleanSeq.isNotEmpty()) {
                            (cleanSeq.count { it == 'G' || it == 'C' }.toDouble() / cleanSeq.length) * 100.0
                        } else 0.0
                        Text("GC Content: ${String.format("%.1f", gc)}% • Motifs: ${detectedMotifs.size} • Features: ${features.size}", fontSize = 11.sp, color = HighDensityTextSecondary)
                    }

                    // Preset Selector Menu
                    var showPresetMenu by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { showPresetMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp), tint = HighDensityNavy)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Presets", fontSize = 11.sp, color = HighDensityNavy, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showPresetMenu,
                            onDismissRequest = { showPresetMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("TP53 Human Exons (1,182 bp)") },
                                onClick = {
                                    rawSequence = PresetGenomicData.TP53_EXON_REGION
                                    features = PresetGenomicData.TP53_FEATURES
                                    showPresetMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("GFP Reporter Gene (720 bp)") },
                                onClick = {
                                    rawSequence = PresetGenomicData.GFP_SEQUENCE
                                    features = listOf(
                                        SequenceFeature("GFP1", "GFP Coding Sequence", FeatureType.CDS, 1, 717, "Green Fluorescent Protein"),
                                        SequenceFeature("GFP2", "Chromophore Triad (SYG)", FeatureType.EXON, 196, 204, "Fluorescent emission center")
                                    )
                                    showPresetMenu = false
                                }
                            )
                        }
                    }
                }

                // Motif Search Field & Jump
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search motif / seq (e.g. TATAAA, ATG, GAATTC)...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HighDensityNavy, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("seq_motif_search_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Jump to first match button
                    Button(
                        onClick = {
                            if (detectedMotifs.isNotEmpty()) {
                                val first = detectedMotifs.first()
                                val targetRow = (first.start - 1) / basesPerRow
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetRow)
                                }
                                selectedBaseIndex = first.start
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy),
                        modifier = Modifier.height(48.dp),
                        enabled = detectedMotifs.isNotEmpty()
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jump", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Motif Filter Pills Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = activeMotifFilter == null,
                        onClick = { activeMotifFilter = null },
                        label = { Text("All Motifs (${detectedMotifs.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = HighDensityPeriwinkle)
                    )

                    listOf(
                        "TATA Box" to Color(0xFFEF4444),
                        "Start Codon (ATG)" to Color(0xFF10B981),
                        "Stop Codon (TAA)" to Color(0xFFDC2626),
                        "EcoRI Site" to Color(0xFFF97316),
                        "E-Box (MYC)" to Color(0xFF8B5CF6),
                        "CpG Island Core" to Color(0xFF14B8A6)
                    ).forEach { (mName, mColor) ->
                        val count = detectedMotifs.count { it.motifName == mName }
                        if (count > 0 || activeMotifFilter == mName) {
                            FilterChip(
                                selected = activeMotifFilter == mName,
                                onClick = {
                                    activeMotifFilter = if (activeMotifFilter == mName) null else mName
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(mColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$mName ($count)", fontSize = 11.sp)
                                    }
                                }
                            )
                        }
                    }
                }

                // View Toggle Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = showComplement,
                            onClick = { showComplement = !showComplement },
                            label = { Text("3'->5'", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = showRnaTranscript,
                            onClick = { showRnaTranscript = !showRnaTranscript },
                            label = { Text("RNA", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = showAminoAcids,
                            onClick = { showAminoAcids = !showAminoAcids },
                            label = { Text("AA", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = showFeaturesOverlay,
                            onClick = { showFeaturesOverlay = !showFeaturesOverlay },
                            label = { Text("Features", fontSize = 10.sp) }
                        )
                    }

                    // Bases Per Row Chooser
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Zoom: ", fontSize = 11.sp, color = HighDensityTextSecondary)
                        listOf(20, 30, 50).forEach { bp ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (basesPerRow == bp) HighDensityNavy else HighDensityPeriwinkle)
                                    .clickable { basesPerRow = bp }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$bp",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (basesPerRow == bp) Color.White else HighDensityNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        // Mini-map Overview Scrubber Canvas
        SequenceMiniMap(
            sequenceLength = cleanSeq.length,
            features = features,
            motifs = detectedMotifs,
            currentVisibleRow = listState.firstVisibleItemIndex,
            totalRows = rowsCount,
            basesPerRow = basesPerRow,
            onScrubToPosition = { pos ->
                val targetRow = (pos - 1) / basesPerRow
                coroutineScope.launch {
                    listState.scrollToItem(targetRow.coerceIn(0, max(0, rowsCount - 1)))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Virtualized Sequence Grid (LazyColumn for extreme efficiency with long sequences)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(rowsCount) { rowIndex ->
                    val startIdx = rowIndex * basesPerRow
                    val endIdx = min(startIdx + basesPerRow, cleanSeq.length)
                    val rowSub = cleanSeq.substring(startIdx, endIdx)

                    SequenceRowItem(
                        startPos = startIdx + 1,
                        endPos = endIdx,
                        sequenceChunk = rowSub,
                        features = if (showFeaturesOverlay) features else emptyList(),
                        motifs = detectedMotifs,
                        showComplement = showComplement,
                        showRna = showRnaTranscript,
                        showAminoAcids = showAminoAcids,
                        selectedBasePos = selectedBaseIndex,
                        onBaseClick = { pos -> selectedBaseIndex = pos }
                    )
                }
            }
        }

        // Bottom Selected Base Inspector Sheet
        AnimatedVisibility(visible = selectedBaseIndex != null) {
            val selPos = selectedBaseIndex ?: 1
            if (selPos in 1..cleanSeq.length) {
                val base = cleanSeq[selPos - 1]
                val comp = when (base) {
                    'A' -> 'T'; 'T' -> 'A'; 'G' -> 'C'; 'C' -> 'G'; 'U' -> 'A'; else -> 'N'
                }
                val rna = if (base == 'T') 'U' else base
                val codonIdx = (selPos - 1) / 3
                val codonStart = codonIdx * 3
                val codon = if (codonStart + 3 <= cleanSeq.length) cleanSeq.substring(codonStart, codonStart + 3) else "..."
                val aa = if (codon.length == 3) CodonTable.Standard.translate(codon) else '?'
                val overlappingFeatures = features.filter { selPos in it.start..it.end }
                val overlappingMotifs = detectedMotifs.filter { selPos in it.start..it.end }

                Surface(
                    color = HighDensityNavy,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(getBaseColor(base)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$base", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Position: $selPos bp (Codon #${codonIdx + 1}: $codon -> $aa)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text("Complement: $comp • RNA: $rna • AA: $aa", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                            }

                            IconButton(onClick = { selectedBaseIndex = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }

                        if (overlappingFeatures.isNotEmpty() || overlappingMotifs.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                overlappingFeatures.forEach { f ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = f.type.color.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, f.type.color)
                                    ) {
                                        Text(
                                            text = "Feature: ${f.name} (${f.start}..${f.end})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = f.type.color,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                overlappingMotifs.forEach { m ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = m.color.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, m.color)
                                    ) {
                                        Text(
                                            text = "Motif: ${m.motifName}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = m.color,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SequenceRowItem(
    startPos: Int,
    endPos: Int,
    sequenceChunk: String,
    features: List<SequenceFeature>,
    motifs: List<MotifMatch>,
    showComplement: Boolean,
    showRna: Boolean,
    showAminoAcids: Boolean,
    selectedBasePos: Int?,
    onBaseClick: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = HighDensityCardBg.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, HighDensityBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            // Coordinate ruler header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = String.format("%05d", startPos),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = HighDensityTextSecondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%05d", endPos),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = HighDensityTextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Primary 5'->3' DNA Base Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in sequenceChunk.indices) {
                    val pos = startPos + i
                    val base = sequenceChunk[i]
                    val isSelected = selectedBasePos == pos

                    // Check if this position is inside an active motif
                    val activeMotif = motifs.find { pos in it.start..it.end }
                    val activeFeature = features.find { pos in it.start..it.end }

                    val baseBg = when {
                        isSelected -> HighDensityNavy
                        activeMotif != null -> activeMotif.color.copy(alpha = 0.25f)
                        activeFeature != null -> activeFeature.type.color.copy(alpha = 0.18f)
                        else -> Color.Transparent
                    }

                    val textColor = when {
                        isSelected -> Color.White
                        else -> getBaseColor(base)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(baseBg)
                            .border(
                                width = if (isSelected) 1.5.dp else if (activeMotif != null) 1.dp else 0.dp,
                                color = if (isSelected) HighDensityNavy else activeMotif?.color ?: Color.Transparent,
                                shape = RoundedCornerShape(3.dp)
                            )
                            .clickable { onBaseClick(pos) }
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$base",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = textColor
                        )
                    }
                }
            }

            // 3'->5' Complement strand
            if (showComplement) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in sequenceChunk.indices) {
                        val base = sequenceChunk[i]
                        val comp = when (base) {
                            'A' -> 'T'; 'T' -> 'A'; 'G' -> 'C'; 'C' -> 'G'; 'U' -> 'A'; else -> 'N'
                        }
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$comp",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = getBaseColor(comp).copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // RNA Transcript row
            if (showRna) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (i in sequenceChunk.indices) {
                        val base = sequenceChunk[i]
                        val rna = if (base == 'T') 'U' else base
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rna",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = Color(0xFF6366F1),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Translated Amino Acid Row (Frame 0)
            if (showAminoAcids) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    var i = 0
                    while (i < sequenceChunk.length) {
                        val pos = startPos + i
                        val codonPhase = (pos - 1) % 3
                        if (codonPhase == 0 && i + 3 <= sequenceChunk.length) {
                            val codon = sequenceChunk.substring(i, i + 3)
                            val aa = CodonTable.Standard.translate(codon)
                            Box(
                                modifier = Modifier
                                    .weight(3f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(HighDensityPeriwinkle.copy(alpha = 0.8f))
                                    .padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$aa",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = HighDensityNavy
                                )
                            }
                            i += 3
                        } else {
                            Box(modifier = Modifier.weight(1f))
                            i++
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SequenceMiniMap(
    sequenceLength: Int,
    features: List<SequenceFeature>,
    motifs: List<MotifMatch>,
    currentVisibleRow: Int,
    totalRows: Int,
    basesPerRow: Int,
    onScrubToPosition: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HighDensityCardBg)
            .border(1.dp, HighDensityBorder, RoundedCornerShape(8.dp))
            .pointerInput(sequenceLength) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    val targetBp = (fraction * sequenceLength).toInt().coerceIn(1, sequenceLength)
                    onScrubToPosition(targetBp)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (sequenceLength <= 0) return@Canvas

            // Background baseline
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2.dp.toPx()
            )

            // Draw Features as colored blocks
            features.forEach { f ->
                val startX = (f.start.toFloat() / sequenceLength) * size.width
                val endX = (f.end.toFloat() / sequenceLength) * size.width
                val width = max(endX - startX, 3f)
                drawRoundRect(
                    color = f.type.color.copy(alpha = 0.7f),
                    topLeft = Offset(startX, size.height * 0.15f),
                    size = Size(width, size.height * 0.7f),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }

            // Draw Motifs as small tick marks
            motifs.forEach { m ->
                val startX = (m.start.toFloat() / sequenceLength) * size.width
                drawCircle(
                    color = m.color,
                    radius = 2.5.dp.toPx(),
                    center = Offset(startX, size.height / 2)
                )
            }

            // Draw Visible Window Viewport
            if (totalRows > 0) {
                val startBp = currentVisibleRow * basesPerRow
                val endBp = min(startBp + basesPerRow * 4, sequenceLength)
                val viewX = (startBp.toFloat() / sequenceLength) * size.width
                val viewW = max(((endBp - startBp).toFloat() / sequenceLength) * size.width, 20f)

                drawRoundRect(
                    color = HighDensityNavy.copy(alpha = 0.35f),
                    topLeft = Offset(viewX, 0f),
                    size = Size(viewW, size.height),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            }
        }
    }
}

fun getBaseColor(base: Char): Color {
    return when (base.uppercaseChar()) {
        'A' -> Color(0xFF16A34A) // Adenine -> Emerald Green
        'T', 'U' -> Color(0xFFDC2626) // Thymine / Uracil -> Crimson Red
        'C' -> Color(0xFF2563EB) // Cytosine -> Royal Blue
        'G' -> Color(0xFFD97706) // Guanine -> Amber
        else -> Color(0xFF7C3AED) // Other / Inosine -> Purple
    }
}

private fun findMotifMatches(
    sequence: String,
    pattern: String,
    motifName: String,
    color: Color,
    outList: MutableList<MotifMatch>
) {
    if (pattern.isBlank() || sequence.length < pattern.length) return
    var startIndex = 0
    while (true) {
        val found = sequence.indexOf(pattern, startIndex)
        if (found == -1) break
        outList.add(
            MotifMatch(
                motifName = motifName,
                pattern = pattern,
                start = found + 1,
                end = found + pattern.length,
                matchedSeq = pattern,
                color = color
            )
        )
        startIndex = found + 1
    }
}
