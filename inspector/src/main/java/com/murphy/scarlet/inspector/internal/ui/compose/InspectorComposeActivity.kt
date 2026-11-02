package com.murphy.scarlet.inspector.internal.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.murphy.scarlet.inspector.internal.InspectorNotificationManager
import com.murphy.scarlet.inspector.internal.ui.compose.screen.SocketDetailScreen
import com.murphy.scarlet.inspector.internal.ui.compose.screen.SocketListScreen

class InspectorComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "socket_list") {
                        composable(
                            route = "socket_list",
                            enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it }) },
                            exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it }) }
                        ) {
                            SocketListScreen(
                                onNavigateToDetail = { type ->
                                    navController.navigate("socket_detail/$type")
                                }
                            )
                        }
                        composable(
                            route = "socket_detail/{type}",
                            enterTransition = { androidx.compose.animation.slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { androidx.compose.animation.slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val type = backStackEntry.arguments?.getString("type") ?: "DEFAULT"
                            SocketDetailScreen(
                                type = type,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Hide notification when Inspector is visible
        InspectorNotificationManager.dismissNotification(this)
    }

    override fun onStop() {
        super.onStop()
        // Show notification again when Inspector is hidden/destroyed
        InspectorNotificationManager.showNotification(this)
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, InspectorComposeActivity::class.java)
    }
}
