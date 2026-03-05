package com.starcode.locus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.starcode.locus.data.database.AppDatabase
import com.starcode.locus.ui.theme.LocusTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.starcode.locus.ui.navigation.NavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. IMPORTANTE: Configuración de OSMDroid ANTES del setContent
        org.osmdroid.config.Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        org.osmdroid.config.Configuration.getInstance().userAgentValue = packageName

        val db = AppDatabase.getDatabase(this)
        val dao = db.locusDao()
        com.starcode.locus.util.DatabaseSeeder.insertarPuntosDePrueba(dao)
        setContent {
            LocusTheme {
                // 2. Pedir permisos automáticamente al iniciar
                RequestLocationPermission()

                val navController = rememberNavController()
                NavGraph(navController = navController, dao = dao)
            }
        }
    }
}

@Composable
fun RequestLocationPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Aquí podrías manejar si el usuario denegó los permisos
    }

    LaunchedEffect(Unit) {
        launcher.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE // Necesario para guardar mapas en caché
        ))
    }
}