package com.starcode.locus.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.airbnb.lottie.compose.*
import com.starcode.locus.R
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    viewModel: MapaViewModel,
    onNavigateToPerfil: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Colores Locus
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    var nombreUbicacion by remember { mutableStateOf("Localizando...") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    // Configuración Lottie para doggy.json
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    // Configuración de OSMDroid
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // --- DISEÑO 1: PUNTERO USUARIO (AURA BLUE) ---
    val pointerBitmap = remember {
        val size = 120
        val center = size / 2f
        val radius = 18f
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val gpsBlue = android.graphics.Color.parseColor("#007AFF")
        val gpsBlueAura = android.graphics.Color.parseColor("#33007AFF")

        val auraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gpsBlueAura }
        val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            setShadowLayer(8f, 0f, 2f, android.graphics.Color.argb(60, 0, 0, 0))
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gpsBlue; style = Paint.Style.STROKE; strokeWidth = 5f }
        val dirPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gpsBlue; pathEffect = android.graphics.CornerPathEffect(8f) }

        canvas.drawCircle(center, center, radius + 15f, auraPaint)
        val path = Path().apply {
            moveTo(center, 10f)
            lineTo(center - 20f, 40f)
            lineTo(center + 20f, 40f)
            close()
        }
        canvas.drawPath(path, dirPaint)
        canvas.drawCircle(center, center, radius, pointPaint)
        canvas.drawCircle(center, center, radius, borderPaint)
        bitmap
    }

    // --- DISEÑO 2: CARGA BANDERITA (pin.png) ---
    val banderaRedimensionada = remember {
        val drawable = ContextCompat.getDrawable(context, R.drawable.pin)
        drawable?.toBitmap(width = 50, height = 50, config = Bitmap.Config.ARGB_8888)
    }

    val mapView = remember { MapView(context) }
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            setDrawAccuracyEnabled(false)
            setPersonIcon(pointerBitmap)
            setDirectionIcon(pointerBitmap)
            setPersonAnchor(0.5f, 0.5f)
            setDirectionAnchor(0.5f, 0.5f)
            runOnFirstFix {
                (context as? android.app.Activity)?.runOnUiThread {
                    mapView.controller.animateTo(myLocation)
                    mapView.controller.setZoom(17.5)
                }
            }
        }
    }

    val lugares by viewModel.lugares.collectAsState()
    val cargando by viewModel.estaCargando.collectAsState()
    var lugarSeleccionado by remember { mutableStateOf<LugarEntity?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarLugares() }

    LaunchedEffect(locationOverlay.myLocation) {
        locationOverlay.myLocation?.let { geoPoint ->
            withContext(Dispatchers.IO) {
                try {
                    val direcciones = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                    if (!direcciones.isNullOrEmpty()) {
                        val municipio = direcciones[0].locality ?: direcciones[0].subAdminArea ?: "Explorando"
                        val estado = direcciones[0].adminArea ?: ""
                        nombreUbicacion = "$municipio, $estado"
                    }
                } catch (e: Exception) { Log.e("Locus", "GeoError: ${e.message}") }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { mapView.onResume(); locationOverlay.enableMyLocation() }
            else if (event == Lifecycle.Event.ON_PAUSE) { locationOverlay.disableMyLocation(); mapView.onPause() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // CAPA 0: MAPA
        AndroidView(
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(15.0)
                    overlays.add(locationOverlay)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.overlays.removeIf { it is Marker }
                lugares.forEach { lugar ->
                    val marker = Marker(view).apply {
                        position = GeoPoint(lugar.latitud, lugar.longitud)
                        banderaRedimensionada?.let {
                            icon = android.graphics.drawable.BitmapDrawable(context.resources, it)
                        }
                        setAnchor(0.22f, 1.0f)
                        infoWindow = null
                        setOnMarkerClickListener { _, _ ->
                            val userPos = locationOverlay.myLocation
                            val radioReal = lugar.radio_activacion ?: 100
                            if (userPos != null) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(userPos.latitude, userPos.longitude, lugar.latitud, lugar.longitud, results)
                                if (results[0].toInt() <= radioReal) {
                                    lugarSeleccionado = lugar
                                    showBottomSheet = true
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("👣 Te faltan ${results[0].toInt() - radioReal}m para desbloquear.") }
                                }
                            } else {
                                lugarSeleccionado = lugar
                                showBottomSheet = true
                            }
                            true
                        }
                    }
                    view.overlays.add(marker)
                }
                view.invalidate()
            }
        )

        // CAPA 1: CABECERA
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 45.dp).align(Alignment.TopCenter),
            shape = RoundedCornerShape(30.dp), color = LocusSurfaceWhite, shadowElevation = 10.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Explorador Locus", fontWeight = FontWeight.Bold, color = LocusDeepPurple, fontSize = 14.sp)
                    Text(nombreUbicacion, color = Color.Gray, fontSize = 11.sp)
                }
                IconButton(onClick = onNavigateToPerfil) { Icon(Icons.Default.Person, null, tint = LocusDeepPurple) }
            }
        }

        // CAPA 2: BOTÓN MI UBICACIÓN
        FloatingActionButton(
            onClick = { locationOverlay.myLocation?.let { mapView.controller.animateTo(it) } },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.MyLocation, null) }

        // CAPA 3: LOADING PERSONALIZADO (DOGGY)
        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocusBackground.copy(alpha = 0.8f)), // Fondo suave para que resalte
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(180.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Buscando historias...",
                        color = LocusDeepPurple,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // CAPA 4: BOTTOM SHEET
        if (showBottomSheet && lugarSeleccionado != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = LocusBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = LocusActionOrange) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 50.dp)) {
                    Text(lugarSeleccionado?.titulo_ficha ?: "Lugar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(12.dp))
                    Text(lugarSeleccionado?.descripcion_hist ?: "", style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                    Spacer(Modifier.height(24.dp))
                    Surface(color = LocusActionOrange.copy(alpha = 0.1f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 24.sp); Spacer(Modifier.width(16.dp))
                            Text(lugarSeleccionado?.dato_curioso ?: "", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}