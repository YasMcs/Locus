package com.starcode.locus
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import android.os.Bundle
import android.widget.Toast
import com.starcode.locus.data.entities.UsuarioEntity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.starcode.locus.data.database.AppDatabase
import com.starcode.locus.ui.theme.LocusTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos el DAO de Room
        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName
        setContent {
            LocusTheme {
                val navController = rememberNavController()

                // Grafo de Navegación
                NavHost(navController = navController, startDestination = "registro") {

                    // Pantalla de Registro
                    composable("registro") {
                        RegistroScreen(
                            onRegistrar = { nombre, email, pass ->
                                lifecycleScope.launch {
                                    val nuevo = UsuarioEntity(nombre = nombre, ape_pa = "", ape_ma = "", email = email, password = pass)
                                    dao.registrarUsuario(nuevo)
                                    navController.navigate("login")
                                }
                            },
                            onIrALogin = { navController.navigate("login") }
                        )
                    }

                    // Pantalla 2: Login
                    composable("login") {
                        // Definimos la interfaz del Login
                        LoginScreen(
                            onLogin = { email, pass ->

                                lifecycleScope.launch {
                                    val usuarioEncontrado = dao.buscarUsuarioPorEmail(email)

                                    if (usuarioEncontrado != null) {
                                        if (usuarioEncontrado.password == pass) {
                                            navController.navigate("mapa")
                                        } else {
                                            Toast.makeText(this@MainActivity, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(this@MainActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } // <--- Cierre de onLogin
                        )
                    }

                    composable("mapa") {
                        val context = LocalContext.current

                        // Configuramos la vista del mapa
                        AndroidView(
                            factory = { ctx ->
                                org.osmdroid.views.MapView(ctx).apply {
                                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(15.0)
                                    val startPoint = org.osmdroid.util.GeoPoint(19.4326, -99.1332)
                                    controller.setCenter(startPoint)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    }
                }
            }
        }
    }


// --- COMPONENTES DE INTERFAZ (Fueron los que te dieron error por no existir) ---

@Composable
fun RegistroScreen(onRegistrar: (String, String, String) -> Unit, onIrALogin: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onRegistrar(nombre, email, pass) }, modifier = Modifier.fillMaxWidth()) { Text("Registrarme") }
        TextButton(onClick = onIrALogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onLogin(email, pass) }, modifier = Modifier.fillMaxWidth()) { Text("Entrar") }
    }
}