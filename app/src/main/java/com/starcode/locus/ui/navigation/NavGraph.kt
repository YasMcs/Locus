package com.starcode.locus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.ui.screens.RegistroScreen
import com.starcode.locus.ui.screens.LoginScreen
import com.starcode.locus.ui.screens.MapaScreen
import com.starcode.locus.ui.screens.WelcomeScreen // Importamos la nueva pantalla
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.starcode.locus.data.entities.UsuarioEntity

@Composable
fun NavGraph(navController: NavHostController, dao: LocusDao) {
    val scope = rememberCoroutineScope()

    // Cambiamos el inicio a "welcome" para mostrar el diseño estético primero
    NavHost(navController = navController, startDestination = "welcome") {

        // --- Pantalla de Bienvenida (NUEVA) ---
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        // --- Pantalla de Registro ---
        composable("registro") {
            RegistroScreen(
                onRegistrar = { nombre, email, pass ->
                    scope.launch {
                        val nuevoUsuario = UsuarioEntity(
                            nombre = nombre,
                            ape_pa = "",
                            ape_ma = "",
                            email = email,
                            password = pass
                        )
                        dao.registrarUsuario(nuevoUsuario)
                        navController.navigate("login")
                    }
                },
                onIrALogin = { navController.navigate("login") }
            )
        }

        // --- Pantalla de Login ---
        composable("login") {
            LoginScreen(
                onLogin = { email, pass ->
                    navController.navigate("mapa")
                },
                // ESTA ES LA LÍNEA QUE ARREGLA EL ERROR ROJO
                onIrARegistrar = {
                    navController.navigate("registro")
                }
            )
        }

        // --- Pantalla del Mapa ---
        composable("mapa") {
            MapaScreen(dao = dao)
        }
    }
}