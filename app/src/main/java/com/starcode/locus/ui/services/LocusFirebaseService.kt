package com.starcode.locus.ui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.starcode.locus.MainActivity
import com.starcode.locus.R

class LocusFirebaseService : FirebaseMessagingService() {

    // 1. Manejo de mensajes de Firebase (Nube)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            mostrarNotificacion(this, it.title ?: "Locus", it.body ?: "")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Token: $token")
    }

    // 2. Funciones estáticas accesibles desde TODA la app
    companion object {
        fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "locus_channel_urgent"

            // Crear el canal si es Android 8.0 o superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Notificaciones Locus",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertas de proximidad y sistema"
                    enableLights(true)
                    enableVibration(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Intent para abrir la app al tocar la notificación
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Construir la notificación
            // 1. Cargamos el logo a color como Bitmap
            val largeIconBitmap = BitmapFactory.decodeResource(context.resources,
                R.drawable.locusicon
            )

// 2. Construimos la notificación encadenando correctamente
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.locusicon) // El icono pequeño de la barra de estado
                .setLargeIcon(largeIconBitmap)      // ✅ ESTO cambiará el mapa blanco por tu logo
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

            // Lanzar la notificación con un ID único basado en el tiempo
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}