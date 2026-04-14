package com.starcode.locus.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusErrorRed = Color(0xFFB00020)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)
    val context = LocalContext.current

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = LocusDeepPurple,
        unfocusedTextColor = LocusDeepPurple,
        focusedContainerColor = LocusSurfaceWhite,
        unfocusedContainerColor = LocusSurfaceWhite,
        focusedBorderColor = LocusActionOrange,
        unfocusedBorderColor = Color(0xFFD1D1D1).copy(alpha = 0.5f),
        errorBorderColor = LocusErrorRed,
        focusedLabelColor = LocusActionOrange
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime))
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Locus (Ajustado a un tamaño más elegante)
            Image(
                painter = painterResource(id = com.starcode.locus.R.drawable.locuslogo),
                contentDescription = "Logo Locus",
                modifier = Modifier.size(160.dp)
            )

            Text(
                text = "¡Hola de nuevo!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = LocusDeepPurple,
                    letterSpacing = (-1).sp
                )
            )

            Text(
                text = "Ingresa tus datos para continuar explorando",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- CONTENEDOR DEL FORMULARIO ---
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 4.dp, // Elevación para dar profundidad
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // CAMPO: EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; emailError = false },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        isError = emailError || authState is AuthResult.Error,
                        colors = customTextFieldColors
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // CAMPO: CONTRASEÑA
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it; passError = false },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passError || authState is AuthResult.Error,
                        colors = customTextFieldColors
                    )

                    if (authState is AuthResult.Error) {
                        Text(
                            text = "Credenciales incorrectas",
                            color = LocusErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN ENTRAR (Estilo Premium) ---
            Button(
                onClick = {
                    emailError = email.isBlank()
                    passError = pass.isBlank()
                    if (!emailError && !passError) {
                        onLogin(email, pass)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                enabled = authState !is AuthResult.Loading
            ) {
                if (authState is AuthResult.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Iniciar Sesión", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onIrARegistrar) {
                Row {
                    Text("¿Aún no eres explorador? ", color = Color.Gray)
                    Text("Regístrate", color = LocusActionOrange, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}