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
    sessionManager: SessionManager, // <--- AGREGA ESTO
    onNavigateToPerfil: () -> Unit,
    onNavigateToRecuerdos: () -> Unit // <--- AGREGA ESTA LÍNEA AQUÍ
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

    fun procesarNuevoRecuerdo(bitmap: Bitmap) {
        val idLugar = lugarSeleccionado?.id_lugar ?: return
        val idUsuario = sessionManager.getUserId()

        scope.launch(Dispatchers.IO) {
            try {
                // Comprimir imagen
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()

                val mediaType = "text/plain".toMediaTypeOrNull()

                // 1. IDs (aseguramos que no tengan espacios ni caracteres raros)
                val userIdBody = RequestBody.create(mediaType, idUsuario.toString().trim())
                val lugarIdBody = RequestBody.create(mediaType, idLugar.toString().trim())

                // 2. Imagen (Usamos "file" por compatibilidad universal)
                val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
                val bodyImagen = MultipartBody.Part.createFormData(
                    "file", // <--- Si esto sigue dando 500, pregunta al de backend el nombre exacto del campo
                    "foto_${System.currentTimeMillis()}.jpg",
                    requestFile
                )

                // 3. Envío
                viewModel.subirImagenConDatos(
                    userId = userIdBody,
                    lugarId = lugarIdBody,
                    imagenPart = bodyImagen,
                    nota = "Aquí va el texto del recuerdo" // <-- Pasa la variable que contenga la nota
                )

            } catch (e: Exception) {
                Log.e("LocusDebug", "Error: ${e.message}")
            }
        }
    }
    // 1. El lanzador que recibe la foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            procesarNuevoRecuerdo(bitmap) // La función que creamos antes
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
                IconButton(onClick = onNavigateToPerfil) { Icon(Icons.Default.Person, null, tint = LocusActionOrange) }
            }
        }

        FloatingActionButton(
            onClick = { locationOverlay.myLocation?.let { mapView.controller.animateTo(it) } },
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = RoundedCornerShape(16.dp)
        ) { Icon(Icons.Default.MyLocation, null)}

        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 32.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomCenter) {
                Column(
                    modifier = Modifier.offset(y = animY).alpha(alphaAnim),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Favoritos
                    FloatingActionButton(
                        onClick = {
                            menuExpandido = false
                            // onNavigateToFavoritos() // <--- Si tienes esta ruta
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape
                    ) { Icon(Icons.Default.Favorite, "Favoritos") }

                    // 📸 2. NUEVO: RECUERDOS (GALERÍA)
                    FloatingActionButton(
                        onClick = {
                            menuExpandido = false
                            onNavigateToRecuerdos()
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = LocusSurfaceWhite,
                        contentColor = LocusActionOrange,
                        shape = CircleShape
                    ) { Icon(Icons.Default.Collections, "Mis Recuerdos") }

                    // 3. Historial
                    FloatingActionButton(
                        onClick = {
                            menuExpandido = false
                            // onNavigateToHistorial() // <--- Si tienes esta ruta
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = LocusSurfaceWhite, contentColor = LocusActionOrange, shape = CircleShape
                    ) { Icon(Icons.Default.History, "Historial") }

                    Spacer(modifier = Modifier.height(68.dp))
                }

                FloatingActionButton(
                    onClick = { menuExpandido = !menuExpandido },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (menuExpandido) LocusActionOrange else LocusSurfaceWhite,
                    contentColor = if (menuExpandido) Color.White else LocusActionOrange,
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

// --- FICHA ACTUALIZADA (TÍTULO Y FAVORITO ALINEADOS) ---
        if (showBottomSheet && lugarSeleccionado != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = LocusBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = LocusActionOrange) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 40.dp)
                ) {
                    // 1. CABECERA: TÍTULO Y CORAZÓN EN UNA FILA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = lugarSeleccionado?.titulo_ficha ?: "Lugar",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f) // Esto empuja al corazón a la orilla y respeta el espacio
                        )

                        // Corazón limpio, sin fondo circular
                        IconButton(
                            onClick = { /* TODO: Implementar favoritos */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = LocusActionOrange,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 2. DESCRIPCIÓN
                    Text(
                        text = lugarSeleccionado?.descripcion_hist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = LocusDeepPurple.copy(alpha = 0.8f)
                    )

                    Spacer(Modifier.height(24.dp))

                    // 3. SECCIÓN DATO CURIOSO
                    Surface(
                        color = LocusActionOrange.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LocusActionOrange.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // CAMBIO: Icono en lugar de Emoji
                            Icon(
                                imageVector = Icons.Default.Lightbulb, // O Icons.Default.Info si prefieres algo más sobrio
                                contentDescription = null,
                                tint = LocusActionOrange, // Usamos tu color naranja para que todo combine
                                modifier = Modifier.size(28.dp)
                            )

                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Dato Curioso",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = LocusActionOrange
                                )
                                Text(
                                    text = lugarSeleccionado?.dato_curioso ?: "¡Explora para descubrir más!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // 4. BOTÓN CAPTURAR RECUERDO
                    Button(
                        onClick = {
                            showBottomSheet = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text("Capturar Recuerdo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))

        // --- LOADING DOGGY (IGUAL QUE EN RECUERDOS) ---
        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocusBackground.copy(alpha = 0.8f))
                    .zIndex(10f), // Esto asegura que tape el mapa y botones
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
                        text = "Buscando historias...",
                        color = LocusDeepPurple,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    } // <-- Aquí cierra el Box principal
}