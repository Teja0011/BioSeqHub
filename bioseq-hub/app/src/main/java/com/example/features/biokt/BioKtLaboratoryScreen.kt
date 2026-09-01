package com.example.features.biokt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.biokt.alignment.MultipleSequenceAlignment
import com.example.biokt.alignment.NeedlemanWunsch
import com.example.biokt.alignment.SmithWaterman
import com.example.biokt.analysis.CpGIslandDetector
import com.example.biokt.analysis.OrfFinder
import com.example.biokt.analysis.PhylogeneticTree
import com.example.biokt.analysis.PrimerDesigner
import com.example.biokt.io.FastaIO
import com.example.biokt.io.FastqIO
import com.example.biokt.sequence.DnaSequence
import com.example.biokt.sequence.KmerCounter
import com.example.biokt.sequence.MotifFinder
import com.example.biokt.structure.SecondaryStructurePredictor
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioKtLaboratoryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🧬 Sequences", "🏛️ 3D PDB Molecule", "📜 FASTA/Q IO", "🔬 Align & MSA", "🧪 ORF & Primers", "🌳 Phylogeny")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("BioKt Bioinformatics Suite", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(percent = 50),
                            color = HighDensityPeriwinkle
                        ) {
                            Text(
                                text = "v1.4.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = HighDensityNavy,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HighDensityNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = HighDensityNavBg)
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
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = HighDensityNavBg,
                contentColor = HighDensityNavy,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) HighDensityNavy else HighDensityTextSecondary
                            )
                        }
                    )
                }
            }

            if (selectedTab == 1) {
                // Full viewport 3D Molecule component
                com.example.features.visualization.MoleculeViewer3DComponent(
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> SequenceToolsView()
                        2 -> FastaFastqIoView()
                        3 -> AlignmentMsaView()
                        4 -> OrfPrimersView()
                        5 -> PhylogenyView()
                    }
                }
            }
        }
    }
}

