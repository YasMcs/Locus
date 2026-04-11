package com.starcode.locus.ui.screens

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.ui.screens.*
import com.starcode.locus.ui.viewmodels.AuthViewModel
import com.starcode.locus.ui.viewmodels.AuthResult
import com.starcode.locus.ui.viewmodels.EdadViewModel
import com.starcode.locus.ui.viewmodels.MapaViewModel

@Composable
fun NavGraph(navController: NavHostController, dao: LocusDao) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val sessionManager = remember { SessionManager(application) }

    val estaLogueado = sessionManager.obtenerToken() != null
    val edadYaValidada = sessionManager.esEdadValidada()

    val startDest = when {
        estaLogueado -> "mapa"
        edadYaValidada -> "registro"
        else -> "welcome"
    }

    // ViewModels principales
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(application, dao) as T
            }
        }
    )

    // Declaramos MapaViewModel aquí afuera para que "mapa" y "favoritos" lo compartan
    val mapaViewModel: MapaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapaViewModel(application, dao) as T
            }
        }
    )

    val edadViewModel: EdadViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = startDest) {

        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegistro = { navController.navigate("validar_edad") }
            )
        }

        composable("validar_edad") {
            EdadScreen(
                viewModel = edadViewModel,
                onEdadValida = {
                    sessionManager.guardarEdadValidada(true)
                    navController.navigate("registro") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable(route = "registro") {
            val fechaParaDB by edadViewModel.fechaParaDB.collectAsState()
            val authState by authViewModel.authState.collectAsState()

            RegistroScreen(
                viewModel = authViewModel,
                onIrALogin = { navController.navigate("login") },
                fechaValidada = fechaParaDB
            )

            LaunchedEffect(authState) {
                if (authState is AuthResult.Success) {
                    navController.navigate("mapa") {
                        popUpTo("registro") { inclusive = true }
                    }
                    authViewModel.resetAuthState()
                }
            }
        }

        composable("login") {
            LoginScreen(
                onLogin = { email, pass -> authViewModel.login(email, pass) },
                onIrARegistrar = { navController.navigate("validar_edad") },
                authState = authState
            )

            LaunchedEffect(authState) {
                if (authState is AuthResult.Success) {
                    navController.navigate("mapa") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.resetAuthState()
                }
            }
        }

        composable("mapa") {
            MapaScreen(
                viewModel = mapaViewModel,
                sessionManager = sessionManager,
                onNavigateToPerfil = { navController.navigate("perfil") },
                onNavigateToRecuerdos = { navController.navigate("recuerdos") },
                onNavigateToFavoritos = { navController.navigate("favoritos") }
            )
        }

        composable("favoritos") {
            FavoritosScreen(
                viewModel = mapaViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("perfil") {
            PerfilScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogoutNavigation = {
                    navController.navigate("login") {
                        popUpTo("mapa") { inclusive = true } // Cambiado a mapa para limpiar todo
                    }
                }
            )
        }

        composable("recuerdos") {
            val recuerdosViewModel: com.starcode.locus.ui.viewmodels.RecuerdosViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return com.starcode.locus.ui.viewmodels.RecuerdosViewModel(sessionManager) as T
                    }
                }
            )

            RecuerdosScreen(
                viewModel = recuerdosViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}