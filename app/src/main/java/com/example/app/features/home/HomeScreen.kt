package com.example.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.example.app.ui.components.BottomNavDestination
import com.example.app.ui.components.NavigationBar


/*
 * ================================================================
 * HOME SCREEN
 * ================================================================
 */

class HomeScreen : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.current

        HomeContent(
            onNavigate = { destination ->
                when (destination) {
                    BottomNavDestination.Home -> Unit
                    BottomNavDestination.Stats -> {
                        // TODO: navigator?.replace(StatsScreen())
                    }
                    BottomNavDestination.Notifications -> {
                        // TODO: navigator?.replace(NotificationsScreen())
                    }
                }
            }
        )
    }
}

/*
 * ================================================================
 * HOME CONTENT
 * ================================================================
 */
@Composable
fun HomeContent(
    onNavigate: (BottomNavDestination) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        /*
         * ============================================================
         * BARRA DE NAVEGACIÓN
         * ============================================================
         */

        NavigationBar(
            selectedDestination = BottomNavDestination.Home,
            onDestinationSelected = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}