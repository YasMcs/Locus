 package com.starcode.locus.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EdadViewModel : ViewModel() {
    // Guardamos la edad como String para que sea fácil de escribir en el TextField
    private val _edad = MutableStateFlow("")
    val edad: StateFlow<String> = _edad

    // Para controlar si mostramos el mensaje de error de "Eres menor de edad"
    private val _errorEdad = MutableStateFlow(false)
    val errorEdad: StateFlow<Boolean> = _errorEdad

    fun onEdadChanged(nuevaEdad: String) {
        // Solo permitimos números y máximo 3 dígitos (por si vive 100 años jaja)
        if (nuevaEdad.all { it.isDigit() } && nuevaEdad.length <= 3) {
            _edad.value = nuevaEdad
            _errorEdad.value = false // Limpiamos el error mientras escribe
        }
    }

    fun esMayorDeEdad(): Boolean {
        val edadInt = _edad.value.toIntOrNull() ?: 0
        val esMayor = edadInt >= 18

        if (!esMayor) {
            _errorEdad.value = true
        }

        return esMayor
    }
}