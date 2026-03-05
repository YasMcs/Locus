package com.starcode.locus.util

import com.starcode.locus.data.dao.LocusDao
import com.starcode.locus.data.entities.LugarEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

object DatabaseSeeder {
    fun insertarPuntosDePrueba(dao: LocusDao) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // PASO 1: Forzamos la limpieza eliminando la condición isEmpty
                // Si tienes una función para borrar, úsala aquí: dao.borrarTodo()

                val puntoPreciso = LugarEntity(
                    id_lugar = 0,
                    id_categoria = 0,
                    nombre_lugar = "Punto de Prueba Local",
                    latitud = 16.62619, // <--- CAMBIA ESTO por tu latitud actual
                    longitud = -93.10294, // <--- CAMBIA ESTO por tu longitud actual
                    radio_activacion = 100, // Radio generoso de 100 metros para que "salte" fácil
                    titulo_ficha = "¡Has llegado al destino!",
                    descripcion_hist = "Este es un punto de prueba para verificar que la ficha se desbloquea correctamente cuando estás cerca.",
                    dato_curioso = "Si estás leyendo esto, ¡tu lógica de geofencing funciona a la perfección!"
                )

                dao.insertarLugar(puntoPreciso)

                // PASO 2: Verificar en consola si se guardó
                val total = dao.obtenerLugares().size
                Log.d("LocusDebug", "Total de lugares en BD: $total")

            } catch (e: Exception) {
                Log.e("LocusDebug", "Error fatal: ${e.message}")
            }
        }
    }
}