package com.coldwatch.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 */
@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

    public static final String UID_ATTRIBUTE = "firebaseUid";

    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
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
