package com.starcode.locus.ui.screens

import android.app.Application
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
import com.starcode.locus.ui.viewmodels.*

@Composable
fun NavGraph(navController: NavHostController, dao: LocusDao) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val sessionManager = remember { SessionManager(application) }

    // Lógica de inicio de sesión y validación de edad
    val estaLogueado = sessionManager.obtenerToken() != null
    val edadYaValidada = sessionManager.esEdadValidada()

    val startDest = when {
        estaLogueado -> "mapa"
        edadYaValidada -> "registro"
        else -> "welcome"
    }

    // --- VIEWMODELS CON FACTORIES ---

    // AuthViewModel (Necesita Application y DAO)
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(application, dao) as T
            }
        }
    )

    // MapaViewModel (Necesita Application y DAO)
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
                onNavigateToFavoritos = { navController.navigate("favoritos") },
                onNavigateToEstadisticas = { navController.navigate("estadisticas") }
            )
        }

        composable("favoritos") {
            FavoritosScreen(
                viewModel = mapaViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("estadisticas") {
            val estadisticasViewModel: EstadisticasViewModel = viewModel(
                factory = GenericViewModelFactory { EstadisticasViewModel(sessionManager) }
            )
            EstadisticasScreen(
                viewModel = estadisticasViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("perfil") {
            PerfilScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogoutNavigation = {
                    navController.navigate("login") {
                        popUpTo("mapa") { inclusive = true }
                    }
                }
            )
        }

        composable("recuerdos") {
            val recuerdosViewModel: RecuerdosViewModel = viewModel(
                factory = GenericViewModelFactory { RecuerdosViewModel(sessionManager) }
            )
            RecuerdosScreen(
                viewModel = recuerdosViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Una fábrica genérica para simplificar la creación de ViewModels que solo
 * necesitan el SessionManager o dependencias simples.
 */
class GenericViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}