package com.starcode.locus.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.starcode.locus.ui.viewmodels.EdadViewModel

@Composable
fun EdadScreen(
    onEdadValida: () -> Unit,
    viewModel: EdadViewModel = viewModel()
) {
    var fechaVisible by remember { mutableStateOf("Selecciona tu fecha") }
    var mostrarError by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val LocusDeepPurple = Color(0xFF1D1B20)
    val LocusActionOrange = Color(0xFFE6673D)
    val LocusBackground = Color(0xFFFDF6EE)
    val LocusSurfaceWhite = Color(0xFFFFFFFF)
    val LocusErrorRed = Color(0xFFB00020)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            fechaVisible = "$day/${month + 1}/$year"
            val hoy = java.util.Calendar.getInstance()
            var edad = hoy.get(java.util.Calendar.YEAR) - year
            if (hoy.get(java.util.Calendar.MONTH) < month ||
                (hoy.get(java.util.Calendar.MONTH) == month && hoy.get(java.util.Calendar.DAY_OF_MONTH) < day)) {
                edad--
            }
            val fechaFormateada = "%04d-%02d-%02d".format(year, month + 1, day)
            viewModel.actualizarFecha(fechaFormateada, edad)
            mostrarError = false
        },
        2000, 0, 1
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- ICONO DE BIENVENIDA (Llena el espacio superior) ---
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(LocusActionOrange.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = LocusActionOrange,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¿Cuándo naciste?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = LocusDeepPurple,
                    letterSpacing = (-1).sp
                )
            )

            Text(
                text = "Para vivir la experiencia Locus, necesitamos verificar que eres mayor de edad.",
                color = Color.Gray,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp).padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- SELECTOR ENVOLVENTE (Surface Premium) ---
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "FECHA DE NACIMIENTO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocusActionOrange,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = LocusBackground.copy(alpha = 0.5f),
                            contentColor = LocusDeepPurple
                        ),
                        border = BorderStroke(
                            width = if (mostrarError) 2.dp else 1.dp,
                            color = if (mostrarError) LocusErrorRed else Color(0xFFD1D1D1)
                        )
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = LocusActionOrange)
                        Spacer(Modifier.width(12.dp))
                        Text(fechaVisible, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    if (mostrarError) {
                        Text(
                            text = "Debes ser mayor de 18 años para usar Locus ✋",
                            color = LocusErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- BOTÓN CONTINUAR ---
            Button(
                onClick = {
                    if (viewModel.esMayorDeEdad()) onEdadValida() else mostrarError = true
                },
                enabled = fechaVisible != "Selecciona tu fecha",
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LocusActionOrange),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("SIGUIENTE", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}