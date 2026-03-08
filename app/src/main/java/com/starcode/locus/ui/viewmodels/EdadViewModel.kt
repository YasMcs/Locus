package com.starcode.locus.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EdadViewModel : ViewModel() {
    // Guardamos la fecha en formato YYYY-MM-DD para la base de datos
    private val _fechaParaDB = MutableStateFlow("")
    val fechaParaDB: StateFlow<String> = _fechaParaDB

    // Guardamos la edad calculada para validaciones
    private val _edadActual = MutableStateFlow(-1)
    val edadActual: StateFlow<Int> = _edadActual

    fun actualizarFecha(fecha: String, edad: Int) {
        _fechaParaDB.value = fecha
        _edadActual.value = edad
    }

    fun esMayorDeEdad(): Boolean = _edadActual.value >= 18
}