package com.coldwatch.backend.service;

import com.coldwatch.backend.dto.EstadoNotificaciones;
import com.coldwatch.backend.model.EstadoHeladera;
import com.coldwatch.backend.model.Heladera;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manda push notifications con Firebase Cloud Messaging cuando una heladera
 * entra en alerta o pierde conexión. El texto se arma acá, así la app solo lo muestra.
 */
@Service
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    // Mismo id que el canal que crea la app (Notificaciones.kt)
    private static final String CANAL_ALERTAS = "alertas_temperatura";

    // IDs de instalación de Firebase (fid) de los celulares registrados.
    // Todavía no hay usuarios, así que todos reciben todas las alertas
    private final Set<String> fids = ConcurrentHashMap.newKeySet();

    // null si no están las credenciales de Firebase: las alertas solo se loguean
    private final FirebaseMessaging firebaseMessaging;

    public NotificacionService(@Value("${firebase.credenciales}") String rutaCredenciales) {
        this.firebaseMessaging = inicializarFirebase(rutaCredenciales);
    }

    public void registrarCelular(String fid) {
        fids.add(fid);
    }

    public EstadoNotificaciones estado() {
        return new EstadoNotificaciones(firebaseMessaging != null, fids.size());
    }

    /**
     * Avisa solo cuando la heladera pasa a un estado que requiere atención,
     * no en cada lectura (si ya estaba en alerta no vuelve a avisar).
     */
    public void notificarCambioDeEstado(EstadoHeladera anterior, Heladera heladera) {
        EstadoHeladera nuevo = heladera.estado();
        if (nuevo == anterior) {
            return;
        }

        switch (nuevo) {
            case ALERTA -> enviar(
                    heladera.nombre() + " en alerta",
                    "Marca " + formatear(heladera.temperaturaActual()) + " °C y el rango configurado es de "
                            + formatear(heladera.temperaturaMinima()) + " a "
                            + formatear(heladera.temperaturaMaxima()) + " °C.",
                    heladera.id()
            );
            case SIN_CONEXION -> enviar(
                    heladera.nombre() + " sin conexión",
                    "El sensor dejó de enviar lecturas.",
                    heladera.id()
            );
            case NORMAL -> {
                // Volver a la normalidad no requiere atención
            }
        }
    }

    private void enviar(String titulo, String texto, Long heladeraId) {
        if (firebaseMessaging == null || fids.isEmpty()) {
            log.info("Alerta sin enviar ({}): {} - {}",
                    firebaseMessaging == null ? "Firebase no configurado" : "ningún celular registrado",
                    titulo, texto);
            return;
        }

        List<String> destinatarios = List.copyOf(fids);
        List<Message> mensajes = destinatarios.stream()
                .map(fid -> Message.builder()
                        .setFid(fid)
                        .setNotification(Notification.builder()
                                .setTitle(titulo)
                                .setBody(texto)
                                .build())
                        .setAndroidConfig(AndroidConfig.builder()
                                .setPriority(AndroidConfig.Priority.HIGH)
                                .setNotification(AndroidNotification.builder()
                                        .setChannelId(CANAL_ALERTAS)
                                        .build())
                                .build())
                        .putData("heladeraId", heladeraId.toString())
                        .build())
                .toList();

        try {
            BatchResponse respuesta = firebaseMessaging.sendEach(mensajes);
            log.info("Alerta enviada a {} de {} celulares: {}",
                    respuesta.getSuccessCount(), mensajes.size(), titulo);

            List<SendResponse> resultados = respuesta.getResponses();
            for (int i = 0; i < resultados.size(); i++) {
                FirebaseMessagingException error = resultados.get(i).getException();
                if (error == null) {
                    continue;
                }
                // El celular ya no está registrado (ej: desinstalaron la app), no tiene sentido seguir mandándole
                if (error.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    fids.remove(destinatarios.get(i));
                }
                log.warn("No se pudo enviar la alerta a un celular: {}", error.getMessage());
            }
        } catch (FirebaseMessagingException e) {
            log.error("No se pudo enviar la alerta: {}", e.getMessage());
        }
    }

    private static FirebaseMessaging inicializarFirebase(String rutaCredenciales) {
        File archivo = new File(rutaCredenciales);
        if (!archivo.exists()) {
            log.warn("No se encontró {}: las push notifications quedan desactivadas", archivo.getAbsolutePath());
            return null;
        }

        try (InputStream archivoCredenciales = new FileInputStream(archivo)) {
            GoogleCredentials credenciales = GoogleCredentials.fromStream(archivoCredenciales);
            FirebaseOptions opciones = FirebaseOptions.builder()
                    .setCredentials(credenciales)
                    .build();
            FirebaseApp app = FirebaseApp.getApps().isEmpty()
                    ? FirebaseApp.initializeApp(opciones)
                    : FirebaseApp.getInstance();

            String proyecto = credenciales instanceof ServiceAccountCredentials cuenta ? cuenta.getProjectId() : "?";
            log.info("Push notifications activadas (proyecto de Firebase: {})", proyecto);
            return FirebaseMessaging.getInstance(app);
        } catch (IOException e) {
            log.error("No se pudo leer {}: las push notifications quedan desactivadas", archivo.getAbsolutePath(), e);
            return null;
        }
    }

    /** 2.0 -> "2"  |  9.5 -> "9.5" (igual que formatLimite en la app) */
    private static String formatear(double valor) {
        return valor % 1.0 == 0.0
                ? String.valueOf((long) valor)
                : String.format(Locale.US, "%.1f", valor);
    }
}
