package com.starcode.locus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.* // Agregamos runtime para los estados
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PerfilScreen(onLogout: () -> Unit, onBack: () -> Unit) {
    // ESTADO: Controla si se muestra el aviso de confirmación
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusDark = Color(0xFF333333)

    // --- DIÁLOGO DE CONFIRMACIÓN ---
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            containerColor = LocusBackground,
            title = {
                Text(
                    "¿Cerrar sesión?",
                    fontWeight = FontWeight.Bold,
                    color = LocusDark
                )
            },
            text = {
                Text(
                    "Tendrás que volver a ingresar tus credenciales para explorar las historias de Suchiapa.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(onClick = onLogout) {
                    Text("SÍ, SALIR", color = Color(0xFFD32F2F), fontWeight = FontWeight.ExtraBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("CANCELAR", color = LocusDark)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera con botón atrás
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = LocusDark)
            }
            Text(
                text = "Mi Perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LocusDark
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Avatar representativo
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(LocusActionOrange.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(90.dp),
                tint = LocusActionOrange
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Texto informativo (puedes hacerlo dinámico después)
        Text(
            text = "Explorador Locus",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = LocusDark
        )
        Text(
            text = "Suchiapa, Chiapas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

        // Botón de Cerrar Sesión
        Button(
            onClick = { mostrarConfirmacion = true }, // Ahora activa el diálogo primero
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}