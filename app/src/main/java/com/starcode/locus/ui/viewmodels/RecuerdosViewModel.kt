package com.starcode.locus.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starcode.locus.data.remote.RetrofitClient
import com.starcode.locus.data.remote.SessionManager
import com.starcode.locus.data.remote.request.ImagenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecuerdosViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _imagenes = MutableStateFlow<List<ImagenResponse>>(emptyList())
    val imagenes: StateFlow<List<ImagenResponse>> = _imagenes

    private val _estaCargando = MutableStateFlow(false)
    val estaCargando: StateFlow<Boolean> = _estaCargando

    fun cargarRecuerdos() {
        val userId = sessionManager.getUserId()
        val tokenRaw = sessionManager.obtenerToken()

        viewModelScope.launch {
            _estaCargando.value = true
            try {
                if (tokenRaw.isNullOrBlank()) return@launch
                val token = "Bearer $tokenRaw"
                val lista = RetrofitClient.instance.obtenerImagenesUsuario(token, userId)
                _imagenes.value = lista
            } catch (e: Exception) {
                Log.e("LocusDebug", "Error al cargar recuerdos: ${e.message}")
            } finally {
                _estaCargando.value = false
            }
        }
    }
}