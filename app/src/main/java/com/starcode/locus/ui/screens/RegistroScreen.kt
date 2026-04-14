package com.starcode.locus.ui.screens

import android.widget.Toast
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
import com.starcode.locus.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onIrALogin: () -> Unit,
    fechaValidada: String
) {
    var nombre by remember { mutableStateOf("") }
    var apePa by remember { mutableStateOf("") }
    var apeMa by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Paleta de colores Locus
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
        unfocusedBorderColor = Color(0xFFD1D1D1).copy(alpha = 0.5f),
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
                .padding(horizontal = 24.dp) // Unificado a 24dp como las otras pantallas
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo más estilizado
            Image(
                painter = painterResource(id = com.starcode.locus.R.drawable.locuslogo),
                contentDescription = "Logo Locus",
                modifier = Modifier.size(120.dp)
            )

            Text(
                text = "Crea tu cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = LocusDeepPurple,
                    letterSpacing = (-1).sp
                )
            )

            Text(
                text = "Estás a un paso de comenzar tu aventura",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECCIÓN: DATOS PERSONALES ---
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Información Personal",
                        fontWeight = FontWeight.Bold,
                        color = LocusActionOrange,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre(s)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = customTextFieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = apePa,
                            onValueChange = { apePa = it },
                            label = { Text("Ap. Paterno") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = customTextFieldColors,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = apeMa,
                            onValueChange = { apeMa = it },
                            label = { Text("Ap. Materno") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = customTextFieldColors,
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            value = genero,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Género") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = customTextFieldColors,
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(LocusSurfaceWhite)
                        ) {
                            listOf("Masculino", "Femenino", "Otro").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option, color = LocusDeepPurple) },
                                    onClick = {
                                        genero = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- SECCIÓN: ACCESO ---
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Datos de Acceso",
                        fontWeight = FontWeight.Bold,
                        color = LocusActionOrange,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = customTextFieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = customTextFieldColors,
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN DE REGISTRO ---
            Button(
                onClick = {
                    if (nombre.isNotBlank() && apePa.isNotBlank() && apeMa.isNotBlank() && genero.isNotBlank() && email.contains("@") && pass.length >= 6) {
                        viewModel.registrarUsuario(nombre, apePa, apeMa, fechaValidada, email, pass, genero)
                    } else {
                        Toast.makeText(context, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                enabled = authState !is AuthResult.Loading
            ) {
                if (authState is AuthResult.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Finalizar registro", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onIrALogin) {
                Row {
                    Text("¿Ya tienes cuenta? ", color = Color.Gray)
                    Text("Inicia sesión", color = LocusActionOrange, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}