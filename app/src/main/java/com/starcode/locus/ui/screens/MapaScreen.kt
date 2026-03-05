package com.starcode.locus.ui.screens

import android.graphics.PorterDuff
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person // Icono para el perfil
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
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapaScreen(
    viewModel: MapaViewModel,
    onNavigateToPerfil: () -> Unit // AGREGADO: Callback para navegación
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val lugares by viewModel.lugares.collectAsState()
    val cargando by viewModel.estaCargando.collectAsState()

    // Colores de marca
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDark = Color(0xFF333333)

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

    LaunchedEffect(lugares) {
        mapView.overlays.clear()
        mapView.overlays.add(locationOverlay)
        lugares.forEach { lugar ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(lugar.latitud, lugar.longitud)
                infoWindow = null
                val icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)
                icon?.let {
                    it.setTint(android.graphics.Color.parseColor("#E6673D"))
                    it.setTintMode(PorterDuff.Mode.SRC_ATOP)
                    this.icon = it
                }
                setOnMarkerClickListener { _, _ ->
                    val userPos = locationOverlay.myLocation
                    if (userPos != null) {
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            userPos.latitude, userPos.longitude,
                            lugar.latitud, lugar.longitud,
                            results
                        )
                        val radioReal = lugar.radio_activacion ?: 50
                        if (results[0] <= radioReal) {
                            lugarSeleccionado = lugar
                            mostrarFicha = true
                        } else {
                            val faltante = (results[0] - radioReal).toInt()
                            scope.launch { snackbarHostState.showSnackbar("👣 Te faltan $faltante metros para desbloquear.") }
                        }
                    }
                    true
                }
            }
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
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
                        if (myPos != null) { handler.post { controller.animateTo(myPos) } }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (cargando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = LocusActionOrange
            )
        }

        // Panel Superior
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.TopCenter).padding(top = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = LocusBackground.copy(alpha = 0.95f),
            shadowElevation = 6.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Explorador Locus", fontWeight = FontWeight.ExtraBold, color = LocusDark)
                    Text("Suchiapa, Chiapas", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }

        // AGREGADO: Botón Flotante para ir al Perfil
        SmallFloatingActionButton(
            onClick = onNavigateToPerfil,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 24.dp),
            containerColor = LocusBackground,
            contentColor = LocusActionOrange,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Perfil")
        }

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

        if (mostrarFicha && lugarSeleccionado != null) {
            AlertDialog(
                onDismissRequest = { mostrarFicha = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = LocusBackground,
                title = { Text(text = lugarSeleccionado?.titulo_ficha ?: "Lugar sin nombre", fontWeight = FontWeight.ExtraBold) },
                text = {
                    Column {
                        Text(text = lugarSeleccionado?.descripcion_hist ?: "No hay descripción disponible.")
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = lugarSeleccionado?.dato_curioso ?: "¡Pronto habrá más información!", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { mostrarFicha = false }) {
                        Text("ENTENDIDO", fontWeight = FontWeight.ExtraBold, color = LocusActionOrange)
                    }
                }
            )
        }
    }
}