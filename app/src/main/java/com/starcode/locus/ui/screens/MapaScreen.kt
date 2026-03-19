package com.starcode.locus.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.location.Geocoder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
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

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    var nombreUbicacion by remember { mutableStateOf("Localizando...") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    var menuExpandido by remember { mutableStateOf(false) }
    val animY by animateDpAsState(
        targetValue = if (menuExpandido) 0.dp else 110.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val alphaAnim by animateFloatAsState(targetValue = if (menuExpandido) 1f else 0f)

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    // --- LANZADORES PARA RECUERDOS (Update) ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            scope.launch { Log.d("Locus", "Captura de cámara lista") }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            scope.launch { Log.d("Locus", "Imagen de galería seleccionada") }
        }
    }

    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

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
        val path = Path().apply { moveTo(center, 10f); lineTo(center - 20f, 40f); lineTo(center + 20f, 40f); close() }
        canvas.drawPath(path, dirPaint); canvas.drawCircle(center, center, radius, pointPaint); canvas.drawCircle(center, center, radius, borderPaint)
        bitmap
    }

    val marcadorNativo = remember {
        ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.apply {
            setTint(android.graphics.Color.parseColor("#E6673D"))
        }
    }

    val mapView = remember { MapView(context) }
    val locationOverlay = remember {
        MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation(); setDrawAccuracyEnabled(false)
            setPersonIcon(pointerBitmap); setDirectionIcon(pointerBitmap)
            setPersonAnchor(0.5f, 0.5f); setDirectionAnchor(0.5f, 0.5f)
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
    var debugStep by mutableStateOf("Iniciando...")

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
                } catch (e: Exception) { Log.e("kora", "GeoError: ${e.message}") }
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
                        icon = marcadorNativo
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        infoWindow = null
                        setOnMarkerClickListener { _, _ ->
                            val userPos = locationOverlay.myLocation
                            val radioReal = lugar.radio_activacion ?: 100
                            if (userPos != null) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(userPos.latitude, userPos.longitude, lugar.latitud, lugar.longitud, results)
                                if (results[0].toInt() <= radioReal) {
                                    lugarSeleccionado = lugar; showBottomSheet = true
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("👣 Te faltan ${results[0].toInt() - radioReal}m para desbloquear.") }
                                }
                            } else { lugarSeleccionado = lugar; showBottomSheet = true }
                            true
                        }
                    }
                    view.overlays.add(marker)
                }; view.invalidate()
            }
        )

        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 45.dp).align(Alignment.TopCenter),
            shape = RoundedCornerShape(30.dp), color = LocusSurfaceWhite, shadowElevation = 10.dp
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Explorador Kora", fontWeight = FontWeight.Bold, color = LocusDeepPurple, fontSize = 14.sp)
                    Text(nombreUbicacion, color = Color.Gray, fontSize = 11.sp)
                }
                IconButton(onClick = onNavigateToPerfil) { Icon(Icons.Default.Person, null, tint = LocusDeepPurple) }
            }
        }

        FloatingActionButton(
            onClick = { locationOverlay.myLocation?.let { mapView.controller.animateTo(it) } },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.MyLocation, null) }

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Column(
                    modifier = Modifier.offset(y = animY).alpha(alphaAnim),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { menuExpandido = false },
                        modifier = Modifier.size(56.dp),
                        containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape
                    ) { Icon(Icons.Default.Favorite, null) }

                    FloatingActionButton(
                        onClick = { menuExpandido = false },
                        modifier = Modifier.size(56.dp),
                        containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape
                    ) { Icon(Icons.Default.History, null) }

                    Spacer(modifier = Modifier.height(68.dp))
                }

                FloatingActionButton(
                    onClick = { menuExpandido = !menuExpandido },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (menuExpandido) LocusActionOrange else LocusSurfaceWhite,
                    contentColor = if (menuExpandido) Color.White else LocusDeepPurple,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (menuExpandido) Icons.Default.Close else Icons.Default.Menu,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (menuExpandido) 90f else 0f)
                    )
                }
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().background(LocusBackground.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(180.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Buscando historias...", color = LocusDeepPurple, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
            }
        }

        // --- FICHA ACTUALIZADA (BOTTOM SHEET) ---
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

                    // SECCIÓN DE ACCIONES (Favoritos y Recuerdos)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Botón Favoritos (Diseño listo)
                        OutlinedButton(
                            onClick = { /* Pendiente API */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LocusActionOrange),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LocusActionOrange)
                        ) {
                            Icon(Icons.Default.FavoriteBorder, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Favorito")
                        }

                        // Botón Recuerdo (Con diálogo de selección)
                        var showRecuerdoOptions by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showRecuerdoOptions = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange)
                        ) {
                            Icon(Icons.Default.AddAPhoto, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Recuerdo")
                        }

                        if (showRecuerdoOptions) {
                            AlertDialog(
                                onDismissRequest = { showRecuerdoOptions = false },
                                title = { Text("Añadir Recuerdo") },
                                text = { Text("Elige cómo quieres capturar este momento:") },
                                confirmButton = {
                                    TextButton(onClick = { cameraLauncher.launch(null); showRecuerdoOptions = false }) {
                                        Text("Cámara")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { galleryLauncher.launch("image/*"); showRecuerdoOptions = false }) {
                                        Text("Galería")
                                    }
                                }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // DATO CURIOSO
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
        Column(
            modifier = Modifier
                .align(Alignment.TopStart) // Arriba a la izquierda
                .padding(top = 120.dp, start = 16.dp) // Baja lo suficiente para no chocar con tu Surface blanca
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(16.dp)
                .zIndex(10f) // Esto asegura que esté por encima de cualquier otro elemento
        ) {
            val lugares by viewModel.lugares.collectAsState()
            Text("📍 Puntos en lista: ${lugares.size}", color = Color.Green, fontWeight = FontWeight.Bold)
            Text("⚙️ Estado: ${viewModel.debugStep}", color = Color.Yellow, fontSize = 12.sp)
        }
    }
}