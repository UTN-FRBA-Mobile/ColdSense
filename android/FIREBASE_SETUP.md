# Configurar Firebase Authentication

La app ofrece registro e inicio de sesión con email/contraseña o con Google.

1. En [Firebase Console](https://console.firebase.google.com/), registrá la app Android con el package name `ar.edu.utn.coldsense`.
2. En **Authentication → Sign-in method**, habilitá **Email/Password**.
3. Habilitá también **Google** y seleccioná el email de soporte del proyecto.
4. En **Project settings → General → Your apps**, agregá el SHA-1 de la firma de debug. Podés obtenerlo ejecutando `./gradlew signingReport` desde `android/`.
5. Descargá el `google-services.json` actualizado y copialo en `android/app/google-services.json`.
6. Sincronizá Gradle y ejecutá la app.

El plugin de Google Services se aplica automáticamente cuando encuentra el archivo de configuración. Sin ese archivo, la app compila, pero las acciones de autenticación muestran un mensaje de configuración pendiente.

La primera vez que alguien continúa con Google, Firebase crea la cuenta y abre la pantalla principal. El alta por email también inicia sesión automáticamente. Por ahora no se solicita verificación del email ni recuperación de contraseña.
