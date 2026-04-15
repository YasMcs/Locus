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
import kotlinx.coroutines.delay

@Composable
fun NavGraph(navController: NavHostController, dao: LocusDao) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val sessionManager = remember { SessionManager(application) }

    // ✅ ESTADO REACTIVO: Observamos el token
    // Usamos un remember que se actualiza para detectar cuando el token sea nulo
    var estaLogueado by remember { mutableStateOf(sessionManager.obtenerToken() != null) }

    // ✅ EFECTO DE MONITOREO: Si el Interceptor borra el token, esto lo detecta
    LaunchedEffect(Unit) {
        while(true) {
            val tokenActual = sessionManager.obtenerToken() != null
            if (estaLogueado != tokenActual) {
                estaLogueado = tokenActual
                if (!estaLogueado) {
                    // Si detectamos que ya no está logueado, mandamos a Login
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // Borra todo el historial
                    }
                }
            }
            delay(1000) // Revisa cada segundo de forma eficiente
        }
    }

    val edadYaValidada = sessionManager.esEdadValidada()

    // Decisión inicial de ruta
    val startDest = remember {
        when {
            estaLogueado -> "mapa"
            edadYaValidada -> "registro"
            else -> "welcome"
        }
    }

    // --- VIEWMODELS CON FACTORIES ---
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(application, dao) as T
            }
        }
    )

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
                    estaLogueado = true // Actualizamos estado local
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
                    estaLogueado = true // Actualizamos estado local
                    navController.navigate("mapa") {
                        popUpTo(0) { inclusive = true }
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
                    sessionManager.cerrarSesion()
                    estaLogueado = false
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
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

class GenericViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return creator() as T
    }
}