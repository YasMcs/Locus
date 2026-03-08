package com.starcode.locus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CameraAlt
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
import com.starcode.locus.R


@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegistro: () -> Unit // Nuevo parámetro
) {
    // Definición de colores según tu diseño
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
    ) {
        // 1. Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.locus_illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )

        // 2. Capa de Degradado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.Center)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            LocusBackground.copy(alpha = 0.6f),
                            LocusBackground
                        )
                    )
                )
        )

        // 3. Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.9f))

            // Título y Eslogan
            Text(
                text = "Locus",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = LocusDeepPurple,
                    letterSpacing = (-2).sp
                )
            )

            Text(
                text = "Cultura al instante. camina, descubre y aprende.",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Fila de tarjetas (Feature Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.NotificationsActive,
                    text = "Recibe\nalertas",
                    backgroundColor = Color.White,
                    iconColor = LocusActionOrange
                )
                FeatureCard(
                    icon = Icons.Default.LocationOn,
                    text = "Descubre\nlugares",
                    backgroundColor = Color.White,
                    iconColor = LocusActionOrange
                )
                FeatureCard(
                    icon = Icons.Default.Explore,
                    text = "Explora la\nhistoria",
                    backgroundColor = Color.White,
                    iconColor = LocusActionOrange
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN PRINCIPAL (Registro + Edad) ---
            Button(
                onClick = onNavigateToRegistro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange)
            ) {
                Text(
                    text = "Comenzar a explorar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- BOTÓN SECUNDARIO (Login Directo) ---
            OutlinedButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LocusActionOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LocusActionOrange)
            ) {
                Text(
                    text = "Ya tengo una cuenta",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- Componente FeatureCard (Fuera de la WelcomeScreen para que weight funcione) ---
//
@Composable
fun RowScope.FeatureCard(
    icon: ImageVector,
    text: String,
    backgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(0.9f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 14.sp
                )
            )
        }
    }
}