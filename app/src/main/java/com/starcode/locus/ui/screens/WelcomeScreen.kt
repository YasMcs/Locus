package com.starcode.locus.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
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
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
    // Definición de colores según tu diseño
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
    ) {
        // 1. Imagen de fondo (ocupa la parte superior)
        Image(
            painter = painterResource(id = R.drawable.locus_illustration),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // Un poco más alta para que el degradado tenga espacio
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )

        // 2. Capa de Degradado (El "Fading Edge")
        // Este Box crea la transición suave de transparente a beige
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f) // Cubre la zona media de la pantalla
                .align(Alignment.Center)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            LocusBackground.copy(alpha = 0.6f), // Desvanecimiento intermedio
                            LocusBackground // Beige sólido al final
                        )
                    )
                )
        )

        // 3. Contenido Principal (Textos y Tarjetas)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spacer con weight para empujar el contenido hacia el final del degradado
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
                text = "Cultura al instante. Apunta, descubre, aprende.",
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Fila de tarjetas (Feature Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    icon = Icons.Default.CameraAlt,
                    text = "Escanea\nmonumentos",
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

            Spacer(modifier = Modifier.height(40.dp))

            // Botón de acción naranja
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
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

            Spacer(modifier = Modifier.height(40.dp))
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