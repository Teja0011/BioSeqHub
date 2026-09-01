package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun ProvenanceBadge(
    sourceName: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (sourceName.uppercase()) {
        "UNIPROT", "UNIPROTKB" -> BadgeUniProt to Color.White
        "NCBI", "NCBI ENTREZ", "NCBI GENE" -> BadgeNCBI to Color.White
        "INTERPRO" -> BadgeInterPro to Color.White
        "PFAM" -> BadgePfam to Color.White
        "PUBCHEM" -> BadgePubChem to Color.White
        "DISGENET", "OMIM", "CLINVAR" -> BadgeDisGeNET to Color.White
        "GEMINI", "AI-GENERATED" -> BadgeGemini to Color.White
        else -> HighDensityNavy to Color.White
    }

    Surface(
        modifier = modifier.clip(RoundedCornerShape(6.dp)),
        color = HighDensityCardBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(bgColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "[$sourceName]",
                style = MaterialTheme.typography.labelSmall,
                color = HighDensityNavy,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = HighDensityTextPrimary,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = HighDensityTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AiDisclaimerBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = HighDensityPeriwinkle
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "AI Disclaimer",
                tint = HighDensityNavy,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "AI-synthesized bioinformatics summary • Verified via live NCBI/UniProt databases",
                style = MaterialTheme.typography.bodySmall,
                color = HighDensityNavy,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun LoadingState(
    message: String = "Accessing bioinformatics database...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = HighDensityNavy,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = HighDensityTextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFFFEF2F2),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Query Notification",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7F1D1D),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Retry Query", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

