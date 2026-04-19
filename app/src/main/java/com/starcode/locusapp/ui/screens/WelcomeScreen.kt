package com.starcode.locusapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starcode.locusapp.R
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
    ) {
        // 1. Ilustración de fondo (Aumentamos el impacto visual)
        Image(
            painter = painterResource(id = R.drawable.locus_illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f) // Un poco menos para dar aire al texto
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )

        // 2. Degradado más profundo (Mejora la legibilidad del texto)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            LocusBackground.copy(alpha = 0.9f),
                            LocusBackground
                        )
                    )
                )
        )

        // 3. Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 28.dp), // Un poco más de margen lateral
            horizontalAlignment = Alignment.Start // Alineación natural de lectura
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Título con estilo Premium
            Text(
                text = "Locus",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black, // Más peso
                    color = LocusDeepPurple,
                    letterSpacing = (-3).sp
                )
            )

            Text(
                text = "Cultura al instante.\nCamina, descubre y aprende.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Fila de tarjetas (Estilo unificado con el Dashboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.NotificationsActive,
                    text = "Alertas",
                    iconColor = LocusActionOrange
                )
                FeatureCard(
                    icon = Icons.Default.LocationOn,
                    text = "Lugares",
                    iconColor = LocusActionOrange
                )
                FeatureCard(
                    icon = Icons.Default.Explore,
                    text = "Historia",
                    iconColor = LocusActionOrange
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // --- BOTONERÍA (Más alta y redondeada) ---
            Button(
                onClick = onNavigateToRegistro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp), // Altura premium
                shape = RoundedCornerShape(20.dp), // Consistente con las cards
                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "Comenzar aventura",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ya tengo una cuenta. ", color = Color.Gray)
                    Text("Iniciar sesión", color = LocusActionOrange, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RowScope.FeatureCard(
    icon: ImageVector,
    text: String,
    iconColor: Color
) {
    // Usamos Surface para un control más fino de sombras y bordes (como en Stats)
    Surface(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f),
        shape = RoundedCornerShape(24.dp), // Esquinas más suaves (ADN Locus)
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )
        }
    }
}