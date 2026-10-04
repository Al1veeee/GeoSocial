package com.example.geosocial.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.geosocial.presentation.auth.AuthViewModel
import com.example.geosocial.presentation.auth.LoginScreen
import com.example.geosocial.presentation.auth.RegisterScreen
import com.example.geosocial.presentation.createplace.CreatePlaceScreen
import com.example.geosocial.presentation.feed.FeedScreen
import com.example.geosocial.presentation.map.MapScreen
import com.example.geosocial.presentation.place.PlaceDetailScreen
import com.example.geosocial.presentation.profile.ProfileScreen

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    when (isLoggedIn) {
        // состояние авторизации ещё не известно
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        false -> AuthNavHost(authViewModel)
        true -> MainNavHost()
    }
}

@Composable
private fun AuthNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    val tabs = listOf(
        BottomTab(Routes.FEED, "Лента", Icons.AutoMirrored.Filled.List),
        BottomTab(Routes.MAP, "Карта", Icons.Filled.Map),
        BottomTab(Routes.PROFILE, "Профиль", Icons.Filled.Person)
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = tabs.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.FEED,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.FEED) {
                FeedScreen(onPlaceClick = { navController.navigate(Routes.placeDetail(it)) })
            }
            composable(Routes.MAP) {
                MapScreen(
                    onPlaceClick = { navController.navigate(Routes.placeDetail(it)) },
                    onAddPlace = { lat, lng -> navController.navigate(Routes.createPlace(lat, lng)) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(onPlaceClick = { navController.navigate(Routes.placeDetail(it)) })
            }
            composable(
                route = Routes.PLACE_DETAIL,
                arguments = listOf(navArgument("placeId") { type = NavType.LongType })
            ) {
                PlaceDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Routes.CREATE_PLACE,
                arguments = listOf(
                    navArgument("lat") { type = NavType.FloatType; defaultValue = 55.7558f },
                    navArgument("lng") { type = NavType.FloatType; defaultValue = 37.6173f }
                )
            ) {
                CreatePlaceScreen(onDone = { navController.popBackStack() })
            }
        }
    }
}
