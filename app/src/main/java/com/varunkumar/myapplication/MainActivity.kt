package com.varunkumar.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.varunkumar.myapplication.data.DatabaseHelper
import com.varunkumar.myapplication.ui.screens.KeystrokeScreen
import com.varunkumar.myapplication.ui.screens.TouchScreen
import com.varunkumar.myapplication.ui.screens.TouchTaskMode
import com.varunkumar.myapplication.ui.screens.TracingPattern
import com.varunkumar.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val dbHelper = remember { DatabaseHelper(context) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val steps = listOf(
                    EnrollmentStep.TracingSCurve,
                    EnrollmentStep.TracingCircle,
                    EnrollmentStep.Tapping,
                    EnrollmentStep.Keystroke,
                    EnrollmentStep.Completion,
                )

                val currentStepIndex = steps.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
                val progressValue = if (currentRoute == EnrollmentStep.Completion.route) 1f else currentStepIndex.toFloat() / (steps.size - 1)

                Scaffold(
                    topBar = {
                        Column {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text("Enrollment Pipeline")
                                },
                            )
                            if (currentRoute != EnrollmentStep.Completion.route) {
                                LinearProgressIndicator(
                                    progress = { progressValue },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Text(
                                    text = "Step ${currentStepIndex + 1} of ${steps.size - 1}: ${steps[currentStepIndex].label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    },
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = EnrollmentStep.TracingSCurve.route,
                        modifier = Modifier.padding(paddingValues),
                    ) {
                        composable(EnrollmentStep.TracingSCurve.route) {
                            TouchScreen(
                                dbHelper = dbHelper,
                                mode = TouchTaskMode.TRACING,
                                pattern = TracingPattern.S_CURVE,
                            ) { navController.navigate(EnrollmentStep.TracingCircle.route) }
                        }
                        composable(EnrollmentStep.TracingCircle.route) {
                            TouchScreen(
                                dbHelper = dbHelper,
                                mode = TouchTaskMode.TRACING,
                                pattern = TracingPattern.CIRCLE,
                            ) { navController.navigate(EnrollmentStep.Tapping.route) }
                        }
                        composable(EnrollmentStep.Tapping.route) {
                            TouchScreen(
                                dbHelper = dbHelper,
                                mode = TouchTaskMode.TAPPING,
                                pattern = TracingPattern.S_CURVE, // Pattern ignored in tapping mode
                            ) { navController.navigate(EnrollmentStep.Keystroke.route) }
                        }
                        composable(EnrollmentStep.Keystroke.route) {
                            KeystrokeScreen(dbHelper = dbHelper) {
                                navController.navigate(EnrollmentStep.Completion.route)
                            }
                        }
                        composable(EnrollmentStep.Completion.route) {
                            CompletionScreen {
                                navController.navigate(EnrollmentStep.TracingSCurve.route) {
                                    popUpTo(EnrollmentStep.TracingSCurve.route) { inclusive = true }
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
fun CompletionScreen(onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(top = 100.dp) // Add some spacing
            .fillMaxWidth(),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Profile Created Successfully!", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Your behavioral biometric profile has been recorded across all tests.", style = MaterialTheme.typography.bodyMedium)
        androidx.compose.material3.Button(onClick = onRestart) {
            Text("Restart Enrollment")
        }
    }
}

sealed class EnrollmentStep(val route: String, val label: String) {
    object TracingSCurve : EnrollmentStep("tracing_s_curve", "Trace S-Curve")
    object TracingCircle : EnrollmentStep("tracing_circle", "Trace Circle")
    object Tapping : EnrollmentStep("tapping", "Target Tapping")
    object Keystroke : EnrollmentStep("keystroke", "Keystroke Profiling")
    object Completion : EnrollmentStep("completion", "Enrollment Complete")
}
