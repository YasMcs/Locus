package com.starcode.locus

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.starcode.locus.data.database.AppDatabase
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.ui.navigation.NavGraph
import com.starcode.locus.ui.theme.LocusTheme
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Llamado primero

        // ✅ Solo una inicialización del cliente
        RetrofitClient.init(applicationContext)
        Log.d("LocusDebug", "✅ RetrofitClient inicializado")

        // ✅ Configuración de OSMDroid
        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        setContent {
            LocusTheme {
                // Aquí solo llamamos a una función que se encargue de TODOS los permisos
                RequestAllPermissions()
                val navController = rememberNavController()
                NavGraph(navController = navController, dao = dao)
            }
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val token = task.result
                Log.d("FCM_Locus", "✅ Token obtenido: $token")

                // SOLO enviamos si el token no es una cadena vacía
                if (token.isNotEmpty()) {
                    Thread {
                        try {
                            val nombreUsuario = "Yasleb"
                            val url = java.net.URL("http://192.168.1.86:3000/register-token")
                            val conn = url.openConnection() as java.net.HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.setRequestProperty("Content-Type", "application/json")
                            conn.doOutput = true

                            // REVISA BIEN: la llave debe ser "nombre" para que el server lo lea
                            val jsonInputString = "{\"username\": \"$nombreUsuario\", \"token\": \"$token\"}"

                            conn.outputStream.use { os ->
                                os.write(jsonInputString.toByteArray(charset("utf-8")))
                            }

                            Log.d("FCM_Locus", "Respuesta del servidor: ${conn.responseCode}")
                        } catch (e: Exception) {
                            Log.e("FCM_Locus", "Error: ${e.message}")
                        }
                    }.start()
                }
            } else {
                Log.w("FCM_Locus", "Fallo al obtener el token o permiso pendiente")
            }
        }
    }
}

@Composable
fun RequestAllPermissions() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("FCM_Locus", "Resultado de permisos: $permissions")
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Si es Android 13+, agregamos las notificaciones al mismo paquete de petición
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        launcher.launch(permissionsToRequest.toTypedArray())
    }
}