package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.core.navigation.Screen
import com.example.features.annotation.AnnotationAggregatorScreen
import com.example.features.auth.GoogleSignInScreen
import com.example.features.auth.LoginScreen
import com.example.features.batch.BatchRetrievalScreen
import com.example.features.biokt.BioKtLaboratoryScreen
import com.example.features.crossref.CrossReferenceScreen
import com.example.features.disease.DiseaseAssociationsScreen
import com.example.features.functional.FunctionalAnnotationScreen
import com.example.features.gemini.GeminiAssistantScreen
import com.example.features.googleapis.GoogleApisScreen
import com.example.features.history.QueryHistoryScreen
import com.example.features.home.HomeScreen
import com.example.features.ncbi.NcbiEntrezScreen
import com.example.features.ortholog.OrthologFinderScreen
import com.example.features.paralog.ParalogDetectorScreen
import com.example.features.profile.ProfileScreen
import com.example.features.room.RoomDatabaseScreen
import com.example.features.search.GlobalSearchScreen
import com.example.features.settings.SettingsScreen
import com.example.features.sheets.SheetsSchemaScreen
import com.example.features.structure.StructureViewerScreen
import com.example.features.uniprot.UniprotSparqlScreen
import com.example.features.visualization.ResultVisualizationScreen
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioSeqHubTheme {
                BioSeqApp()
            }
        }
    }
}

@Composable
fun BioSeqApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home, "nav_home"),
        BottomNavItem("Search", Screen.Search.route, Icons.Default.Search, "nav_search"),
        BottomNavItem("History", Screen.QueryHistory.route, Icons.Default.History, "nav_history"),
        BottomNavItem("Gemini AI", Screen.GeminiAssistant.route, Icons.Default.AutoAwesome, "nav_gemini"),
        BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person, "nav_profile")
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = HighDensityNavBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = HighDensityBorder,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = HighDensityNavy,
                                selectedTextColor = HighDensityNavy,
                                indicatorColor = HighDensityPeriwinkle,
                                unselectedIconColor = HighDensityTextSecondary.copy(alpha = 0.8f),
                                unselectedTextColor = HighDensityTextSecondary.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.testTag(item.tag),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Main Top Tabs
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    onOpenSearch = { navController.navigate(Screen.Search.route) },
                    onOpenProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Search.route) {
                GlobalSearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoute = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.QueryHistory.route) {
                QueryHistoryScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onReplayQuery = { db, query ->
                        if (db.contains("NCBI", ignoreCase = true)) {
                            navController.navigate(Screen.NcbiEntrez.route)
                        } else {
                            navController.navigate(Screen.UniprotSparql.route)
                        }
                    }
                )
            }
            composable(Screen.GeminiAssistant.route) {
                GeminiAssistantScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRoute = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenSettings = { navController.navigate(Screen.Settings.route) },
                    onOpenLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Feature 1: Google Sign-In
            composable(Screen.GoogleSignIn.route) {
                GoogleSignInScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 2: Sheets Schema
            composable(Screen.SheetsSchema.route) {
                SheetsSchemaScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 3: Room Database
            composable(Screen.RoomDatabase.route) {
                RoomDatabaseScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 4: Google APIs
            composable(Screen.GoogleApis.route) {
                GoogleApisScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 5: NCBI Entrez
            composable(Screen.NcbiEntrez.route) {
                NcbiEntrezScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 6: UniProt SPARQL
            composable(Screen.UniprotSparql.route) {
                UniprotSparqlScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 7: Batch Sequence Retrieval
            composable(Screen.BatchRetrieval.route) {
                BatchRetrievalScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 8: Annotation Aggregator
            composable(Screen.AnnotationAggregator.route) {
                AnnotationAggregatorScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 9: Ortholog Finder
            composable(Screen.OrthologFinder.route) {
                OrthologFinderScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 10: Paralog Detector
            composable(Screen.ParalogDetector.route) {
                ParalogDetectorScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 11: Functional Annotation
            composable(Screen.FunctionalAnnotation.route) {
                FunctionalAnnotationScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 12: Disease Associations
            composable(Screen.DiseaseAssociations.route) {
                DiseaseAssociationsScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 13: Cross-Referencing
            composable(Screen.CrossReference.route) {
                CrossReferenceScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 14: Result Visualization
            composable(Screen.ResultVisualization.route) {
                ResultVisualizationScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 16: BioKt Bioinformatics Suite
            composable(Screen.BioKtLaboratory.route) {
                BioKtLaboratoryScreen(onNavigateBack = { navController.popBackStack() })
            }

            // Feature 17: 3D Structure Viewer
            composable(Screen.Structure3D.route) {
                StructureViewerScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String
)
