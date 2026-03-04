package uk.ac.tees.mad.exitnote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import uk.ac.tees.mad.exitnote.ui.screens.auth.AuthScreen
import uk.ac.tees.mad.exitnote.ui.screens.home.HomeScreen
import uk.ac.tees.mad.exitnote.ui.screens.settings.SettingsScreen
import uk.ac.tees.mad.exitnote.ui.screens.setup.SetupLocationScreen
import uk.ac.tees.mad.exitnote.ui.screens.splash.SplashScreen
import uk.ac.tees.mad.exitnote.viewmodel.ExitNoteViewModel


@Composable
fun ExitNoteNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH
) {
    // Single ViewModel instance shared across all screens
    val viewModel: ExitNoteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(route = Routes.SPLASH) {
            SplashScreen(
                viewModel = viewModel,
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToSetup = { userEmail ->
                    navController.navigate("${Routes.SETUP_LOCATION}?${NavArgs.USER_EMAIL}=$userEmail") {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Auth Screen
        composable(route = Routes.AUTH) {
            AuthScreen(
                viewModel = viewModel,
                onNavigateToSetup = { userEmail ->
                    navController.navigate("${Routes.SETUP_LOCATION}?${NavArgs.USER_EMAIL}=$userEmail") {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // Setup Location Screen with optional arguments
        composable(
            route = "${Routes.SETUP_LOCATION}?${NavArgs.USER_EMAIL}={${NavArgs.USER_EMAIL}}",
            arguments = listOf(
                navArgument(NavArgs.USER_EMAIL) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userEmail = backStackEntry.arguments?.getString(NavArgs.USER_EMAIL)
            SetupLocationScreen(
                viewModel = viewModel,
                userEmail = userEmail,
                onSetupComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SETUP_LOCATION) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(route = Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToSetup = {
                    navController.navigate(Routes.SETUP_LOCATION)
                }
            )
        }

        // Settings Screen
        composable(route = Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * Navigation actions to be used by screens
 */
class NavigationActions(navController: NavHostController) {
    val navigateToAuth: () -> Unit = {
        navController.navigate(Routes.AUTH) {
            popUpTo(0) { inclusive = true }
        }
    }

    val navigateToHome: () -> Unit = {
        navController.navigate(Routes.HOME) {
            popUpTo(0) { inclusive = true }
        }
    }

    val navigateToSetup: (String?) -> Unit = { userEmail ->
        val route = if (userEmail != null) {
            "${Routes.SETUP_LOCATION}?${NavArgs.USER_EMAIL}=$userEmail"
        } else {
            Routes.SETUP_LOCATION
        }
        navController.navigate(route)
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(Routes.SETTINGS)
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

@Composable
fun rememberNavigationActions(navController: NavHostController): NavigationActions {
    return remember(navController) {
        NavigationActions(navController)
    }
}