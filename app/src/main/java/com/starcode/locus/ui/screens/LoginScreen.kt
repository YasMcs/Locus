package com.starcode.locus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starcode.locus.ui.viewmodels.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onIrARegistrar: () -> Unit,
    authState: AuthResult
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passError by remember { mutableStateOf(false) }

    // Paleta de colores Locus con alto contraste
    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusErrorRed = Color(0xFFB00020)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Locus",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = LocusDeepPurple,
                letterSpacing = (-2).sp
            )
        )

        Text(
            text = "Bienvenido de nuevo",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- CAMPO: EMAIL ---
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = false
            },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            isError = emailError || authState is AuthResult.Error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LocusDeepPurple,      // Letra oscura al escribir
                unfocusedTextColor = LocusDeepPurple,    // Letra oscura siempre
                focusedContainerColor = LocusSurfaceWhite, // Fondo blanco para contraste
                unfocusedContainerColor = LocusSurfaceWhite,
                focusedBorderColor = LocusActionOrange,
                unfocusedBorderColor = Color(0xFFD1D1D1),
                errorBorderColor = LocusErrorRed,
                focusedLabelColor = LocusActionOrange,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- CAMPO: CONTRASEÑA ---
        OutlinedTextField(
            value = pass,
            onValueChange = {
                pass = it
                passError = false
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passError || authState is AuthResult.Error,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LocusDeepPurple,
                unfocusedTextColor = LocusDeepPurple,
                focusedContainerColor = LocusSurfaceWhite,
                unfocusedContainerColor = LocusSurfaceWhite,
                focusedBorderColor = LocusActionOrange,
                unfocusedBorderColor = Color(0xFFD1D1D1),
                errorBorderColor = LocusErrorRed,
                focusedLabelColor = LocusActionOrange,
                unfocusedLabelColor = Color.Gray
            )
        )

        if (authState is AuthResult.Error) {
            Text(
                text = "Credenciales incorrectas",
                color = LocusErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp).align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- BOTÓN DE ENTRAR ---
        Button(
            onClick = {
                emailError = email.isBlank()
                passError = pass.isBlank()
                if (!emailError && !passError) onLogin(email, pass)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocusActionOrange,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFCCCCCC),
                disabledContentColor = Color.DarkGray
            ),
            enabled = authState !is AuthResult.Loading
        ) {
            if (authState is AuthResult.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tienes cuenta?", color = Color.Gray)
            TextButton(onClick = onIrARegistrar) {
                Text("Regístrate", color = LocusActionOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}