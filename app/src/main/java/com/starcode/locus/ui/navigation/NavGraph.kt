package com.starcode.locus.ui.navigation

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
            val authState by authViewModel.authState.collectAsState()

            RegistroScreen(
                onRegistrar = { nom, pat, mat, _, mail, pw ->
                    authViewModel.registrarUsuario(nom, pat, mat, fechaParaDB, mail, pw)
                },
                onIrALogin = { navController.navigate("login") },
                authState = authState,
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
                    Log.d("LocusDebug", "✅ Login Exitoso detectado en NavGraph")
                    navController.navigate("mapa") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                    authViewModel.resetAuthState()
                }
            }
        }

        composable("mapa") {
            SideEffect { Log.d("LocusDebug", "🚀 [NAVGRAPH] Cargando ruta MAPA") }
            val mapaViewModel: MapaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MapaViewModel(application, dao) as T
                    }
                }
            )
            MapaScreen(
                viewModel = mapaViewModel,
                onNavigateToPerfil = { navController.navigate("perfil") }
            )
        }

        // En NavGraph.kt (Línea 128 aprox)
        composable("perfil") {
            PerfilScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogoutNavigation = { // 👈 Cambia 'onLogout' por 'onLogoutNavigation'
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        }
    }
