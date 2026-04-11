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
    // Usamos collectAsStateWithLifecycle para mayor eficiencia en el ciclo de vida
    val lugares by viewModel.lugares.collectAsStateWithLifecycle()
    val favoritos = remember(lugares) { lugares.filter { it.isFavorite } }

    Scaffold(
        containerColor = LocusBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Mis Favoritos", // Ajustado de "Recuerdos" a "Favoritos"
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
                ),
                // Padding automático para el Notch
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (favoritos.isEmpty()) {
                EmptyStateFavoritos()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    // windowInsetsPadding para que el contenido no quede bajo la barra de gestos
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        top = 16.dp,
                        end = 24.dp,
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp, // Bajado un poco para que sea más elegante
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = LocusActionOrange,
                    spotColor = LocusDeepPurple.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(24.dp),
            color = LocusSurfaceWhite
        ) {
            Column {
                // Línea decorativa superior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(LocusActionOrange)
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = lugar.nombre_lugar,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LocusDeepPurple,
                                lineHeight = 22.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            // Badge de categoría
                            Surface(
                                color = LocusBackground,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = lugar.titulo_ficha ?: "Exploración",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LocusActionOrange
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    mostrarAnim = false
                                    delay(300)
                                    viewModel.toggleFavorito(lugar.id_lugar)
                                }
                            },
                            modifier = Modifier
                                .background(LocusBackground, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = LocusActionOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = lugar.descripcion_hist ?: "",
                        fontSize = 14.sp,
                        color = LocusDeepPurple.copy(alpha = 0.6f),
                        lineHeight = 20.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.doggy))
        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(220.dp)
        )

        Text(
            "Tu baúl está vacío",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LocusDeepPurple
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Los lugares que guardes aparecerán aquí para que los visites cuando quieras.",
            fontSize = 15.sp,
            color = LocusDeepPurple.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}