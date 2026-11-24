package com.coldwatch.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Firebase Admin solo se configura si hay credenciales. Sin ellas el backend arranca en
 * modo demo (ver {@link CredencialesFirebaseDisponibles}).
 */
@Configuration
@Conditional(CredencialesFirebaseDisponibles.class)
public class FirebaseAdminConfig {

    @Bean
    public FirebaseApp firebaseApp(@Value("${firebase.project-id}") String projectId) {
        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se encontraron credenciales de Firebase Admin. Configurá " +
                            "GOOGLE_APPLICATION_CREDENTIALS con la ruta a la clave de servicio.",
                    exception
            );
        }

        return FirebaseApp.initializeApp(FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build());
    }

    @Bean
    public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
