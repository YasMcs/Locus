package com.starcode.locus.ui.screens

import android.graphics.PorterDuff
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.util.filtrarLugaresCercanos
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapaScreen(dao: LocusDao) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Colores de marca
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDark = Color(0xFF333333)

    // Estados
    var lugarSeleccionado by remember { mutableStateOf<LugarEntity?>(null) }
    var mostrarFicha by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Configuración OSM
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val mapView = remember { MapView(context) }
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            setDrawAccuracyEnabled(true)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> { mapView.onResume(); locationOverlay.enableMyLocation() }
                Lifecycle.Event.ON_PAUSE -> { locationOverlay.disableMyLocation(); mapView.onPause() }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(18.0)

                    locationOverlay.runOnFirstFix {
                        val myPos = locationOverlay.myLocation
                        if (myPos != null) {
                            handler.post {
                                controller.animateTo(myPos)
                                scope.launch {
                                    val lugares = dao.obtenerLugares()
                                    val cercanos = filtrarLugaresCercanos(myPos.latitude, myPos.longitude, lugares)

                                    cercanos.forEach { lugar ->
                                        val marker = Marker(mapView)
                                        marker.position = GeoPoint(lugar.latitud, lugar.longitud)

                                        // --- CAMBIAR EL PIN VERDE A NARANJA ---
                                        val icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)
                                        icon?.let {
                                            it.setTint(android.graphics.Color.parseColor("#E6673D"))
                                            // Usamos SRC_ATOP para asegurar que el color cubra bien el icono verde
                                            it.setTintMode(PorterDuff.Mode.SRC_ATOP)
                                            marker.icon = it
                                        }

                                        marker.infoWindow = null

                                        marker.setOnMarkerClickListener { _, _ ->
                                            val userPos = locationOverlay.myLocation
                                            if (userPos != null) {
                                                val results = FloatArray(1)
                                                android.location.Location.distanceBetween(
                                                    userPos.latitude, userPos.longitude,
                                                    lugar.latitud, lugar.longitud,
                                                    results
                                                )
                                                val distanciaMetros = results[0]

                                                if (distanciaMetros <= lugar.radio_activacion) {
                                                    lugarSeleccionado = lugar
                                                    mostrarFicha = true
                                                } else {
                                                    val faltante = (distanciaMetros - lugar.radio_activacion).toInt()
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            "👣 Te faltan $faltante metros para desbloquear esta historia."
                                                        )
                                                    }
                                                }
                                            }
                                            true
                                        }
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

        // Panel Superior
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter),
            shape = RoundedCornerShape(24.dp),
            color = LocusBackground.copy(alpha = 0.95f),
            shadowElevation = 6.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Explorador Locus", fontWeight = FontWeight.ExtraBold, color = LocusDark)
                    Text("Acércate a los pines para ver su historia", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }

        // Aviso de Distancia (Snackbar)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        ) { data ->
            Card(
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = LocusDark),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = data.visuals.message,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Ficha Histórica (AlertDialog)
        // Ficha Histórica (AlertDialog)
        if (mostrarFicha && lugarSeleccionado != null) {
            AlertDialog(
                onDismissRequest = { mostrarFicha = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = LocusBackground,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LocusActionOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = lugarSeleccionado!!.titulo_ficha,
                            fontWeight = FontWeight.ExtraBold, // Más grueso para que resalte
                            color = LocusDark,                 // Color oscuro definido arriba
                            style = MaterialTheme.typography.headlineSmall // Tamaño más imponente
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = lugarSeleccionado!!.descripcion_hist,
                            lineHeight = 24.sp,
                            color = LocusDark,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(
                            color = LocusActionOrange.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💡 DATO CURIOSO",
                                    fontWeight = FontWeight.Bold,
                                    color = LocusActionOrange,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = lugarSeleccionado!!.dato_curioso,
                                    color = Color.DarkGray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarFicha = false }) {
                        Text(
                            text = "ENTENDIDO",
                            fontWeight = FontWeight.ExtraBold,
                            color = LocusActionOrange,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
}
}