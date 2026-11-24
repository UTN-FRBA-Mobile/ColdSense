package com.coldwatch.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.IOException;

/**
 * Se cumple si hay credenciales de Firebase Admin (GOOGLE_APPLICATION_CREDENTIALS).
 * Sin credenciales el backend arranca en modo demo: sin verificación de tokens ni push.
 */
public class CredencialesFirebaseDisponibles implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            GoogleCredentials.getApplicationDefault();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }
}
