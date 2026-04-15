package com.starcode.locus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.starcode.locus.ui.viewmodels.EstadisticasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel,
    onNavigateBack: () -> Unit
) {
    val stats by viewModel.stats.collectAsState()
    val estaCargando by viewModel.estaCargando.collectAsState()
    val visitados by viewModel.lugaresVisitados.collectAsState() // ✅ Recogemos la lista del ViewModel

    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusOrange = Color(0xFFE6673D)

    // Cargamos tanto las stats como el historial al iniciar
    LaunchedEffect(Unit) { viewModel.cargarTodo() }

    Scaffold(
        containerColor = LocusBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("MI PROGRESO", fontWeight = FontWeight.Black, color = LocusDeepPurple)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = LocusDeepPurple)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (estaCargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LocusOrange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // CABECERA
                Column {
                    Text("¡Hola, Explorador!", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = LocusDeepPurple)
                    Text("Resumen real de tus aventuras.", fontSize = 14.sp, color = Color.Gray)
                }

                // KM REALES
                BigStatCard(
                    title = "Distancia Real",
                    value = String.format("%.2f", stats.kmRecorridos),
                    unit = "km",
                    icon = Icons.Default.Moving,
                    gradient = listOf(LocusOrange, Color(0xFFFF8A65))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MediumStatCard(
                        title = "Lugares",
                        value = "${stats.lugaresVisitados}",
                        subtitle = "Visitados",
                        icon = Icons.Default.LocationOn,
                        color = Color(0xFFFF8A65),
                        modifier = Modifier.weight(1f)
                    )
                    MediumStatCard(
                        title = "Pasos",
                        value = "${stats.totalPasos}",
                        subtitle = "Caminados",
                        icon = Icons.Default.Hiking,
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
                        Icon(Icons.Default.Stars, null, tint = Color(0xFFFFD600), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Rango Locus", fontSize = 12.sp, color = Color.Gray)
                            Text(stats.categoriaRango, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LocusDeepPurple)
                        }
                    }
                }

                // ✅ SECCIÓN: HISTORIAL DE LUGARES VISITADOS (Línea del Tiempo)
                Text(
                    text = "Mi Diario de Exploración",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocusDeepPurple,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                if (visitados.isEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Aún no has descubierto tesoros.",
                            color = Color.Gray,
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center
                        )
                    }
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

                                    // --- COLUMNA DE LA LÍNEA DEL TIEMPO ---
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

                                    // --- CONTENIDO DEL LUGAR ---
                                    Column(
                                        modifier = Modifier
                                            .padding(bottom = 24.dp)
                                            .fillMaxWidth()
                                    ) {
                                        Text(
                                            // Si titulo_ficha es nulo, usamos el nombre que viene del back
                                            // (Incluso si no está en la Entity, Kotlin lo maneja así si el back lo manda en el JSON)
                                            text = lugar.titulo_ficha ?: lugar.nombre_lugar ?: "Lugar explorado",                                            fontWeight = FontWeight.Bold,
                                            color = LocusDeepPurple,
                                            fontSize = 16.sp
                                        )

                                        // Fila para la Fecha
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Spacer(Modifier.width(6.dp))
                                            // 2. Lógica para la Fecha (Buscando el campo directamente)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Event,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))

                                                // Dentro de tu forEach de lugares visitados:
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

                Spacer(modifier = Modifier.height(20.dp))
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
                Spacer(Modifier.height(16.dp))
                Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(value, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
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
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(16.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
    }
}