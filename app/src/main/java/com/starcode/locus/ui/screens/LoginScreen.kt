package com.starcode.locus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onIrARegistrar: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    // Paleta de colores consistente con WelcomeScreen
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground) // Fondo beige "aesthetic"
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título estilizado como en Welcome
            Text(
                text = "Locus",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = LocusDeepPurple,
                    letterSpacing = (-2).sp
                )
            )

            Text(
                text = "Ingresa tus credenciales para continuar",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo de Correo
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocusActionOrange,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedLabelColor = LocusActionOrange
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Contraseña
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LocusActionOrange,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedLabelColor = LocusActionOrange
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Entrar (Naranja para consistencia)
            Button(
                onClick = { onLogin(email, pass) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange)
            ) {
                Text(
                    "Entrar",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enlace a Registro
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿No tienes cuenta?", color = Color.Gray)
                TextButton(onClick = onIrARegistrar) {
                    Text(
                        "Regístrate",
                        color = LocusActionOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}