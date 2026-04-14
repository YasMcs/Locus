package com.starcode.locus.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import com.starcode.locus.R
import com.starcode.locus.data.entities.LugarEntity
import com.starcode.locus.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
val LocusActionOrange = Color(0xFFE6673D)
val LocusBackground = Color(0xFFFDF6EE)
val LocusDeepPurple = Color(0xFF1D1B20)
val LocusSurfaceWhite = Color(0xFFFFFFFF)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    viewModel: MapaViewModel,
    onNavigateBack: () -> Unit
) {
    val lugares by viewModel.lugares.collectAsStateWithLifecycle()
    val favoritos = remember(lugares) { lugares.filter { it.isFavorite } }

    Scaffold(
        containerColor = LocusBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MIS FAVORITOS",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- ENCABEZADO ESTILO DASHBOARD ---
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "Lugares Guardados",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LocusDeepPurple
                )
                Text(
                    text = if (favoritos.isNotEmpty()) "Tienes ${favoritos.size} destinos en tu lista de deseos."
                    else "Explora el mapa y guarda tus lugares preferidos.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (favoritos.isEmpty()) {
                    EmptyStateFavoritos()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                        )
                    ) {
                        items(favoritos, key = { it.id_lugar }) { lugar ->
                            CardFavorito(lugar = lugar, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardFavorito(
    lugar: LugarEntity,
    viewModel: MapaViewModel
) {
    val scope = rememberCoroutineScope()
    var mostrarAnim by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = mostrarAnim,
        enter = scaleIn(animationSpec = tween(400)) + fadeIn(),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically()
    ) {
        // Usamos Surface para mantener la consistencia de sombras de Locus
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp), // Radio unificado Locus
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador visual izquierdo (Acento de color)
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(60.dp)
                        .clip(CircleShape)
                        .background(LocusActionOrange)
                )

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lugar.nombre_lugar,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocusDeepPurple,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = lugar.titulo_ficha ?: "Punto de interés",
                        fontSize = 12.sp,
                        color = LocusActionOrange,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = lugar.descripcion_hist ?: "",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Botón de favorito con feedback visual
                IconButton(
                    onClick = {
                        scope.launch {
                            mostrarAnim = false
                            delay(300)
                            viewModel.toggleFavorito(lugar.id_lugar)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(LocusBackground, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = LocusActionOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun EmptyStateFavoritos() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Asegúrate de que el archivo R.raw.doggy existe en tu carpeta res/raw
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Tu baúl está vacío",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = LocusDeepPurple
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Los lugares que guardes aparecerán aquí para que los visites cuando quieras.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}