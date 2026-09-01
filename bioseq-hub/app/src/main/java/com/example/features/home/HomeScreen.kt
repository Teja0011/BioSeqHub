package com.example.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.FeatureCardItem
import com.example.core.navigation.FeatureRegistry
import com.example.core.navigation.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRoute: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mandatoryFeatures = FeatureRegistry.mandatoryFacultyFeatures
    val additionalFeatures = FeatureRegistry.additionalFeatures

    Scaffold(
        topBar = {
            Surface(
                color = HighDensityCanvas,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Home",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Normal,
                                color = HighDensityTextPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(HighDensityStatusGreen)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "All systems active",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = HighDensityTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = onOpenSearch,
                                modifier = Modifier.testTag("home_search_action")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = HighDensityNavy
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(HighDensityPeriwinkle)
                                    .border(1.dp, HighDensityAccentBlue, CircleShape)
                                    .clickable { onOpenProfile() }
                                    .testTag("home_profile_action"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "BT",
                                    fontWeight = FontWeight.SemiBold,
                                    color = HighDensityNavy,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = HighDensityCanvas,
        modifier = modifier
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 4.dp,
                bottom = 96.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. High Density Hero Consumption / Pipeline Card
            item(span = { GridItemSpan(2) }) {
                HighDensityHeroBanner(
                    onGeminiClick = { onNavigateToRoute(Screen.GeminiAssistant.route) },
                    onStructureClick = { onNavigateToRoute(Screen.Structure3D.route) }
                )
            }

            // 2. High Density Search Bar
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = HighDensityCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onOpenSearch() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search icon",
                            tint = HighDensityNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search genes, proteins, SPARQL, diseases...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HighDensityTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 3. Faculty Required Features Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RESEARCH MODULES",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "15 Mandatory Capabilities",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HighDensityTextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = HighDensityPeriwinkle
                    ) {
                        Text(
                            text = "15 OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = HighDensityNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 4. The 15 Mandatory Faculty Cards (High Density 2-Column Grid)
            items(mandatoryFeatures, key = { it.id }) { feature ->
                // Apply High Density card styling: some contrast cards in HighDensityNavy for visual hierarchy
                val isContrastCard = feature.id == "feat_uniprot" || feature.id == "feat_ortholog"
                HighDensityFeatureCard(
                    feature = feature,
                    isContrast = isContrastCard,
                    onClick = { onNavigateToRoute(feature.route) }
                )
            }

            // 5. Additional Key Capabilities Header
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                    Text(
                        text = "ADVANCED ENGINES",
                        style = MaterialTheme.typography.labelSmall,
                        color = HighDensityTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "AI & Structural Modeling",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityTextPrimary
                    )
                }
            }

            // 6. Additional Feature Cards (Gemini & 3D Structure)
            items(additionalFeatures, key = { it.id }) { feature ->
                val isContrast = feature.id == "feat_gemini"
                HighDensityFeatureCard(
                    feature = feature,
                    isContrast = isContrast,
                    onClick = { onNavigateToRoute(feature.route) }
                )
            }

            // 7. High Density Recent Activity Card
            item(span = { GridItemSpan(2) }) {
                HighDensityRecentActivitySection(
                    onSeeAll = { onNavigateToRoute(Screen.QueryHistory.route) }
                )
            }
        }
    }
}

@Composable
fun HighDensityHeroBanner(
    onGeminiClick: () -> Unit,
    onStructureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = HighDensityPeriwinkle,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Top row: Energy/Pipeline Load + ECO MODE pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "RESEARCH PIPELINE CONSUMPTION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HighDensityNavy,
                        letterSpacing = 0.8.sp,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "2.48",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Light,
                            color = HighDensityNavy,
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kReq • 15 ENGINES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = HighDensityNavy.copy(alpha = 0.75f),
                            modifier = Modifier.padding(bottom = 6.dp),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = HighDensityNavy,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "ECO MODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // High Density Spark-Bars Chart
            val barFractions = listOf(0.30f, 0.45f, 0.25f, 0.60f, 0.85f, 1.00f, 0.70f, 0.40f, 0.55f, 0.35f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                barFractions.forEachIndexed { index, fraction ->
                    val isPeak = index == 5
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(
                                if (isPeak) HighDensityNavy else HighDensityNavy.copy(alpha = if (index == 6) 0.35f else 0.15f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick launch buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onGeminiClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HighDensityNavy,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🤖 Gemini AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onStructureClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = HighDensityNavy
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(HighDensityNavy.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🧬 3D Viewer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HighDensityFeatureCard(
    feature: FeatureCardItem,
    isContrast: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isContrast) HighDensityNavy else HighDensityCardBg
    val iconBg = if (isContrast) HighDensityPeriwinkle else HighDensityAccentBlue
    val titleColor = if (isContrast) Color.White else HighDensityTextPrimary
    val subColor = if (isContrast) Color.White.copy(alpha = 0.75f) else HighDensityTextSecondary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("feature_card_${feature.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = feature.emoji,
                    fontSize = 20.sp
                )
            }

            Column {
                Text(
                    text = feature.title.substringAfter(". ").trim(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${feature.tag} • Active",
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HighDensityRecentActivitySection(
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ACTIVITY",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityTextSecondary,
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                )
                Text(
                    text = "SEE ALL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HighDensityNavy,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable { onSeeAll() }
                )
            }

            val activities = listOf(
                Triple("🔔", "NCBI TP53 sequence retrieved & cached", "Entrez Nucleotide • 14:02"),
                Triple("🔋", "UniProt SPARQL kinase query synchronized", "Room Local Storage • 13:45"),
                Triple("⚠️", "ClinVar Li-Fraumeni pathogenic variant flagged", "DisGeNET Node • 12:30"),
                Triple("🧬", "Needleman-Wunsch pairwise alignment completed", "Ortholog Engine • 11:15")
            )

            activities.forEachIndexed { index, (emoji, title, subtitle) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when (index) {
                                    0 -> HighDensityPeriwinkle
                                    1 -> HighDensityCardBg
                                    2 -> Color(0xFFFFEDD5)
                                    else -> HighDensityAccentBlue
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = HighDensityTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = HighDensityTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
