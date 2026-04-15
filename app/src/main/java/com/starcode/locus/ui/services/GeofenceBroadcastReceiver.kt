package com.starcode.locus.ui.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent


class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e("LocusGeofence", "Error: ${geofencingEvent.errorCode}")
            return
        }

        // Si el usuario ENTRA en el rango de 50m
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            triggeringGeofences?.forEach { geofence ->
                LocusFirebaseService.mostrarNotificacion(
                    context,
                    "¡Lugar Cercano!",
                    "Estás a unos pasos de: ${geofence.requestId}. ¡Pulsa para explorar!"
                )
            }
        }
    }
}