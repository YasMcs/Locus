package com.starcode.locus.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.airbnb.lottie.compose.*
import com.starcode.locus.R
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    sessionManager: SessionManager,
    onNavigateToPerfil: () -> Unit,
    onNavigateToRecuerdos: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToEstadisticas: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Colores Locus
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    // Estados de menú
    var menuExpandido by remember { mutableStateOf(false) }
    val animY by animateDpAsState(
        targetValue = if (menuExpandido) 0.dp else 110.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy),
        label = "animY"
    )
    val alphaAnim by animateFloatAsState(targetValue = if (menuExpandido) 1f else 0f)

    var nombreUbicacion by remember { mutableStateOf("Localizando...") }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    // --- ESCUCHADOR DE EVENTOS (Aviso de guardado) ---
    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is MapaViewModel.MapaEvent.FotoGuardadaExito -> {
                    Toast.makeText(context, "¡Recuerdo guardado con éxito! 📸", Toast.LENGTH_LONG).show()
                }
                is MapaViewModel.MapaEvent.Error -> {
                    Toast.makeText(context, evento.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Configuración OSMDroid
    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Bitmap personalizado para el puntero GPS (Simplificado para el ejemplo)
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
        canvas.drawPath(path, dirPaint)
        canvas.drawCircle(center, center, radius, pointPaint)
        canvas.drawCircle(center, center, radius, borderPaint)
        bitmap
    }

    // Inicialización del Mapa
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
                    myLocation?.let { viewModel.actualizarUbicacionReal(it.latitude, it.longitude) }
                }
            }
        }
    }

    val lugares by viewModel.lugares.collectAsStateWithLifecycle()
    val cargando by viewModel.estaCargando.collectAsStateWithLifecycle()
    var lugarSeleccionado by remember { mutableStateOf<LugarEntity?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarLugares() }

    // Geocoder para nombre de ubicación
    LaunchedEffect(locationOverlay.myLocation) {
        locationOverlay.myLocation?.let { geoPoint ->
            viewModel.actualizarUbicacionReal(geoPoint.latitude, geoPoint.longitude)
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

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val idLugar = lugarSeleccionado?.id_lugar ?: return@rememberLauncherForActivityResult
            val idUsuario = sessionManager.getUserId()
            scope.launch(Dispatchers.IO) {
                try {
                    val stream = java.io.ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val byteArray = stream.toByteArray()
                    val userIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), idUsuario.toString())
                    val lugarIdBody = RequestBody.create("text/plain".toMediaTypeOrNull(), idLugar.toString())
                    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
                    val bodyImagen = MultipartBody.Part.createFormData("file", "foto_${System.currentTimeMillis()}.jpg", requestFile)
                    viewModel.subirImagenConDatos(userIdBody, lugarIdBody, bodyImagen, "Texto del recuerdo")
                } catch (e: Exception) { Log.e("LocusDebug", "Error: ${e.message}") }
            }
        }
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
                        icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.apply {
                            setTint(android.graphics.Color.parseColor("#E6673D"))
                        }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        infoWindow = null
                        setOnMarkerClickListener { _, _ ->
                            val userPos = locationOverlay.myLocation
                            val radioReal = lugar.radio_activacion ?: 100
                            if (userPos != null) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(userPos.latitude, userPos.longitude, lugar.latitud, lugar.longitud, results)
                                if (results[0].toInt() <= radioReal) {
                                    lugarSeleccionado = lugar
                                    viewModel.seleccionarLugar(lugar) // Vital para la subida
                                    showBottomSheet = true
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("👣 Te faltan ${results[0].toInt() - radioReal}m para desbloquear.") }
                                }
                            } else {
                                lugarSeleccionado = lugar
                                viewModel.seleccionarLugar(lugar)
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

        // BARRA SUPERIOR
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(30.dp),
                color = LocusSurfaceWhite,
                shadowElevation = 10.dp
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Explorador Locus", fontWeight = FontWeight.Bold, color = LocusDeepPurple, fontSize = 14.sp)
                        Text(nombreUbicacion, color = Color.Gray, fontSize = 11.sp)
                    }
                    IconButton(onClick = onNavigateToPerfil) { Icon(Icons.Default.Person, null, tint = LocusActionOrange) }
                }
            }
        }

        // BOTÓN GPS
        FloatingActionButton(
            onClick = { locationOverlay.myLocation?.let { mapView.controller.animateTo(it) } },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            containerColor = LocusSurfaceWhite,
            contentColor = LocusActionOrange,
            shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.MyLocation, null) }

        // MENÚ BURBUJA
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Column(
                    modifier = Modifier.offset(y = animY).alpha(alphaAnim),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(onClick = { menuExpandido = false; onNavigateToFavoritos() }, modifier = Modifier.size(56.dp), containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape) { Icon(Icons.Default.Favorite, "Favoritos") }
                    FloatingActionButton(onClick = { menuExpandido = false; onNavigateToRecuerdos() }, modifier = Modifier.size(56.dp), containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape) { Icon(Icons.Default.Collections, "Mis Recuerdos") }
                    FloatingActionButton(onClick = { menuExpandido = false; onNavigateToEstadisticas() },modifier = Modifier.size(56.dp), containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape) { Icon(Icons.Default.Leaderboard, "Estadisticas") }
                    Spacer(modifier = Modifier.height(68.dp))
                }
                FloatingActionButton(
                    onClick = { menuExpandido = !menuExpandido },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (menuExpandido) LocusActionOrange else LocusSurfaceWhite,
                    contentColor = if (menuExpandido) Color.White else LocusActionOrange,
                    shape = CircleShape
                ) {
                    Icon(imageVector = if (menuExpandido) Icons.Default.Close else Icons.Default.Menu, contentDescription = null, modifier = Modifier.rotate(if (menuExpandido) 90f else 0f))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 80.dp)
        )

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().background(LocusBackground.copy(alpha = 0.8f)).zIndex(10f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(180.dp))
                    Text("Buscando historias...", color = LocusDeepPurple, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
            }
        }

        if (showBottomSheet && lugarSeleccionado != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = LocusBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = LocusActionOrange) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(lugarSeleccionado?.titulo_ficha ?: "Lugar", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                        val esFavorito = lugarSeleccionado?.isFavorite ?: false
                        IconButton(onClick = { lugarSeleccionado?.id_lugar?.let { viewModel.toggleFavorito(it) } }) {
                            Icon(if (esFavorito) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, null, tint = if (esFavorito) LocusActionOrange else Color.Gray)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(lugarSeleccionado?.descripcion_hist ?: "", style = MaterialTheme.typography.bodyLarge, color = LocusDeepPurple.copy(alpha = 0.8f))
                    Spacer(Modifier.height(24.dp))
                    Surface(color = LocusActionOrange.copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, null, tint = LocusActionOrange, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Dato Curioso", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LocusActionOrange)
                                Text(lugarSeleccionado?.dato_curioso ?: "¡Explora más!", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { showBottomSheet = false; cameraLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text("Capturar Recuerdo", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}