package com.example.p3

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
        setContent {
            val users: UserViewModel = viewModel()
            val isDarkMode by users.isDarkMode.collectAsState()
            AppTheme(darkTheme = isDarkMode) {
                EvoriaApp(users)
            }
        }
    }
}

@Composable
private fun EvoriaApp(users: UserViewModel) {
    val navController = rememberNavController()
    val events: EventViewModel = viewModel()
    val user by users.currentUser.collectAsState()
    val onboardingDone by users.isOnboardingCompleted.collectAsState()
    val sessionChecked by users.sessionChecked.collectAsState()

    if (!sessionChecked) {
        // Pantalla de carga mientras se restaura la sesión
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val initialStartDestination = remember {
            when {
                user != null -> "home"
                onboardingDone -> "login"
                else -> "onboarding"
            }
        }

        NavHost(
            navController,
            startDestination = initialStartDestination,
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onRegister = {
                        users.completeOnboarding()
                        navController.navigate("register") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    },
                    onLogin = {
                        users.completeOnboarding()
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("login") { LoginScreen(navController, users) }
            composable("register") { RegisterScreen(navController, users) }
            composable("home") {
                user?.let { current ->
                    AppScaffold(current, users, navController) {
                        EventHomeScreen(events, users, navController)
                    }
                }
            }
            composable("event_search") {
                EventSearchScreen(events, navController)
            }
            composable("my_events") {
                user?.let { current ->
                    AppScaffold(current, users, navController) {
                        MyEventsScreen(current, events, navController)
                    }
                }
            }
            composable("profile") {
                user?.let { current ->
                    AppScaffold(current, users, navController) {
                        ProfileScreen(current, users, navController)
                    }
                }
            }
            composable(
                "event_detail/{eventId}",
                listOf(navArgument("eventId") { type = NavType.StringType })
            ) { it ->
                user?.let { current ->
                    val allUsers by users.users.collectAsState()
                    EventDetailScreen(
                        eventId = Uri.decode(requireNotNull(it.arguments?.getString("eventId"))),
                        user = current,
                        viewModel = events,
                        users = allUsers,
                        navController = navController,
                    )
                }
            }
            composable("event_form") {
                user?.let { current ->
                    EventFormScreen(null, current, events, navController)
                }
            }
            composable(
                "event_form/{eventId}",
                listOf(navArgument("eventId") { type = NavType.StringType })
            ) { it ->
                user?.let { current ->
                    EventFormScreen(
                        requireNotNull(it.arguments?.getString("eventId")),
                        current,
                        events,
                        navController
                    )
                }
            }
        }
    }
}

@Composable
private fun AppScaffold(user: User, userViewModel: UserViewModel, navController: androidx.navigation.NavHostController, content: @Composable () -> Unit) {
    val tabs = listOf("home" to "Inicio", "my_events" to "Mis eventos", "profile" to "Perfil")
    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val destination = backStack?.destination
                tabs.forEach { (route, label) ->
                    val icon = when (route) {
                        "home" -> Icons.Default.Home
                        "my_events" -> Icons.Default.CalendarMonth
                        else -> Icons.Default.AccountCircle
                    }
                    NavigationBarItem(
                        selected = destination?.hierarchy?.any { it.route == route } == true,
                        onClick = { navController.navigate(route) { launchSingleTop = true } },
                        icon = {
                            if (route == "profile" && !user.avatar.isNullOrBlank()) {
                                AsyncImage(
                                    user.avatar,
                                    "Foto de perfil",
                                    Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Icon(icon, label)
                            }
                        },
                        label = { Text(label) },
                    )
                }
            }
        }
    ) { padding ->
        androidx.compose.foundation.layout.Box(Modifier.padding(padding)) {
            content()
        }
    }
}
