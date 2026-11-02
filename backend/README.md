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

## Simulador de sensores
Abrí **http://localhost:8080** en el navegador. Hay un panel por heladera para mandar lecturas como si fueran
del sensor: mové la aguja (o escribí un valor) y tocá **Enviar lectura**, o usá los botones rápidos
(**En rango**, **Sobre el máximo**, **Desconectar sensor**). El registro de la derecha muestra qué hizo el bridge
con cada lectura (si mandó la push o por qué no). Es una página estática en `src/main/resources/static/index.html`.

El simulador no tiene login: las llamadas **sin** `Authorization` que vienen de la misma PC (`localhost`) se aceptan
sin token. Si mandan un token se verifica igual, y desde otra máquina (un celular en la red) o detrás de un proxy
el token sigue siendo obligatorio. Por eso hay que abrirlo como `localhost`, no con la IP de la PC.

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
| POST | /notificaciones/registro | Registra un celular (su `fid` de Firebase) para recibir alertas (push) |
| DELETE | /notificaciones/registro/{fid} | Da de baja un celular (la app lo llama al cerrar sesión) |
| GET | /notificaciones/estado | Si las push están activas y cuántos celulares hay registrados |

## Push notifications (Firebase)
Cuando una lectura deja una heladera **en alerta** o **sin conexión**, el backend les avisa con
Firebase Cloud Messaging a los celulares registrados (la app se registra sola al abrir el Home).
Avisa solo cuando cambia el estado: si ya estaba en alerta, una nueva lectura fuera de rango no vuelve a avisar.

Las push usan la misma cuenta de servicio configurada para Firebase Admin en la sección anterior.
La cuenta debe pertenecer al mismo proyecto que usa Android y `FIREBASE_PROJECT_ID` (por defecto
`coldsense-159a5`). No hace falta una segunda copia de las credenciales.

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

# Poner una heladera en alerta (manda la push)
curl -X PUT http://localhost:8080/heladeras/1/temperatura \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -d '{"temperatura": 9.5}'

# Simular desconexión (manda la push)
curl -X PUT http://localhost:8080/heladeras/1/temperatura \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN" \
  -d '{"temperatura": null}'

curl -X DELETE http://localhost:8080/heladeras/5 \
  -H "Authorization: Bearer $FIREBASE_ID_TOKEN"
```
