package com.example.p3

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.p3.data.image.fastImageUrl
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
            composable("notifications") {
                user?.let { current ->
                    NotificationsScreen(current, users, events, navController)
                }
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
                "public_profile/{userId}",
                listOf(navArgument("userId") { type = NavType.StringType }),
            ) { entry ->
                val allUsers by users.users.collectAsState()
                val profile = allUsers.firstOrNull {
                    it.id == entry.arguments?.getString("userId")
                }
                PublicProfileScreen(profile, navController)
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
    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
            ) {
                val backStack by navController.currentBackStackEntryAsState()
                val destination = backStack?.destination
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                BottomBarItem(
                    route = "home",
                    label = "Inicio",
                    icon = Icons.Default.Home,
                    destination = destination,
                    onClick = { navController.navigate("home") { launchSingleTop = true } },
                )
                BottomBarItem(
                    route = "event_search",
                    label = "Explorar",
                    icon = Icons.Default.Search,
                    destination = destination,
                    onClick = { navController.navigate("event_search") { launchSingleTop = true } },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { navController.navigate("event_form") },
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = Color(0xFF244B68),
                        shadowElevation = 5.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, "Crear evento", tint = Color.White)
                        }
                    }
                }
                BottomBarItem(
                    route = "my_events",
                    label = "Mis eventos",
                    icon = Icons.Default.CalendarMonth,
                    destination = destination,
                    onClick = { navController.navigate("my_events") { launchSingleTop = true } },
                )
                BottomBarItem(
                    route = "profile",
                    label = "Perfil",
                    icon = Icons.Default.AccountCircle,
                    destination = destination,
                    onClick = { navController.navigate("profile") { launchSingleTop = true } },
                    customIcon = {
                        if (!user.avatar.isNullOrBlank()) {
                            AsyncImage(
                                user.avatar.fastImageUrl(),
                                "Foto de perfil",
                                Modifier
                                    .size(26.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, "Perfil")
                        }
                    },
                )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            content()
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    route: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    destination: androidx.navigation.NavDestination?,
    onClick: () -> Unit,
    customIcon: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (customIcon != null) customIcon() else Icon(icon, label)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (destination?.hierarchy?.any { it.route == route } == true) {
                Color(0xFF244B68)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
