package com.starcode.locus.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.starcode.locus.R
import com.starcode.locus.data.remote.request.ImagenResponse
import com.starcode.locus.ui.viewmodels.RecuerdosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuerdosScreen(
    viewModel: RecuerdosViewModel,
    onNavigateBack: () -> Unit
) {
    val imagenes by viewModel.imagenes.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    var imagenSeleccionada by remember { mutableStateOf<ImagenResponse?>(null) }
    val context = LocalContext.current

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    LaunchedEffect(Unit) {
        viewModel.cargarRecuerdos()
    }


    Scaffold(
        containerColor = LocusBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MIS MOMENTOS",
                        fontWeight = FontWeight.Black,
                        color = LocusDeepPurple,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Regresar", tint = LocusDeepPurple)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- ENCABEZADO ESTILO DASHBOARD (Elimina el vacío visual) ---
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "Tu Galería Locus",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LocusDeepPurple
                )
                Text(
                    text = if (imagenes.isNotEmpty()) "Has capturado ${imagenes.size} recuerdos inolvidables."
                    else "Tu historia está esperando ser escrita.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (estaCargando) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(160.dp)
                        )
                        Text("Buscando tus momentos...", color = LocusDeepPurple, fontWeight = FontWeight.Bold)
                    }
                } else if (imagenes.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(12.dp))
                        Text("Aún no tienes recuerdos", color = Color.Gray)
                        TextButton(onClick = { viewModel.cargarRecuerdos() }) {
                            Text("Actualizar galería", color = LocusActionOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // --- GRID DE 2 COLUMNAS (Más grande, llena más la pantalla) ---
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(imagenes) { foto ->
                            Surface(
                                modifier = Modifier
                                    .aspectRatio(0.85f)
                                    .clickable { imagenSeleccionada = foto },
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 4.dp,
                                color = Color.White
                            ) {
                                Box {
                                    AsyncImage(
                                        model = foto.url_imagen,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Overlay inferior para el nombre del lugar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                )
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = foto.nombre_lugar ?: "Lugar",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // --- DIÁLOGO DE VISTA PREVIA (Mantenemos tu lógica funcional pero con estilo) ---
                if (imagenSeleccionada != null) {
                    Dialog(
                        onDismissRequest = { imagenSeleccionada = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                            AsyncImage(
                                model = imagenSeleccionada?.url_imagen,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().align(Alignment.Center),
                                contentScale = ContentScale.Fit
                            )

                            // Header del Dialog
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
                                    .align(Alignment.TopCenter)
                            )

                            Column(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .padding(top = 16.dp, start = 20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = imagenSeleccionada?.nombre_lugar ?: "Lugar Explorador",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                val fechaCorta = imagenSeleccionada?.fecha_subida?.split("T")?.get(0) ?: "Reciente"
                                Text(
                                    text = "Capturado el $fechaCorta",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 30.dp)
                                )
                            }

                            IconButton(
                                onClick = { imagenSeleccionada = null },
                                modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(16.dp)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            // Botón de acción (Download)
                            Button(
                                onClick = { descargarImagen(context, imagenSeleccionada?.url_imagen ?: "") },
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .padding(bottom = 32.dp)
                                    .height(60.dp)
                                    .fillMaxWidth(0.8f)
                                    .align(Alignment.BottomCenter),
                                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                                shape = RoundedCornerShape(30.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                Icon(Icons.Default.Download, null, tint = Color.White)
                                Spacer(Modifier.width(12.dp))
                                Text("GUARDAR EN EL TELÉFONO", fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun descargarImagen(context: Context, url: String) {
    try {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Locus Recuerdo")
            .setDescription("Descargando imagen...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Locus_${System.currentTimeMillis()}.jpg")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}