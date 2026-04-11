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
                        "Mis Recuerdos",
                        fontWeight = FontWeight.Black,
                        color = LocusDeepPurple
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = LocusDeepPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = LocusBackground
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    Text(
                        "Buscando tus momentos...",
                        color = LocusDeepPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (imagenes.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Text("Aún no tienes recuerdos", color = Color.Gray)

                    TextButton(onClick = { viewModel.cargarRecuerdos() }) {
                        Text("Actualizar galería", color = LocusActionOrange)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imagenes) { foto ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { imagenSeleccionada = foto },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            AsyncImage(
                                model = foto.url_imagen,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // --- DIÁLOGO PARA VER EN GRANDE ---
            if (imagenSeleccionada != null) {
                Dialog(
                    onDismissRequest = { imagenSeleccionada = null },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        decorFitsSystemWindows = false
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        AsyncImage(
                            model = imagenSeleccionada?.url_imagen,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )

                        // Gradiente superior para visibilidad de texto
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)))
                                .align(Alignment.TopCenter)
                        )

                        // Información del lugar
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(top = 16.dp, start = 20.dp, end = 60.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = LocusActionOrange, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = imagenSeleccionada?.nombre_lugar ?: "Lugar Explorador",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            val fechaCorta = imagenSeleccionada?.fecha_subida?.split("T")?.get(0) ?: "Reciente"
                            Text(
                                text = "Capturado el $fechaCorta",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 28.dp)
                            )
                        }

                        // Botón Cerrar
                        IconButton(
                            onClick = { imagenSeleccionada = null },
                            modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(top = 8.dp, end = 10.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(30.dp))
                        }

                        // Botón Descargar
                        Button(
                            onClick = { descargarImagen(context, imagenSeleccionada?.url_imagen ?: "") },
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(bottom = 20.dp)
                                .height(56.dp)
                                .fillMaxWidth(0.7f)
                                .align(Alignment.BottomCenter),
                            colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(Icons.Default.Download, null, tint = Color.White)
                            Spacer(Modifier.width(10.dp))
                            Text("GUARDAR EN GALERÍA", fontWeight = FontWeight.Bold, color = Color.White)
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

        Toast.makeText(context, "Descarga iniciada", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}