@Composable
fun SequenceToolsView() {
    var inputSeq by remember { mutableStateOf("ATGCGATCGATCGATCGATCGATCGATCGCGCTAGCTAGCTAGCTAATAAATAGCC") }
    val dna = remember(inputSeq) { DnaSequence(inputSeq) }
    val rna = remember(dna) { dna.transcribe() }
    val protein = remember(dna) { dna.translate() }
    val kmers = remember(inputSeq) { KmerCounter.countKmers(inputSeq, 3).take(8) }
    val motifs = remember(inputSeq) { MotifFinder.scanKnownMotifs(inputSeq) }
    val secStruct = remember(protein) { SecondaryStructurePredictor.predict(protein.rawSequence) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt Sequence Engine", fontWeight = FontWeight.Bold, color = HighDensityNavy, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputSeq,
                    onValueChange = { inputSeq = it },
                    label = { Text("DNA Sequence (5'->3')") },
                    modifier = Modifier.fillMaxWidth().testTag("biokt_seq_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Physico-chemical Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Length",
                value = "${dna.length} bp",
                subtitle = "Nucleotides",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "GC Content",
                value = "${String.format("%.1f", dna.gcContent)}%",
                subtitle = "G + C Ratio",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Mol. Weight",
                value = "${String.format("%.0f", dna.molecularWeight)} Da",
                subtitle = "Estimated",
                modifier = Modifier.weight(1f)
            )
        }

        // Transformations
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Transformations & Central Dogma", fontWeight = FontWeight.Bold, color = HighDensityNavy)

                BioResultField(label = "Reverse Complement (5'->3')", value = dna.reverseComplement().rawSequence)
                BioResultField(label = "Transcribed RNA", value = rna.rawSequence)
                BioResultField(label = "Translated Protein (Frame 0)", value = protein.rawSequence)
                BioResultField(
                    label = "Chou-Fasman 2° Structure",
                    value = "${secStruct.predictedStateString} (Helix: ${String.format("%.1f", secStruct.helixPercentage)}%, Sheet: ${String.format("%.1f", secStruct.sheetPercentage)}%)"
                )
            }
        }

        // K-mers and Motifs
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("K-mer Spectrum & Known Motifs", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Top 3-mers Frequency:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    kmers.take(4).forEach {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HighDensityPeriwinkle
                        ) {
                            Text(
                                text = "${it.kmer}: ${it.count}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = HighDensityNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Discovered Promoter Motifs:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                if (motifs.isEmpty()) {
                    Text("No standard promoter motifs detected in current window.", style = MaterialTheme.typography.bodySmall, color = HighDensityTextSecondary)
                } else {
                    motifs.forEach { m ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• ${m.motifName} (${m.matchedSequence})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Pos: ${m.startIndex}..${m.endIndex}", fontSize = 12.sp, color = HighDensityNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FastaFastqIoView() {
    val sampleFasta = """
        >sp|P01308|INS_HUMAN Insulin OS=Homo sapiens
        MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTRREAED
        LQVGQVELGGGPGAGSLQPLALEGSLQKRGIVEQCCTSICSLYQLENYCN
        >sp|P01315|INS_PIG Insulin OS=Sus scrofa
        MALWTRLLPLLALLALWAPAPAQAFVNQHLCGSHLVEALYLVCGERGFFYTPKARREAEN
        PQAGAVELGGGLGGLQALALEGPPQKRGIVEQCCTSICSLYQLENYCN
    """.trimIndent()

    val sampleFastq = """
        @SEQ_ID_001 Illumina HiSeq Read 1
        GATCGATCGATCGATCGATC
        +
        IIIIIIIIIIIIIIIIIIII
        @SEQ_ID_002 Illumina HiSeq Read 2
        ATCGATCGATCGATCGATCG
        +
        IIIIIIIIIIIIIIIIIIII
    """.trimIndent()

    var fastaInput by remember { mutableStateOf(sampleFasta) }
    var fastqInput by remember { mutableStateOf(sampleFastq) }

    val parsedFasta = remember(fastaInput) { FastaIO.parse(fastaInput) }
    val parsedFastq = remember(fastqInput) { FastqIO.parse(fastqInput) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt FASTA Parser (${parsedFasta.size} records)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fastaInput,
                    onValueChange = { fastaInput = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                parsedFasta.forEach { record ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighDensityPeriwinkle.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ID: ${record.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityNavy)
                            Text("Desc: ${record.description}", fontSize = 11.sp, color = HighDensityTextSecondary)
                            Text("Length: ${record.length} aa • Seq: ${record.sequence.take(30)}...", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt FASTQ Phred Quality Analyzer (${parsedFastq.size} reads)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fastqInput,
                    onValueChange = { fastqInput = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))
                parsedFastq.forEach { read ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighDensityCardBg,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("@${read.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityNavy)
                                Text("Mean Q-Score: ${String.format("%.1f", read.meanQualityScore)}", fontSize = 11.sp, color = HighDensityTextSecondary)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (read.q30Percent >= 90) Color(0xFFDCFCE7) else HighDensityPeriwinkle
                            ) {
                                Text(
                                    text = "Q30: ${String.format("%.0f", read.q30Percent)}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (read.q30Percent >= 90) Color(0xFF166534) else HighDensityNavy,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlignmentMsaView() {
    var seqA by remember { mutableStateOf("MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTR") }
    var seqB by remember { mutableStateOf("MALWTRLLPLLALLALWAPAPAQAFVNQHLCGSHLVEALYLVCGERGFFYTPKAR") }

    val nwAlign = remember(seqA, seqB) { NeedlemanWunsch.align(seqA, seqB, isProtein = true) }
    val swAlign = remember(seqA, seqB) { SmithWaterman.align(seqA, seqB, isProtein = true) }

    val msaResult = remember {
        MultipleSequenceAlignment.align(
            listOf(
                "Human_INS" to "MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTR",
                "Pig_INS" to "MALWTRLLPLLALLALWAPAPAQAFVNQHLCGSHLVEALYLVCGERGFFYTPKAR",
                "Bovine_INS" to "MALWTRLLPLLALLALWAPAPAQAFVNQHLCGSHLVEALYLVCGERGFFYTPKA"
            ),
            isProtein = true
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt Pairwise Alignments (BLOSUM62)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = seqA,
                    onValueChange = { seqA = it },
                    label = { Text("Sequence 1 (Human)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = seqB,
                    onValueChange = { seqB = it },
                    label = { Text("Sequence 2 (Pig)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighDensityPeriwinkle,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Needleman-Wunsch", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityNavy)
                            Text("Score: ${nwAlign.score}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Identity: ${String.format("%.1f", nwAlign.identityPercent)}%", fontSize = 11.sp)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighDensityPeriwinkle,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Smith-Waterman", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityNavy)
                            Text("Score: ${swAlign.score}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Identity: ${String.format("%.1f", swAlign.identityPercent)}%", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Multiple Sequence Alignment (MSA Clustal-Style)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(10.dp))

                msaResult.alignedSequences.forEach { (id, seq) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(id, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = HighDensityNavy, modifier = Modifier.width(90.dp))
                        Text(seq.take(35) + "...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Consensus", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF166534), modifier = Modifier.width(90.dp))
                    Text(msaResult.consensusSequence.take(35) + "...", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OrfPrimersView() {
    var dnaSeq by remember {
        mutableStateOf("ATGGCCCTGTGGATGCGCCTCCTGCCCCTGCTGGCGCTGCTGGCCCTCTGGGGACCTGACCCAGCCGCAGCCTTTGTGAACCAACACCTGTGCGGCTCACACCTGGTGGAAGCTCTCTACCTAGTGTGCGGGGAACGAGGCTTCTTCTACACACCCAAGACCCGCCGGGAGGCAGAGGACCTGCAGGTAGCCTGA")
    }

    val orfs = remember(dnaSeq) { OrfFinder.findOrfs(dnaSeq, minProteinLength = 10) }
    val primers = remember(dnaSeq) { PrimerDesigner.designPrimers(dnaSeq, primerLength = 18, targetAmpliconMinLen = 80, targetAmpliconMaxLen = 180) }
    val cpgs = remember(dnaSeq) { CpGIslandDetector.detect(dnaSeq, windowSize = 80, stepSize = 20) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt 6-Frame ORF Finder (${orfs.size} ORFs)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(8.dp))

                orfs.take(3).forEach { orf ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighDensityPeriwinkle.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Frame: ${if (orf.frame > 0) "+${orf.frame}" else "${orf.frame}"}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityNavy)
                                Text("Pos: ${orf.startNucleotideIndex}..${orf.endNucleotideIndex} (${orf.proteinLength} aa)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("Protein: ${orf.translatedProtein.take(40)}...", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt PCR Primer Designer (${primers.size} Pairs)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(8.dp))

                primers.take(2).forEachIndexed { idx, pair ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighDensityCardBg,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pair #${idx + 1} (Amplicon: ${pair.ampliconLength} bp • ΔTm: ${String.format("%.1f", pair.tmDifference)}°C)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HighDensityNavy)
                            Text("Fwd (5'->3'): ${pair.forwardPrimer.sequence} (Tm: ${String.format("%.1f", pair.forwardPrimer.meltingTempTm)}°C, GC: ${String.format("%.0f", pair.forwardPrimer.gcPercent)}%)", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text("Rev (5'->3'): ${pair.reversePrimer.sequence} (Tm: ${String.format("%.1f", pair.reversePrimer.meltingTempTm)}°C, GC: ${String.format("%.0f", pair.reversePrimer.gcPercent)}%)", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhylogenyView() {
    val sampleTree = remember {
        PhylogeneticTree.buildUpgmaTree(
            listOf(
                "Human" to "MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTR",
                "Chimp" to "MALWMRLLPLLALLALWGPDPAAAFVNQHLCGSHLVEALYLVCGERGFFYTPKTR",
                "Mouse" to "MALWMRFLPLLALLALWEPKPAQAFVKQHLCGSHLVEALYLVCGERGFFYTPMSR",
                "Zebrafish" to "MASWLRLLPLLALLVLWEPMPAQAFVQQHLCGSHLVDALYLVCGEKGFFYNPKTL"
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BioKt Phylogenetic Reconstruction (UPGMA / NJ)", fontWeight = FontWeight.Bold, color = HighDensityNavy)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Calculated Evolutionary Newick Tree:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HighDensityPeriwinkle.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Text(
                        text = sampleTree.toNewick() + ";",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = HighDensityNavy,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Evolutionary Node Structure:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("├─ Human / Chimp (Branch length: 0.0000 - 100% Identical)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("├─ Mouse Divergence (Branch length: 0.0769)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("└─ Zebrafish Outgroup (Branch length: 0.1852)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = HighDensityPeriwinkle,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = HighDensityTextSecondary, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HighDensityNavy)
            Text(text = subtitle, fontSize = 10.sp, color = HighDensityTextSecondary)
        }
    }
}

@Composable
fun BioResultField(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = HighDensityTextSecondary, fontWeight = FontWeight.SemiBold)
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = HighDensityCardBg,
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = HighDensityNavy,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
