package com.coldwatch.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Rejects requests without a valid Firebase Authentication ID token,
 * except token-less requests from this same PC (the sensor simulator).
 * In demo mode (no Firebase Admin credentials) only requests from this same PC are accepted.
 */
@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    public static final String UID_ATTRIBUTE = "firebaseUid";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

    // null en modo demo (no hay credenciales de Firebase Admin para verificar tokens)
    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthenticationFilter(ObjectProvider<FirebaseAuth> firebaseAuth) {
        this.firebaseAuth = firebaseAuth.getIfAvailable();
        if (this.firebaseAuth == null) {
            log.warn("MODO DEMO: no hay credenciales de Firebase Admin (GOOGLE_APPLICATION_CREDENTIALS). "
                    + "Solo se aceptan llamadas desde esta PC (el emulador cuenta como esta PC) y no se mandan push.");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");

        // El simulador de sensores (index.html) se abre en el navegador de la misma PC y no tiene login.
        // Si mandan token se verifica igual; desde otra máquina o detrás de un proxy sigue siendo obligatorio.
        if (authorization == null && esDeLaMismaPc(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Modo demo: sin credenciales no se puede verificar ningún token, así que solo pasa
        // lo que viene de esta PC (con un usuario mock de la app o con uno real, da igual)
        if (firebaseAuth == null) {
            if (esDeLaMismaPc(request)) {
                filterChain.doFilter(request, response);
            } else {
                writeUnauthorized(response);
            }
            return;
        }

        if (authorization == null || !authorization.regionMatches(
                true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            writeUnauthorized(response);
            return;
        }

        String idToken = authorization.substring(BEARER_PREFIX.length()).trim();
        if (idToken.isEmpty()) {
            writeUnauthorized(response);
            return;
        }

        try {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            request.setAttribute(UID_ATTRIBUTE, decodedToken.getUid());
            filterChain.doFilter(request, response);
        } catch (FirebaseAuthException exception) {
            writeUnauthorized(response);
        }
    }

    private static boolean esDeLaMismaPc(HttpServletRequest request) {
        if (request.getHeader("X-Forwarded-For") != null || request.getHeader("Forwarded") != null) {
            return false;
        }
        try {
            return InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"error\":\"unauthorized\",\"message\":\"Se requiere un token de Firebase válido.\"}"
        );
    }
}
