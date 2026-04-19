package com.starcode.locusapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starcode.locusapp.data.entities.LugarEntity
import com.starcode.locusapp.ui.components.LocusDoggyState
import com.starcode.locusapp.ui.viewmodels.MapaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.starcode.locusapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    viewModel: MapaViewModel,
    onNavigateToMapa: () -> Unit,
    onNavigateToRecuerdos: () -> Unit,
    onNavigateToPerfil: () -> Unit,
    onNavigateToEstadisticas: () -> Unit
) {
    val lugares by viewModel.lugares.collectAsStateWithLifecycle()
    val favoritos = remember(lugares) { lugares.filter { it.isFavorite } }

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    Scaffold(
        containerColor = LocusBackground,
        // ✅ TopBar eliminada para cumplir con el diseño minimalista
        bottomBar = {
            Box(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(72.dp),
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
                            isSelected = true,
                            onClick = { /* Ya estamos aquí */ }
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
                            onClick = onNavigateToMapa
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.statusBars) // Mantiene el contenido debajo de la barra de estado
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "Tus lugares favoritos",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LocusDeepPurple
                )
                Text(
                    text = if (favoritos.isNotEmpty()) "Tienes ${favoritos.size} destinos en tu lista de favoritos."
                    else "Explora el mapa y guarda tus lugares preferidos.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (favoritos.isEmpty()) {
                    LocusDoggyState(message = "Tu baúl está vacío. Los lugares que guardes aparecerán aquí.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            top = 8.dp,
                            bottom = 100.dp
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

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)

    AnimatedVisibility(
        visible = mostrarAnim,
        enter = scaleIn(animationSpec = tween(400)) + fadeIn(),
        exit = fadeOut(animationSpec = tween(300)) + slideOutVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = LocusActionOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}