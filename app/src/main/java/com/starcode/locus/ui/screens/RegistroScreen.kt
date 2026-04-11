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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
        unfocusedBorderColor = Color(0xFFD1D1D1),
        focusedLabelColor = LocusActionOrange
    )

    // El Box nos permite usar fillMaxSize sin que el Scroll se confunda con los Insets
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // EXPLICACIÓN: systemBars gestiona tanto el Notch superior como la barra de gestos inferior.
                // ime gestiona el padding cuando el teclado aparece para que no tape el botón.
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime))
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(
                painter = painterResource(id = com.starcode.locus.R.drawable.logoko),
                contentDescription = "Logo Locus",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 8.dp),
                alignment = Alignment.Center
            )

            Text(
                text = "Crea tu perfil",
                color = LocusDeepPurple.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CAMPO NOMBRE ---
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

            // --- CAMPO APELLIDO PATERNO ---
            OutlinedTextField(
                value = apePa,
                onValueChange = { apePa = it },
                label = { Text("Ap. Paterno") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- CAMPO APELLIDO MATERNO ---
            OutlinedTextField(
                value = apeMa,
                onValueChange = { apeMa = it },
                label = { Text("Ap. Materno") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = customTextFieldColors,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- SELECTOR DE GÉNERO ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
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
                    DropdownMenuItem(
                        text = { Text("Masculino", color = LocusDeepPurple) },
                        onClick = {
                            genero = "Masculino"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Femenino", color = LocusDeepPurple) },
                        onClick = {
                            genero = "Femenino"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Otro", color = LocusDeepPurple) },
                        onClick = {
                            genero = "Otro"
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- CAMPO EMAIL ---
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

            // --- CAMPO CONTRASEÑA ---
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

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTÓN DE REGISTRO ---
            Button(
                onClick = {
                    if (nombre.isNotBlank() && apePa.isNotBlank() && apeMa.isNotBlank() && genero.isNotBlank() && email.contains("@") && pass.length >= 6) {
                        viewModel.registrarUsuario(nombre, apePa, apeMa, fechaValidada, email, pass, genero)
                    } else {
                        Toast.makeText(context, "ERROR: Todos los campos son requeridos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
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

            // Espaciado final para asegurar que el contenido no quede pegado al borde inferior
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}