package com.starcode.locus.ui.navigation

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
import com.starcode.locus.ui.screens.*
import com.starcode.locus.ui.viewmodels.AuthViewModel
import com.starcode.locus.ui.viewmodels.AuthResult
import com.starcode.locus.ui.viewmodels.MapaViewModel

@Composable
fun NavGraph(navController: NavHostController, dao: LocusDao) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // 1. LÓGICA DE AUTO-LOGIN: Verificamos si ya hay un token guardado
    val sessionManager = remember { SessionManager(application) }
    val estaLogueado = sessionManager.obtenerToken() != null

    // Si hay token, empezamos en "mapa"; si no, en "welcome"
    val startDest = if (estaLogueado) "mapa" else "welcome"

    // 2. Instanciamos AuthViewModel
    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(application, dao) as T
            }
        }
    )

    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = startDest) {

        composable("welcome") {
            WelcomeScreen(onNavigateToLogin = { navController.navigate("login") })
        }

        composable("registro") {
            RegistroScreen(
                onRegistrar = { nombre, email, pass ->
                    authViewModel.registrar(nombre, "", email, pass)
                },
                onIrALogin = { navController.navigate("login") }
            )

            LaunchedEffect(authState) {
                if (authState is AuthResult.Success) {
                    navController.navigate("login")
                }
            }
        }

        composable("login") {
            LoginScreen(
                onLogin = { email, pass ->
                    authViewModel.login(email, pass)
                },
                onIrARegistrar = { navController.navigate("registro") }
            )

            LaunchedEffect(authState) {
                if (authState is AuthResult.Success) {
                    navController.navigate("mapa") {
                        popUpTo("login") { inclusive = true }
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            }
        }

        composable("mapa") {
            val mapaViewModel: MapaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MapaViewModel(dao) as T
                    }
                }
            )

            // Pasamos la navegación hacia el perfil
            MapaScreen(
                viewModel = mapaViewModel,
                onNavigateToPerfil = { navController.navigate("perfil") }
            )
        }

        // 3. NUEVA RUTA: Perfil / Opciones
        composable("perfil") {
            PerfilScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate("welcome") {
                        // Limpiamos todo el historial para que no pueda volver al mapa
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}