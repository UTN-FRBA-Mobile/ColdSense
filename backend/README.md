# ColdWatch Backend (mock)

Backend Spring Boot con datos en memoria (sin base de datos).

## Requisitos
- Java 17 o superior
- Maven 3.9+

## Correr
```bash
mvn spring-boot:run
```
Queda escuchando en http://localhost:8080

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
```bash
curl http://localhost:8080/heladeras/1
curl "http://localhost:8080/heladeras/1/lecturas?limit=5"

curl -X POST http://localhost:8080/heladeras \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Heladera Bar","temperaturaMinima":2.0,"temperaturaMaxima":6.0}'

# Agregar un sensor como en la app: buscar y crear la heladera con su número de serie
curl -X POST http://localhost:8080/dispositivos/busqueda
curl -X POST http://localhost:8080/heladeras \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Heladera Bar","temperaturaMinima":2.0,"temperaturaMaxima":6.0,"numeroSerie":"CS-4F2A"}'

curl -X PUT http://localhost:8080/heladeras/1/parametros \
  -H "Content-Type: application/json" \
  -d '{"temperaturaMinima":2,"temperaturaMaxima":8,"intervaloLecturaMinutos":10}'

# Poner una heladera en alerta
curl -X PUT http://localhost:8080/heladeras/1/temperatura \
  -H "Content-Type: application/json" \
  -d '{"temperatura": 9.5}'

# Simular desconexión
curl -X PUT http://localhost:8080/heladeras/1/temperatura \
  -H "Content-Type: application/json" \
  -d '{"temperatura": null}'

curl -X DELETE http://localhost:8080/heladeras/5
```
