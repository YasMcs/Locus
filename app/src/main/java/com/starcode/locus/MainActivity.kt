package com.starcode.locus

import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.starcode.locus.data.database.AppDatabase
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.entities.UsuarioEntity
import com.starcode.locus.ui.theme.LocusTheme
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Toast.makeText(this, "GPS Concedido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        requestPermissionLauncher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            LocusTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()

                NavHost(navController = navController, startDestination = "registro") {
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

                    composable("login") {
                        LoginScreen(
                            onLogin = { email, pass ->
                                lifecycleScope.launch {
                                    val usuarioEncontrado = dao.buscarUsuarioPorEmail(email)
                                    if (usuarioEncontrado?.password == pass) {
                                        navController.navigate("mapa")
                                    } else {
                                        Toast.makeText(this@MainActivity, "Error de acceso", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }

                    composable("mapa") {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(17.0)

                                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                                    locationOverlay.enableMyLocation()
                                    locationOverlay.enableFollowLocation()

                                    locationOverlay.runOnFirstFix {
                                        val myPos = locationOverlay.myLocation
                                        if (myPos != null) {
                                            handler.post {
                                                controller.animateTo(myPos)
                                                scope.launch {
                                                    val lugares = dao.obtenerLugares()
                                                    val cercanos = filtrarLugaresCercanos(myPos.latitude, myPos.longitude, lugares)

                                                    cercanos.forEach { lugar ->
                                                        val marker = org.osmdroid.views.overlay.Marker(this@apply)
                                                        marker.position = GeoPoint(lugar.latitud, lugar.longitud)
                                                        marker.title = lugar.nombre_lugar
                                                        overlays.add(marker)
                                                    }
                                                    invalidate()
                                                }
                                            }
                                        }
                                    }
                                    overlays.add(locationOverlay)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } // Cierre LocusTheme
        } // Cierre setContent
    } // Cierre onCreate

    private fun filtrarLugaresCercanos(miLat: Double, miLon: Double, lugares: List<LugarEntity>): List<LugarEntity> {
        val resultados = FloatArray(1)
        return lugares.filter {
            Location.distanceBetween(miLat, miLon, it.latitud, it.longitud, resultados)
            resultados[0] <= 500
        }
    }
} // Cierre MainActivity

// --- COMPONENTES UI (Fuera de la clase) ---

@Composable
fun RegistroScreen(onRegistrar: (String, String, String) -> Unit, onIrALogin: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Pass") }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = { onRegistrar(nombre, email, pass) }) { Text("Registrar") }
        TextButton(onClick = onIrALogin) { Text("Ir al Login") }
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Login", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Pass") }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = { onLogin(email, pass) }) { Text("Entrar") }
    }
}