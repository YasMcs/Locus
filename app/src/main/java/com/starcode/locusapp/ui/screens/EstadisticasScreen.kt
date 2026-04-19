package com.starcode.locusapp.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.starcode.locusapp.ui.components.LocusDoggyState
import com.starcode.locusapp.ui.viewmodels.EstadisticasViewModel
import com.starcode.locusapp.ui.viewmodels.MapaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel,
    mapaViewModel: MapaViewModel, 
    onNavigateToMapa: () -> Unit,
    onNavigateToRecuerdos: () -> Unit,
    onNavigateToFavoritos: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    val visitados by viewModel.lugaresVisitados.collectAsState()

    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusOrange = Color(0xFFE6673D)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    LaunchedEffect(Unit) { viewModel.cargarTodo() }

    Scaffold(
        containerColor = LocusBackground,
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
                            color = LocusOrange,
                            onClick = onNavigateToFavoritos
                        )
                        NavBarItem(
                            icon = Icons.Outlined.PhotoLibrary,
                            iconSelected = Icons.Rounded.PhotoLibrary,
                            label = "Recuerdos",
                            color = LocusOrange,
                            onClick = onNavigateToRecuerdos
                        )
                        NavBarItem(
                            icon = Icons.Outlined.Map,
                            iconSelected = Icons.Rounded.Map,
                            label = "Explorar",
                            color = LocusOrange,
                            onClick = onNavigateToMapa
                        )
                        NavBarItem(
                            icon = Icons.Outlined.EmojiEvents,
                            iconSelected = Icons.Rounded.EmojiEvents,
                            label = "Logros",
                            color = LocusOrange,
                            isSelected = true,
                            onClick = { /* Ya estamos aquí */ }
                        )
                        NavBarItem(
                            icon = Icons.Outlined.Person,
                            iconSelected = Icons.Rounded.Person,
                            label = "Perfil",
                            color = LocusOrange,
                            onClick = onNavigateToPerfil
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (estaCargando) {
            LocusDoggyState(message = "Analizando tus aventuras...")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // ✅ Espaciado uniforme
            ) {
                Spacer(Modifier.height(4.dp))

                // ENCABEZADO
                Column {
                    Text(
                        text = "¡Hola, Explorador!", 
                        fontSize = 28.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = LocusDeepPurple
                    )
                    Text(
                        text = "Resumen real de tus aventuras.", 
                        fontSize = 14.sp, 
                        color = Color.Gray
                    )
                }

                // KM REALES (TARJETA GRANDE)
                BigStatCard(
                    title = "Distancia Real",
                    value = String.format("%.2f", stats.kmRecorridos),
                    unit = "km",
                    icon = Icons.Outlined.Moving,
                    gradient = listOf(LocusOrange, Color(0xFFFF8A65))
                )

                // FILA DE TARJETAS MEDIANAS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MediumStatCard(
                        title = "Lugares",
                        value = "${stats.lugaresVisitados}",
                        subtitle = "Visitados",
                        icon = Icons.Outlined.LocationOn,
                        color = Color(0xFFFF8A65),
                        modifier = Modifier.weight(1f)
                    )
                    MediumStatCard(
                        title = "Pasos",
                        value = "${stats.totalPasos}",
                        subtitle = "Caminados",
                        icon = Icons.Outlined.Hiking,
                        color = Color(0xFFFF8A65),
                        modifier = Modifier.weight(1f)
                    )
                }

                // RANGO DINÁMICO
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Color(0xFFFFD600).copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Stars, null, tint = Color(0xFFFFD600), modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Rango Locus", fontSize = 12.sp, color = Color.Gray)
                            Text(stats.categoriaRango, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocusDeepPurple)
                        }
                    }
                }

                // DIARIO DE EXPLORACIÓN
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Mi Diario de Exploración",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocusDeepPurple
                    )

                    if (visitados.isEmpty()) {
                        LocusDoggyState(
                            message = "Aún no has descubierto tesoros.",
                            modifier = Modifier.fillMaxWidth().height(240.dp)
                        )
                    } else {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                visitados.forEachIndexed { index, lugar ->
                                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(24.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(LocusOrange, CircleShape)
                                            )
                                            if (index < visitados.size - 1) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .fillMaxHeight()
                                                        .background(LocusOrange.copy(alpha = 0.2f))
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(
                                            modifier = Modifier
                                                .padding(bottom = 24.dp)
                                                .fillMaxWidth()
                                        ) {
                                            Text(
                                                text = lugar.titulo_ficha ?: lugar.nombre_lugar ?: "Lugar explorado",
                                                fontWeight = FontWeight.Bold,
                                                color = LocusDeepPurple,
                                                fontSize = 16.sp
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Event,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                val fechaLimpia = lugar.fecha_visita?.split("T")?.get(0) ?: "Recientemente"
                                                Text(
                                                    text = "Visitado el: $fechaLimpia",
                                                    color = Color.Gray,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp)) // Espacio final para Scroll
            }
        }
    }
}

@Composable
fun BigStatCard(title: String, value: String, unit: String, icon: ImageVector, gradient: List<Color>) {
    Surface(
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.background(Brush.linearGradient(gradient)).padding(24.dp)) {
            Column {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(12.dp))
                Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 15.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(8.dp))
                    Text(unit, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
                }
            }
        }
    }
}

@Composable
fun MediumStatCard(title: String, value: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
            Text(subtitle, fontSize = 11.sp, color = Color.Gray)
        }
    }
}