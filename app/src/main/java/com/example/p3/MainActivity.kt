package com.example.p3

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.p3.data.model.User
import com.example.p3.ui.screens.*
import com.example.p3.ui.theme.AppTheme
import com.example.p3.ui.viewmodel.EventViewModel
import com.example.p3.ui.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { EvoriaApp() } }
    }
}

@Composable
private fun EvoriaApp() {
    val navController = rememberNavController()
    val users: UserViewModel = viewModel()
    val events: EventViewModel = viewModel()
    val user by users.currentUser.collectAsState()
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController, users) }
        composable("home") {
            user?.let { AppScaffold(navController) { EventHomeScreen(events, navController) } }
        }
        composable("my_events") {
            user?.let { current -> AppScaffold(navController) { MyEventsScreen(current, events, navController) } }
        }
        composable("profile") {
            user?.let { current -> AppScaffold(navController) { ProfileScreen(current, users, navController) } }
        }
        composable("event_detail/{eventId}", listOf(navArgument("eventId") { type = NavType.StringType })) {
            user?.let { current ->
                EventDetailScreen(
                    Uri.decode(requireNotNull(it.arguments?.getString("eventId"))),
                    current,
                    events,
                    navController,
                )
            }
        }
        composable("event_form") { user?.let { current -> EventFormScreen(null, current, events, navController) } }
        composable("event_form/{eventId}", listOf(navArgument("eventId") { type = NavType.StringType })) {
            user?.let { current -> EventFormScreen(requireNotNull(it.arguments?.getString("eventId")), current, events, navController) }
        }
    }
}

@Composable
private fun AppScaffold(navController: androidx.navigation.NavHostController, content: @Composable () -> Unit) {
    val tabs = listOf("home" to "Inicio", "my_events" to "Mis eventos", "profile" to "Perfil")
    Scaffold(bottomBar = {
        NavigationBar {
            val backStack by navController.currentBackStackEntryAsState()
            val destination = backStack?.destination
            tabs.forEach { (route, label) ->
                val icon = when (route) { "home" -> Icons.Default.Home; "my_events" -> Icons.Default.CalendarMonth; else -> Icons.Default.AccountCircle }
                NavigationBarItem(
                    selected = destination?.hierarchy?.any { it.route == route } == true,
                    onClick = { navController.navigate(route) { launchSingleTop = true } },
                    icon = { Icon(icon, label) }, label = { Text(label) }
                )
            }
        }
    }) { padding -> androidx.compose.foundation.layout.Box(Modifier.padding(padding)) { content() } }
}
