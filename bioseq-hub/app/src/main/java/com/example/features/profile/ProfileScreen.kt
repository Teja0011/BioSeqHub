package com.example.features.profile

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.example.core.common.FileExporter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.auth.AccountRegistry
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val currentAccount = AccountRegistry.currentActiveAccount

    val displayName = currentAccount?.fullName ?: "Dr. Boddu Teja"
    val displayEmail = currentAccount?.email ?: "bodduteja2021@gmail.com"
    val displayRole = currentAccount?.role ?: "Principal Genomics Investigator"
    val displayInstitution = currentAccount?.institution ?: "Department of Bioinformatics & Genomics"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Researcher Profile", fontWeight = FontWeight.Bold, color = HighDensityNavy) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = HighDensityNavy)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = HighDensityNavy)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(HighDensityPeriwinkle),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BT",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityNavy
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityNavy
                    )

                    Text(
                        text = displayEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HighDensityTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HighDensityPeriwinkle
                    ) {
                        Text(
                            text = "$displayRole • $displayInstitution",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Research Statistics
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Laboratory Analytics & Cache Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityNavy
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(title = "Room Queries", count = "18 Cached")
                        MetricItem(title = "BioKt Sequences", count = "32 Stored")
                        MetricItem(title = "Sheets Tabs", count = "7 Synced")
                    }
                }
            }

            // Actions
            val context = LocalContext.current
            Button(
                onClick = {
                    val archiveData = mapOf(
                        "README.txt" to "BioSeq Hub Research Archive\nGenerated for: ${displayName}\nDate: 2026-09-01\nIncludes SQLite Cache, FASTA sequences, and Google Sheets schema.",
                        "sequences.fasta" to ">P04637 Cellular tumor antigen p53\nMEEPQSDPSVEPPLSQETFSDLWKLLPENNVLSPLPSQAMDDLMLSPDDIEQWFTEDPGP\n>P38398 Breast cancer type 1 susceptibility protein\nMDLSALRVEEVQNVINAMQKILECPICLELIKEPVSTKCDHIFCKFCMLKLLNQKKGPSQ",
                        "database_cache_dump.json" to "{\"sqlite_version\":\"3.39.0\",\"tables\":[\"queries\",\"results\",\"sequences\",\"annotations\",\"orthologs\",\"diseases\"],\"records_cached\":18}",
                        "sheets_export.csv" to "QueryID,Accession,Gene,Organism,Status\nQ001,P04637,TP53,Homo sapiens,SUCCESS\nQ002,P38398,BRCA1,Homo sapiens,SUCCESS"
                    )
                    val fname = "bioseq_archive_${System.currentTimeMillis() / 1000}.zip"
                    statusMessage = FileExporter.exportZipArchive(
                        context = context,
                        zipFileName = fname,
                        fileContents = archiveData
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("export_archive_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HighDensityNavy)
            ) {
                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Research Archive (.zip)", fontWeight = FontWeight.Bold, color = Color.White)
            }

            OutlinedButton(
                onClick = onOpenLogin,
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("profile_login_btn"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityNavy)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = HighDensityNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch Account / Login Portal", fontWeight = FontWeight.Bold, color = HighDensityNavy)
            }

            if (statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HighDensityPeriwinkle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = HighDensityNavy,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    title: String,
    count: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HighDensityNavy
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = HighDensityTextSecondary
        )
    }
}
