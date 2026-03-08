package com.starcode.locus

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        RetrofitClient.init(this)
        super.onCreate(savedInstanceState)

        // ✅ PRIMERO QUE TODO: inicializar RetrofitClient con el contexto
        // Si esto va después del setContent, el token no estará disponible
        RetrofitClient.init(applicationContext)
        Log.d("LocusDebug", "✅ RetrofitClient inicializado")

        // ✅ SEGUNDO: Configuración de OSMDroid
        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName
        Log.d("LocusDebug", "✅ OSMDroid configurado")

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()

        setContent {
            LocusTheme {
                RequestLocationPermission()
                val navController = rememberNavController()
                NavGraph(navController = navController, dao = dao)
            }
        }
    }
}

@Composable
fun RequestLocationPermission() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("LocusDebug", "Permisos resultado: $permissions")
    }
    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))
    }
}