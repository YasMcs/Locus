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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.starcode.locus.ui.viewmodels.AuthResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onRegistrar: (String, String, String, String, String, String) -> Unit,
    onIrALogin: () -> Unit,
    authState: AuthResult,
    fechaValidada: String
) {
    var nombre by remember { mutableStateOf("") }
    var apePa by remember { mutableStateOf("") }
    var apeMa by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LocusDeepPurple,
        unfocusedTextColor = LocusDeepPurple,
        focusedContainerColor = LocusSurfaceWhite,
        unfocusedContainerColor = LocusSurfaceWhite,
        focusedBorderColor = LocusActionOrange,
        unfocusedBorderColor = Color(0xFFD1D1D1),
        focusedLabelColor = LocusActionOrange
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Locus",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = LocusDeepPurple,
                letterSpacing = (-2).sp
            )
        )
        Text(text = "Crea tu perfil", color = Color.Gray)

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre(s)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = customTextFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = apePa,
                onValueChange = { apePa = it },
                label = { Text("Ap. Paterno") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = apeMa,
                onValueChange = { apeMa = it },
                label = { Text("Ap. Materno") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = customTextFieldColors
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = customTextFieldColors
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (nombre.isNotBlank() && email.contains("@") && pass.length >= 6) {
                    onRegistrar(nombre, apePa, apeMa, fechaValidada, email, pass)
                }
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
                Text("Finalizar registro", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes cuenta?", color = Color.Gray)
            TextButton(onClick = onIrALogin) {
                Text("Inicia sesión", color = LocusActionOrange, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}