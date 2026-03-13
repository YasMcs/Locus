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
import androidx.core.content.ContextCompat
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

    // Lanzadores de Cámara y Galería
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if (it != null) Log.d("Locus", "Foto capturada")
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) Log.d("Locus", "Galería seleccionada")
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

    LaunchedEffect(Unit) { viewModel.cargarLugares() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    // --- AQUÍ ESTABA EL ERROR: AGREGUÉ ESTAS DOS LÍNEAS ---
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(16.75, -93.11)) // Un centro inicial (ej. Chiapas) para que no se vea el mundo repetido
                    overlays.add(locationOverlay)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.overlays.removeIf { it is Marker }
                lugares.forEach { lugar ->
                    val marker = Marker(view).apply {
                        position = GeoPoint(lugar.latitud, lugar.longitud)
                        icon = marcadorNativo; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); infoWindow = null
                        setOnMarkerClickListener { _, _ ->
                            val userPos = locationOverlay.myLocation
                            val radioReal = lugar.radio_activacion ?: 100
                            if (userPos != null) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(userPos.latitude, userPos.longitude, lugar.latitud, lugar.longitud, results)
                                if (results[0].toInt() <= radioReal) { lugarSeleccionado = lugar; showBottomSheet = true }
                                else { scope.launch { snackbarHostState.showSnackbar("👣 Te faltan ${results[0].toInt() - radioReal}m para desbloquear.") } }
                            } else { lugarSeleccionado = lugar; showBottomSheet = true }
                            true
                        }
                    }; view.overlays.add(marker)
                }; view.invalidate()
            }
        )

        // Header
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

        // Botones flotantes (MyLocation y Menú) se mantienen...
        FloatingActionButton(
            onClick = { locationOverlay.myLocation?.let { mapView.controller.animateTo(it) } },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.MyLocation, null) }

        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Column(modifier = Modifier.offset(y = animY).alpha(alphaAnim), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(onClick = {}, modifier = Modifier.size(56.dp), containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape) { Icon(Icons.Default.Favorite, null) }
                    FloatingActionButton(onClick = {}, modifier = Modifier.size(56.dp), containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape) { Icon(Icons.Default.History, null) }
                    Spacer(modifier = Modifier.height(68.dp))
                }
                FloatingActionButton(onClick = { menuExpandido = !menuExpandido }, modifier = Modifier.size(56.dp), containerColor = if (menuExpandido) LocusActionOrange else LocusSurfaceWhite, contentColor = if (menuExpandido) Color.White else LocusDeepPurple, shape = CircleShape) {
                    Icon(if (menuExpandido) Icons.Default.Close else Icons.Default.Menu, null, modifier = Modifier.rotate(if (menuExpandido) 90f else 0f))
                }
            }
        }

        // --- FICHA PROFESIONAL: CORAZÓN ARRIBA, BOTÓN ANCHO ABAJO ---
        if (showBottomSheet && lugarSeleccionado != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = LocusBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = LocusActionOrange) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 50.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            lugarSeleccionado?.titulo_ficha ?: "Lugar",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = LocusDeepPurple
                        )
                        IconButton(onClick = { /* API Favoritos */ }) {
                            Icon(Icons.Default.FavoriteBorder, null, tint = LocusActionOrange)
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(lugarSeleccionado?.descripcion_hist ?: "", style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp, color = LocusDeepPurple.copy(alpha = 0.8f))

                    Spacer(Modifier.height(24.dp))

                    Surface(color = LocusActionOrange.copy(alpha = 0.05f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💡", fontSize = 22.sp); Spacer(Modifier.width(12.dp))
                            Text(lugarSeleccionado?.dato_curioso ?: "", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = LocusDeepPurple.copy(alpha = 0.8f))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    var showOptions by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showOptions = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange)
                    ) {
                        Icon(Icons.Default.AddAPhoto, null, tint=Color.White)
                        Spacer(Modifier.width(10.dp))
                        Text("Añadir Recuerdo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (showOptions) {
                        AlertDialog(
                            onDismissRequest = { showOptions = false },
                            containerColor = LocusSurfaceWhite,
                            title = { Text("Nuevo Recuerdo", fontWeight = FontWeight.Bold) },
                            text = { Text("¿Cómo quieres guardar este momento?") },
                            confirmButton = {
                                Button(onClick = { cameraLauncher.launch(null); showOptions = false }, colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange)) {
                                    Text("Cámara")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { galleryLauncher.launch("image/*"); showOptions = false }) {
                                    Text("Galería")
                                }
                            }
                        )
                    }
                }
            }
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().background(LocusBackground.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(180.dp))
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}