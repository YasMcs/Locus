package com.starcode.locusapp.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.starcode.locusapp.data.entities.LugarEntity
import com.starcode.locusapp.data.remote.SessionManager
import com.starcode.locusapp.ui.components.LocusDoggyState
import com.starcode.locusapp.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import com.starcode.locusapp.R

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
    val uriHandler = LocalUriHandler.current 

    var fotoCapturada by remember { mutableStateOf<Bitmap?>(null) }
    var mostrarConfirmacionFoto by remember { mutableStateOf(false) }
    var mostrarExitoGuardado by remember { mutableStateOf(false) }

    var mostrarModalPrivacidad by remember {
        mutableStateOf(!sessionManager.esPrivacidadAceptada())
    }

    if (mostrarModalPrivacidad) {
        DialogoPrivacidadLocus(
            onAceptar = {
                sessionManager.guardarPrivacidadAceptada(true)
                mostrarModalPrivacidad = false
            },
            onVerMas = {
                uriHandler.openUri("https://sites.google.com/ids.upchiapas.edu.mx/locus-privacy-policy/inicio")
            }
        )
    }

    val statusVisita by viewModel.statusVisita.collectAsStateWithLifecycle()
    val lugares by viewModel.lugares.collectAsStateWithLifecycle()
    val cargando by viewModel.estaCargando.collectAsStateWithLifecycle()
    val nombreUbicacion by viewModel.nombreUbicacion.collectAsStateWithLifecycle()
    val pasosLive: Int by viewModel.pasosLive.collectAsStateWithLifecycle(initialValue = 0)

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    LaunchedEffect(mostrarExitoGuardado) {
        if (mostrarExitoGuardado) {
            delay(3500)
            mostrarExitoGuardado = false
        }
    }

    remember {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val pointerBitmap: Bitmap = remember {
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

    val mapView: MapView = remember { MapView(context) }
    
    val locationOverlay: MyLocationNewOverlay = remember(mapView) {
        val provider = GpsMyLocationProvider(context)
        val overlay = MyLocationNewOverlay(provider, mapView)
        overlay.enableMyLocation()
        overlay.setDrawAccuracyEnabled(false)
        overlay.setPersonIcon(pointerBitmap)
        overlay.setDirectionIcon(pointerBitmap)
        overlay.setPersonAnchor(0.5f, 0.5f)
        overlay.setDirectionAnchor(0.5f, 0.5f)
        overlay.runOnFirstFix {
            (context as? android.app.Activity)?.runOnUiThread {
                mapView.controller.animateTo(overlay.myLocation)
                mapView.controller.setZoom(17.5)
                overlay.myLocation?.let { viewModel.actualizarUbicacionReal(it.latitude, it.longitude) }
            }
        }
        overlay
    }

    var lugarSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.cargarLugares() }

    LaunchedEffect(locationOverlay.myLocation) {
        val geoPoint: GeoPoint? = locationOverlay.myLocation
        if (geoPoint != null) {
            viewModel.actualizarUbicacionReal(geoPoint.latitude, geoPoint.longitude)
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

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoCapturada = bitmap
            mostrarConfirmacionFoto = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView<MapView>(
            factory = { ctx ->
                mapView.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(15.0)
                    overlays.add(locationOverlay)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view: MapView ->
                view.overlays.removeIf { it is Marker }
                lugares.forEach { lugar: LugarEntity ->
                    val marker = Marker(view).apply {
                        position = GeoPoint(lugar.latitud, lugar.longitud)
                        icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.marker_default)?.apply {
                            setTint(android.graphics.Color.parseColor("#E6673D"))
                        }
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        infoWindow = null
                        setOnMarkerClickListener { m: Marker, v: MapView ->
                            val userPos = locationOverlay.myLocation
                            val radioReal = lugar.radio_activacion ?: 100
                            if (userPos != null) {
                                val results = FloatArray(1)
                                android.location.Location.distanceBetween(userPos.latitude, userPos.longitude, lugar.latitud, lugar.longitud, results)
                                if (results[0].toInt() <= radioReal) {
                                    lugarSeleccionadoId = lugar.id_lugar
                                    viewModel.seleccionarLugar(lugar)
                                    showBottomSheet = true
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("👣 Te faltan ${results[0].toInt() - radioReal}m para desbloquear.") }
                                }
                            } else {
                                lugarSeleccionadoId = lugar.id_lugar
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .zIndex(30f),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(
                visible = mostrarExitoGuardado, 
                enter = slideInVertically() + fadeIn(), 
                exit = slideOutVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF4CAF50),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "¡Recuerdo guardado con éxito!", 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .zIndex(20f),
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedVisibility(visible = statusVisita != null, enter = fadeIn(), exit = fadeOut()) {
                statusVisita?.let { mensaje ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LocusDeepPurple,
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = mensaje,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

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
                Row(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Explore,
                            contentDescription = null,
                            tint = LocusActionOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "EXPLORANDO",
                                fontWeight = FontWeight.ExtraBold,
                                color = LocusActionOrange,
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                nombreUbicacion,
                                color = LocusDeepPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LocusActionOrange.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { onNavigateToEstadisticas() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Hiking,
                                contentDescription = null,
                                tint = LocusActionOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "$pasosLive",
                                fontWeight = FontWeight.Black,
                                color = LocusActionOrange,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { locationOverlay.myLocation?.let { mapView.controller.animateTo(it) } },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 16.dp),
            containerColor = LocusSurfaceWhite,
            contentColor = LocusActionOrange,
            shape = RoundedCornerShape(16.dp)
        ) { 
            Icon(Icons.Outlined.NearMe, null) 
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(36.dp),
                color = LocusSurfaceWhite,
                shadowElevation = 15.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavBarItem(
                        icon = Icons.Outlined.FavoriteBorder,
                        iconSelected = Icons.Rounded.Favorite,
                        label = "Favoritos",
                        color = LocusActionOrange,
                        onClick = onNavigateToFavoritos
                    )
                    NavBarItem(
                        icon = Icons.Outlined.PhotoLibrary,
                        iconSelected = Icons.Rounded.PhotoLibrary,
                        label = "Recuerdos",
                        color = LocusActionOrange,
                        onClick = onNavigateToRecuerdos
                    )
                    NavBarItem(
                        icon = Icons.Outlined.Map,
                        iconSelected = Icons.Rounded.Map,
                        label = "Explorar",
                        color = LocusActionOrange,
                        isSelected = true,
                        onClick = { /* Estamos aquí */ }
                    )
                    NavBarItem(
                        icon = Icons.Outlined.EmojiEvents,
                        iconSelected = Icons.Rounded.EmojiEvents,
                        label = "Logros",
                        color = LocusActionOrange,
                        onClick = onNavigateToEstadisticas
                    )
                    NavBarItem(
                        icon = Icons.Outlined.Person,
                        iconSelected = Icons.Rounded.Person,
                        label = "Perfil",
                        color = LocusActionOrange,
                        onClick = onNavigateToPerfil
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize().background(LocusBackground.copy(alpha = 0.8f)).zIndex(10f), contentAlignment = Alignment.Center) {
                LocusDoggyState(message = "Buscando historias...")
            }
        }

        if (showBottomSheet && lugarSeleccionadoId != null) {
            val lugarActual = lugares.find { it.id_lugar == lugarSeleccionadoId }
            
            if (lugarActual != null) ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                containerColor = LocusBackground,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                dragHandle = { BottomSheetDefaults.DragHandle(color = LocusActionOrange.copy(alpha = 0.4f)) },
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp) // Un poco más de aire a los lados
                        .padding(bottom = 48.dp, top = 8.dp)
                        .verticalScroll(rememberScrollState()) // Por si la descripción es larga
                ) {
                    // --- ENCABEZADO: Título y Favorito ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lugarActual.nombre_lugar,
                                style = MaterialTheme.typography.headlineMedium, // Más grande y elegante
                                fontWeight = FontWeight.Bold,
                                color = LocusDeepPurple,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = lugarActual.titulo_ficha ?: "Punto de interés",
                                style = MaterialTheme.typography.labelLarge,
                                color = LocusActionOrange,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        val esFavorito = lugarActual.isFavorite
                        IconButton(
                            onClick = { viewModel.toggleFavorito(lugarActual.id_lugar) },
                            modifier = Modifier.background(
                                color = if (esFavorito) LocusActionOrange.copy(alpha = 0.1f) else Color.Transparent,
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = if (esFavorito) Icons.Rounded.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (esFavorito) LocusActionOrange else LocusDeepPurple.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- DESCRIPCIÓN HISTÓRICA ---
                    Text(
                        text = lugarActual.descripcion_hist ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = LocusDeepPurple.copy(alpha = 0.7f), // Color más suave para que sea premium
                        lineHeight = 26.sp, // Más espacio entre líneas para lectura fácil
                        textAlign = TextAlign.Justify
                    )

                    Spacer(Modifier.height(32.dp))

                    // --- TARJETA DE DATO CURIOSO (REDISEÑADA) ---
                    Surface(
                        color = LocusActionOrange.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, LocusActionOrange.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(LocusActionOrange.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = LocusActionOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "DATO CURIOSO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = LocusActionOrange,
                                    letterSpacing = 2.sp
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = lugarActual.dato_curioso ?: "¡Explora más!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 20.sp
                                    ),
                                    color = LocusDeepPurple.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(40.dp))

                    // --- BOTÓN DE ACCIÓN PRINCIPAL ---
                    Button(
                        onClick = {
                            showBottomSheet = false
                            cameraLauncher.launch(null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp), // Un poco más alto para que sea fácil de tocar
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Capturar Recuerdo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (mostrarConfirmacionFoto && fotoCapturada != null) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacionFoto = false; fotoCapturada = null },
                title = { 
                    Text("¿Guardar este recuerdo?", fontWeight = FontWeight.Black, color = Color.Black) 
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 8.dp
                        ) {
                            Image(
                                bitmap = fotoCapturada!!.asImageBitmap(),
                                contentDescription = "Vista previa",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Esta foto se guardará en tu galería de recuerdos asociada a este lugar.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bitmap = fotoCapturada ?: return@Button
                            val idLugar = lugarSeleccionadoId ?: return@Button
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
                                    
                                    withContext(Dispatchers.Main) {
                                        mostrarConfirmacionFoto = false
                                        fotoCapturada = null
                                        mostrarExitoGuardado = true 
                                    }
                                } catch (e: Exception) { 
                                    Log.e("LocusDebug", "Error subiendo: ${e.message}") 
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sí, Guardar", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacionFoto = false; fotoCapturada = null }) {
                        Text("Descartar", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun DialogoPrivacidadLocus(
    onAceptar: () -> Unit,
    onVerMas: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                "Términos y Condiciones de Privacidad",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = Color(0xFF1D1B20)
            )
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 300.dp)) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Bienvenido a Locus. Para funcionar correctamente, procesamos los siguientes datos:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "1. Ubicación en Segundo Plano: Recolectamos coordenadas incluso cuando la app no está en uso para activar alertas de monumentos cercanos.\n\n" +
                                "2. Datos de Actividad: Usamos el podómetro para calcular tu progreso y nivel de explorador.\n\n" +
                                "3. Almacenamiento de Recuerdos: Las fotos que captures se asocian a tu ubicación para crear tu historial.\n\n" +
                                "4. Seguridad: Tus datos están encriptados y no se venden a terceros bajo ninguna circunstancia.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Justify,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = onVerMas,
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        Text("Ver política completa y detallada en nuestro sitio oficial",
                            fontSize = 13.sp,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                            color = Color(0xFFE6673D))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAceptar,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6673D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("He leído y acepto los términos", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}