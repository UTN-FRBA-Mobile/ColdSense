# ColdSense Backend (mock)

Backend Spring Boot con datos en memoria (sin base de datos). Todos los endpoints requieren un ID token válido de Firebase Authentication.

## Requisitos
- Java 17 o superior
- Maven 3.9+

## Correr

El backend usa Firebase Admin para verificar los tokens. Descargá una clave de cuenta de servicio desde **Firebase Console → Project settings → Service accounts** y guardala fuera del repositorio. No uses `google-services.json` para el backend ni subas la clave de servicio a Git.

Configurá la ruta de la clave y ejecutá:

```bash
export GOOGLE_APPLICATION_CREDENTIALS="/ruta/segura/firebase-service-account.json"
export FIREBASE_PROJECT_ID="coldsense-159a5"
mvn spring-boot:run
```

Queda escuchando en http://localhost:8080. Si faltan las credenciales de Admin, el backend no inicia.

La app obtiene un ID token de Firebase después del registro o inicio de sesión y lo manda como `Authorization: Bearer <token>` en cada llamada. El servidor verifica firma, vencimiento y proyecto antes de atender el endpoint. La identidad verificada queda disponible en la solicitud como atributo `firebaseUid`; la autorización por dueño de cada heladera todavía no está implementada.

La URL HTTP actual sirve solo para desarrollo local. Como el ID token es una credencial de acceso, al publicar el backend debe usarse HTTPS.

## Endpoints
| Método | Ruta | Descripción |
|---|---|---|
| GET | /heladeras | Lista todas |
| GET | /heladeras/{id} | Una heladera |
| GET | /heladeras/{id}/lecturas?limit=5 | Últimas lecturas (más nueva primero) |
| POST | /heladeras | Crea una heladera (con `numeroSerie` vincula un sensor encontrado) |
| PUT | /heladeras/{id} | Modifica nombre/límites/intervalo |
| PUT | /heladeras/{id}/parametros | Modifica límites e intervalo |
| PUT | /heladeras/{id}/temperatura | Simula lectura del sensor |
| DELETE | /heladeras/{id} | Elimina la heladera y su historial |
| POST | /dispositivos/busqueda | Busca sensores nuevos (tarda ~3 s y siempre encuentra uno) |

## Ejemplos

Para probar con `curl`, exportá antes un ID token vigente obtenido desde un usuario autenticado en Firebase:

```bash
curl http://localhost:8080/heladeras \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"
curl "http://localhost:8080/heladeras/1/lecturas?limit=5" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"

curl -X POST http://localhost:8080/heladeras \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -d '{"nombre":"Heladera Bar","temperaturaMinima":2.0,"temperaturaMaxima":6.0}'

# Agregar un sensor como en la app: buscar y crear la heladera con su número de serie
curl -X POST http://localhost:8080/dispositivos/busqueda
curl -X POST http://localhost:8080/heladeras \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Heladera Bar","temperaturaMinima":2.0,"temperaturaMaxima":6.0,"numeroSerie":"CS-4F2A"}'

curl -X PUT http://localhost:8080/heladeras/1/parametros \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -d '{"temperaturaMinima":2,"temperaturaMaxima":8,"intervaloLecturaMinutos":10}'

# Poner una heladera en alerta
curl -X PUT http://localhost:8080/heladeras/1/temperatura \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -d '{"temperatura": 9.5}'

# Simular desconexión
curl -X PUT http://localhost:8080/heladeras/1/temperatura \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -d '{"temperatura": null}'

curl -X DELETE http://localhost:8080/heladeras/5 \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"
```
