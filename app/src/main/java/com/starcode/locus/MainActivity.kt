package com.starcode.locus

import android.content.Context // ✅ Añadido para SharedPreferences
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
import com.starcode.locus.ui.theme.LocusTheme
import com.google.firebase.messaging.FirebaseMessaging
import com.starcode.locus.ui.screens.NavGraph
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        RetrofitClient.init(applicationContext)
        Log.d("LocusDebug", "✅ RetrofitClient inicializado")

        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        setContent {
            LocusTheme {
                RequestAllPermissions()
                val navController = rememberNavController()
                NavGraph(navController = navController, dao = dao)
            }
        }

        // --- LÓGICA DE FIREBASE ---
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val token = task.result
                Log.d("FCM_Locus", "✅ Token obtenido: $token")

                if (token.isNotEmpty()) {

                    val sharedPref = getSharedPreferences("LocusPrefs", Context.MODE_PRIVATE)
                    val usuarioLogueado = sharedPref.getString("usuario_logueado", null)


                    if (usuarioLogueado != null) {
                        Thread {
                            try {
                                // 2. Nueva URL de Railway proporcionada por tu backend
                                val urlRailway = "https://locus-api-production-13fe.up.railway.app/api/users/update-token"
                                val url = java.net.URL(urlRailway)

                                val conn = url.openConnection() as java.net.HttpURLConnection
                                conn.requestMethod = "POST"
                                conn.setRequestProperty("Content-Type", "application/json")
                                conn.doOutput = true

                                // 3. El JSON ahora usa los campos exactos: username y token
                                val jsonInputString = """
                                    {
                                        "username": "$usuarioLogueado",
                                        "token": "$token"
                                    }
                                """.trimIndent()

                                conn.outputStream.use { os ->
                                    os.write(jsonInputString.toByteArray(charset("utf-8")))
                                }

                                Log.d("FCM_Locus", "✅ Registro en Railway exitoso ($usuarioLogueado): ${conn.responseCode}")
                            } catch (e: Exception) {
                                Log.e("FCM_Locus", "❌ Error al conectar con Railway: ${e.message}")
                            }
                        }.start()
                    } else {
                        Log.d("FCM_Locus", "ℹ️ Esperando a que el usuario inicie sesión para registrar token.")
                    }
                }
            } else {
                Log.w("FCM_Locus", "⚠️ Fallo al obtener el token")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        launcher.launch(permissionsToRequest.toTypedArray())
    }
}