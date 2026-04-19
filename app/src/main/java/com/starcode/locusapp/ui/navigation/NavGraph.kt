package com.starcode.locusapp.ui.navigation

import android.app.Application
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.starcode.locusapp.data.dao.LocusDao
import com.starcode.locusapp.data.remote.SessionManager
import com.starcode.locusapp.ui.screens.*
import com.starcode.locusapp.ui.viewmodels.*
import kotlinx.coroutines.delay

@Composable
fun NavGraph(
    navController: NavHostController, 
    dao: LocusDao,
    mapaViewModel: MapaViewModel // ✅ Recibimos el ViewModel compartido
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val sessionManager = remember { SessionManager(application) }

    var estaLogueado by remember { mutableStateOf(sessionManager.obtenerToken() != null) }

    LaunchedEffect(Unit) {
        while(true) {
            val tokenActual = sessionManager.obtenerToken() != null
            if (estaLogueado != tokenActual) {
                estaLogueado = tokenActual
                if (!estaLogueado) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            delay(1000)
        }
    }

    val edadYaValidada = sessionManager.esEdadValidada()

    val startDest = remember {
        when {
            estaLogueado -> "mapa"
            edadYaValidada -> "registro"
            else -> "welcome"
        }
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(application, dao) as T
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
                    estaLogueado = true
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
                    estaLogueado = true
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
                onNavigateToMapa = { navController.navigate("mapa") },
                onNavigateToRecuerdos = { navController.navigate("recuerdos") },
                onNavigateToPerfil = { navController.navigate("perfil") },
                onNavigateToEstadisticas = { navController.navigate("estadisticas") }
            )
        }

        composable("estadisticas") {
            val estadisticasViewModel: EstadisticasViewModel = viewModel(
                factory = GenericViewModelFactory { EstadisticasViewModel(sessionManager) }
            )
            EstadisticasScreen(
                viewModel = estadisticasViewModel,
                mapaViewModel = mapaViewModel, // ✅ Pasamos el compartido para la barra superior
                onNavigateToMapa = { navController.navigate("mapa") },
                onNavigateToRecuerdos = { navController.navigate("recuerdos") },
                onNavigateToFavoritos = { navController.navigate("favoritos") },
                onNavigateToPerfil = { navController.navigate("perfil") }
            )
        }

        composable("perfil") {
            PerfilScreen(
                authViewModel = authViewModel,
                mapaViewModel = mapaViewModel, // ✅ Pasamos el compartido
                onNavigateToMapa = { navController.navigate("mapa") },
                onNavigateToRecuerdos = { navController.navigate("recuerdos") },
                onNavigateToFavoritos = { navController.navigate("favoritos") },
                onNavigateToEstadisticas = { navController.navigate("estadisticas") },
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
                mapaViewModel = mapaViewModel, // ✅ Pasamos el compartido
                onNavigateToMapa = { navController.navigate("mapa") },
                onNavigateToFavoritos = { navController.navigate("favoritos") },
                onNavigateToEstadisticas = { navController.navigate("estadisticas") },
                onNavigateToPerfil = { navController.navigate("perfil") }
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