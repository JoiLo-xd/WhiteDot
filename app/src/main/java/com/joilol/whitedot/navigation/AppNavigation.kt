package com.joilol.whitedot.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.PopUpToBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joilol.whitedot.view.ScreenDot
import com.joilol.whitedot.view.ScreenLogin
import com.joilol.whitedot.view.ScreenStats
import com.joilol.whitedot.view.ScreenWelcome
import com.joilol.whitedot.viewmodel.DotViewModel
import com.joilol.whitedot.viewmodel.LoginViewModel
import com.joilol.whitedot.viewmodel.StatsViewModel

// FUNCIONS AUXILIARS
fun setInclusiveTrue(builder: PopUpToBuilder) {
    builder.inclusive = true
}

fun configurarPopUpLogin(builder: NavOptionsBuilder) {
    builder.popUpTo(AppScreens.Login.route, ::setInclusiveTrue)
}

fun configurarArgUsername(builder: androidx.navigation.NavArgumentBuilder) {
    builder.type = NavType.StringType
}

@Composable
fun AppNavigation(
    onCloseApp: () -> Unit
){
    val navController = rememberNavController()

    // Funcions de soporte
    fun ferLogout() = navController.navigate(AppScreens.Login.route, ::configurarPopUpLogin)
    fun anarASimon() = navController.navigate(AppScreens.Simon.route)

    NavHost(
        navController = navController,
        startDestination = AppScreens.Login.route
    ){
        // RUTA 1: LOGIN
        composable(route = AppScreens.Login.route) {
            val viewModel: LoginViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            // Lanzamos la navegación cuando el estado cambie a WELCOME
            LaunchedEffect(state.screenState) {
                if (state.screenState == com.joilol.whitedot.viewmodel.AppScreens.WELCOME) {
                    // USAMOS TU FUNCIÓN createRoute para evitar errores de texto
                    navController.navigate(AppScreens.Welcome.createRoute(state.username)) {
                        popUpTo(AppScreens.Login.route) { inclusive = true }
                    }
                }
            }

            ScreenLogin(
                state = state,
                onUsernameChange = viewModel::onUsernameChange,
                onPasswordChange = viewModel::onPasswordChange,
                onRegisterClick = viewModel::onRegisterClick,
                onLoginClick = viewModel::onLoginClick,
                onCloseClick = onCloseApp
            )
        }

        // RUTA 2: WELCOME
        composable(
            route = AppScreens.Welcome.route, // Esto es "welcome_screen/{username}"
            arguments = listOf(navArgument("username", ::configurarArgUsername))
        ){ backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Desconegut"

            ScreenWelcome(
                msgWelcome = "Hola, $username",
                onLogoutClick = ::ferLogout,
                onCloseClick = onCloseApp,
                onStartClick = ::anarASimon
            )
        }

        // RUTA 3: El juego del Dot
        composable(route = AppScreens.Simon.route) {
            val dotVM: DotViewModel = viewModel()
            val dotState by dotVM.uiState.collectAsState()

            ScreenDot(
                state = dotState,
                onDotClick = { dotVM.incrementCount() },
                onAutoClickClick = { dotVM.toggleAutoClick() },
                onStatsClick = { navController.navigate(AppScreens.Stats.route) }, // Navegamos a Stats
                onBack = { navController.popBackStack() }
            )
        }




// RUTA 4: Las stats
        composable(route = AppScreens.Stats.route) {
            val statsVM: StatsViewModel = viewModel()
            val statsState by statsVM.uiState.collectAsState()

            LaunchedEffect(Unit) { statsVM.loadStats() }

            ScreenStats(
                state = statsState,
                onBack = { navController.popBackStack() }
            )
        }
    }
}