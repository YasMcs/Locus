package com.starcode.locus

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.starcode.locus.data.remote.ActividadRequest
import com.starcode.locus.data.remote.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializaciones
        RetrofitClient.init(applicationContext)
        sessionManager = SessionManager(applicationContext)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        setContent {
            LocusTheme {
                RequestAllPermissions() // ✅ Ahora incluye Cámara y Actividad Física
                val navController = rememberNavController()
                NavGraph(navController = navController, dao = dao)
            }
        }

        configurarFirebase()
    }

    // --- LÓGICA DEL SENSOR DE PASOS ---
    override fun onResume() {
        super.onResume()
        stepSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val pasosTotales = event.values[0].toInt()

            // Lógica: Cada 50 pasos, enviamos una actualización de distancia
            // Multiplicamos pasos por 0.76 (zancada promedio) para obtener metros
            val distanciaMetros = pasosTotales * 0.76f

            enviarActividadAlServidor(pasosTotales, distanciaMetros)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun enviarActividadAlServidor(pasos: Int, distancia: Float) {
        val userId = sessionManager.getUserId()
        val token = sessionManager.obtenerToken()

        if (userId > 0 && !token.isNullOrBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = ActividadRequest(
                        id_usuario = userId,
                        distancia_metros = distancia,
                        pasos = pasos,
                        duracion_segundos = 0 // Podrías calcular el tiempo si fuera necesario
                    )
                    RetrofitClient.instance.registrarActividad("Bearer $token", request)
                    Log.d("LocusDebug", "👣 Actividad enviada: $distancia m")
                } catch (e: Exception) {
                    Log.e("LocusDebug", "❌ Error enviando pasos: ${e.message}")
                }
            }
        }
    }

    private fun configurarFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val token = task.result
                val sharedPref = getSharedPreferences("LocusPrefs", Context.MODE_PRIVATE)
                val usuarioLogueado = sharedPref.getString("usuario_logueado", null)

                if (usuarioLogueado != null && token.isNotEmpty()) {
                    Thread {
                        try {
                            val url = java.net.URL("https://locus-api-production-13fe.up.railway.app/api/users/update-token")
                            val conn = url.openConnection() as java.net.HttpURLConnection
                            conn.requestMethod = "POST"
                            conn.setRequestProperty("Content-Type", "application/json")
                            conn.doOutput = true
                            val jsonInputString = """{"username": "$usuarioLogueado", "token": "$token"}"""
                            conn.outputStream.use { it.write(jsonInputString.toByteArray(charset("utf-8"))) }
                            Log.d("FCM_Locus", "✅ Token actualizado en server")
                        } catch (e: Exception) {
                            Log.e("FCM_Locus", "❌ Error update token: ${e.message}")
                        }
                    }.start()
                }
            }
        }
    }
}

@Composable
fun RequestAllPermissions() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("LocusDebug", "Permisos actualizados: $permissions")
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA // ✅ PERMISO DE CÁMARA AÑADIDO
        )

        // Permiso de Actividad Física (Necesario desde Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        // Permiso de Notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        launcher.launch(permissions.toTypedArray())
    }
}