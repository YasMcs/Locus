package com.starcode.locus.ui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.starcode.locus.MainActivity
import com.starcode.locus.ui.services.LocusFirebaseService.LocusFirebaseService.Companion.mostrarNotificacion

class LocusFirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            // Ahora llamamos a la función estática desde aquí también
            mostrarNotificacion(this, it.title ?: "Locus", it.body ?: "")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM_TOKEN", "Token: $token")
    }

    // --- COMPANION OBJECT PARA EL MODO DEMO ---
    // ... (mismo package e imports)

    class LocusFirebaseService : FirebaseMessagingService() {

        // ... (onMessageReceived y onNewToken igual)

        companion object {
            fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channelId = "locus_channel_urgent" // Cambiamos ID para asegurar nuevos ajustes

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Notificaciones Locus",
                        NotificationManager.IMPORTANCE_HIGH // IMPORTANTE: High para que flote
                    ).apply {
                        description = "Alertas de proximidad"
                        enableLights(true)
                        enableVibration(true)
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                    }
                    notificationManager.createNotificationChannel(channel)
                }

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }

                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val builder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_map) // Cambiado a icono de mapa
                    .setContentTitle(titulo)
                    .setContentText(mensaje)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX) // MAX para que aparezca arriba
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Usamos un ID único por tiempo para que no se sobrepongan
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }
}