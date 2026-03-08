package com.starcode.locus.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    // Paleta de colores Locus de alto contraste
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

            // ✅ Formato exacto para la DB: YYYY-MM-DD
            val fechaFormateada = "%04d-%02d-%02d".format(year, month + 1, day)
            viewModel.actualizarFecha(fechaFormateada, edad)
            mostrarError = false
        },
        2000, 0, 1 // Año sugerido por defecto
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocusBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¿Cuándo naciste?",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LocusDeepPurple,
            letterSpacing = (-1).sp
        )

        Text(
            text = "Debes ser mayor de edad para vivir la experiencia Locus.",
            color = Color.Gray,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- SELECTOR DE FECHA (Estilo Premium) ---
        OutlinedButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            // Fondo blanco para que el texto oscuro destaque igual que en los inputs
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = LocusSurfaceWhite,
                contentColor = LocusDeepPurple
            ),
            border = BorderStroke(
                width = if (mostrarError) 2.dp else 1.dp,
                color = if (mostrarError) LocusErrorRed else Color(0xFFD1D1D1)
            )
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = null,
                tint = LocusActionOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = fechaVisible,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // --- MENSAJE DE ERROR ---
        if (mostrarError) {
            Text(
                text = "Debes ser mayor de 18 años para usar Locus ✋",
                color = LocusErrorRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- BOTÓN CONTINUAR ---
        Button(
            onClick = {
                if (viewModel.esMayorDeEdad()) onEdadValida() else mostrarError = true
            },
            enabled = fechaVisible != "Selecciona tu fecha",
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LocusActionOrange,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFCCCCCC),
                disabledContentColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(
                "CONTINUAR",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}