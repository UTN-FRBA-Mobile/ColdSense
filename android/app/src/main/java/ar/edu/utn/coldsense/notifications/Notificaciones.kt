package ar.edu.utn.coldsense.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ar.edu.utn.coldsense.R

// Mismo id que usa el backend (NotificacionService.java)
const val CANAL_ALERTAS = "alertas_temperatura"

/**
 * Crea el canal de las alertas (Android pide un canal para mostrar notificaciones).
 * Si ya existe no hace nada, así que se puede llamar siempre.
 */
fun crearCanalAlertas(context: Context) {
    val canal = NotificationChannel(
        CANAL_ALERTAS,
        "Alertas de temperatura",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Avisos cuando una heladera sale de rango o pierde conexión"
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
}

/**
 * Muestra una alerta que mandó el backend. Se usa con la app abierta: con la app
 * cerrada o en segundo plano, Android la muestra solo con el mismo título y texto.
 *
 * @param heladeraId Si vuelve a avisar por la misma heladera, reemplaza la notificación anterior.
 */
@SuppressLint("MissingPermission") // El permiso se revisa al principio
fun mostrarAlerta(context: Context, titulo: String, texto: String, heladeraId: Long?) {

    // Android 13+: sin el permiso no se pueden mostrar notificaciones
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    crearCanalAlertas(context)

    // Al tocarla abre la app como desde el ícono (si ya estaba abierta, la trae al frente)
    val abrirApp = PendingIntent.getActivity(
        context,
        0,
        context.packageManager.getLaunchIntentForPackage(context.packageName),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notificacion = NotificationCompat.Builder(context, CANAL_ALERTAS)
        .setSmallIcon(R.drawable.ic_notificacion)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(heladeraId?.toInt() ?: 0, notificacion)
}
