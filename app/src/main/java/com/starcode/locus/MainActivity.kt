package com.starcode.locus

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.starcode.locus.data.database.AppDatabase
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.ui.theme.LocusTheme
import com.google.firebase.messaging.FirebaseMessaging
import com.starcode.locus.ui.screens.NavGraph
import androidx.activity.enableEdgeToEdge
import com.starcode.locus.data.remote.request.ActividadRequest
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.ui.services.LocusFirebaseService
import com.starcode.locus.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private lateinit var sessionManager: SessionManager
    private lateinit var mapaViewModel: MapaViewModel
    private var pasosAlIniciarApp = -1
    private var ultimoEnvioPasosTotales = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        RetrofitClient.init(applicationContext)
        sessionManager = SessionManager(applicationContext)

        // Crear canal de notificaciones al arrancar
        crearCanalNotificaciones()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        mapaViewModel = MapaViewModel(application, dao)

        setContent {
            LocusTheme {
                val context = androidx.compose.ui.platform.LocalContext.current

                // ✅ LLAMADA LIMPIA
                VerificadorDePermisos(context)

                val navController = rememberNavController()
                NavGraph(navController = navController, dao = dao)
            }
        }

        configurarFirebase()
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "locus_channel_urgent"
            val name = "Notificaciones Locus"
            val descriptionText = "Alertas de proximidad de lugares"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // --- LÓGICA DE SENSORES (Mantenida) ---
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
            val pasosTotalesDesdeReinicio = event.values[0].toInt()
            if (pasosAlIniciarApp == -1) {
                pasosAlIniciarApp = pasosTotalesDesdeReinicio
                ultimoEnvioPasosTotales = pasosTotalesDesdeReinicio
                return
            }
            val diferenciaParaServidor = pasosTotalesDesdeReinicio - ultimoEnvioPasosTotales
            val pasosSesionActual = pasosTotalesDesdeReinicio - pasosAlIniciarApp
            val distanciaSesionActual = pasosSesionActual * 0.76

            mapaViewModel.actualizarMonitorActividad(pasosSesionActual, distanciaSesionActual)

            if (diferenciaParaServidor >= 3) {
                val distanciaTramo = diferenciaParaServidor * 0.76
                enviarActividadAlServidor(diferenciaParaServidor, distanciaTramo)
                ultimoEnvioPasosTotales = pasosTotalesDesdeReinicio
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun enviarActividadAlServidor(pasos: Int, distancia: Double) {
        if (!::sessionManager.isInitialized || !::mapaViewModel.isInitialized) return
        val userId = sessionManager.getUserId()
        if (userId <= 0) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = ActividadRequest(userId, distancia, pasos, 10)
                RetrofitClient.instance.registrarActividad(request)
            } catch (e: Exception) {
                Log.e("LocusDebug", "Error actividad: ${e.message}")
            }
        }
    }

    private fun configurarFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val fcmToken = task.result
                val usuarioLogueado = getSharedPreferences("locus_prefs", Context.MODE_PRIVATE).getString("usuario_logueado", null)

                if (usuarioLogueado != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Endpoint Railway para actualizar FCM
                        } catch (e: Exception) { }
                    }
                }
            }
        }
    }
}

@Composable
fun VerificadorDePermisos(context: Context) {
    val prefs = remember { context.getSharedPreferences("LocusPrefs", Context.MODE_PRIVATE) }

    // Solo mostramos el diálogo si:
    // 1. NO tenemos el permiso de fondo.
    // 2. NUNCA hemos guardado la marca de "ya solicitado".
    // 3. Estamos en Android 10 o superior.
    var mostrarDialogo by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    !prefs.getBoolean("background_perm_requested", false)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Si después de pedir los básicos, falta el de fondo, mostramos el aviso
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mostrarDialogo = true
            }
        }
    }

    // Pedir permisos básicos SOLO si faltan
    LaunchedEffect(Unit) {
        val basicos = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) basicos.add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) basicos.add(Manifest.permission.POST_NOTIFICATIONS)

        val faltanBasicos = basicos.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (faltanBasicos) {
            launcher.launch(basicos.toTypedArray())
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Ubicación en segundo plano") },
            text = { Text("Para avisarte de lugares cercanos con la app cerrada, selecciona 'Permitir todo el tiempo' en los ajustes de ubicación.") },
            confirmButton = {
                Button(onClick = {
                    // ✅ BLOQUEO INMEDIATO: Guardamos la marca ANTES de irnos a ajustes
                    prefs.edit().putBoolean("background_perm_requested", true).apply()
                    mostrarDialogo = false

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }) { Text("Ir a Ajustes") }
            },
            dismissButton = {
                Button(onClick = {
                    // Si dice que no, guardamos la marca para no molestar más
                    prefs.edit().putBoolean("background_perm_requested", true).apply()
                    mostrarDialogo = false
                }) { Text("Ahora no") }
            }
        )
    }
}