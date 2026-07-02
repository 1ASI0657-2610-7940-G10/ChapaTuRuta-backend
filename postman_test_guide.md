# Guía de Pruebas con Postman: ChapaTuRuta

Esta guía detalla los endpoints, métodos, payloads de petición (Request) y respuestas esperadas (Response) para emular el flujo completo del sistema mediante Postman.

---

## Variables de Entorno Recomendadas en Postman
Configura un entorno en Postman con las siguientes variables:
- `base_url`: `https://api-gateway-pet3.onrender.com/api/v1` (o `http://localhost:8080/api/v1` en local)
- `token`: (Se llena automáticamente al hacer Login)
- `manager_id`: (ID del manager creado)
- `company_id`: (ID de la empresa creada)
- `route_id`: (ID de la ruta creada)
- `driver_id`: (ID del conductor creado)
- `passenger_id`: (ID del pasajero creado)

---

## Flujo de Pruebas Paso a Paso

### 1. Registro del Manager
- **Método:** `POST`
- **Path:** `{{base_url}}/auth/register`
- **Request Body (JSON):**
```json
{
  "name": "Gerente Carlos",
  "email": "carlos@transporte.com",
  "password": "secure123",
  "role": "MANAGER"
}
```
- **Response Esperado (201 Created):**
```json
{
  "id": "6e07cdb3-d786-42db-8b41-1992c5bbcb2d",
  "name": "Gerente Carlos",
  "email": "carlos@transporte.com",
  "role": "MANAGER",
  "createdAt": "2026-07-02T12:00:00"
}
```
*Acción:* Copia el campo `id` y guárdalo en la variable de entorno `manager_id`.

---

### 2. Inicio de Sesión (Login) del Manager
- **Método:** `POST`
- **Path:** `{{base_url}}/auth/login`
- **Request Body (JSON):**
```json
{
  "email": "carlos@transporte.com",
  "password": "secure123"
}
```
- **Response Esperado (200 OK):**
Retorna un string plano con el Token JWT (ej. `eyJhbGciOiJIUzM4NCJ9...`).
*Acción:* Copia el token retornado y guárdalo en la variable de entorno `token`. Asegúrate de que todas las peticiones siguientes incluyan el Header: `Authorization: Bearer {{token}}`.

---

### 3. Registro de la Empresa (Company)
- **Método:** `POST`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/companies/register`
- **Request Body (JSON):**
```json
{
  "name": "Transportes Universitaria S.A.",
  "ruc": "20987654321",
  "managerId": "{{manager_id}}"
}
```
- **Response Esperado (200 OK):**
```json
{
  "id": "e4f5a6b7-c8d9-0e1f-2a3b-4c5d6e7f8a9b",
  "name": "Transportes Universitaria S.A.",
  "ruc": "20987654321",
  "managerId": "6e07cdb3-d786-42db-8b41-1992c5bbcb2d"
}
```
*Acción:* Guarda el campo `id` de la empresa en la variable `company_id`.

---

### 4. Carga Dinámica de Ruta con 15 Paraderos
- **Método:** `POST`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/routes`
- **Request Body (JSON):**
*(Copia el contenido completo del archivo [sample_route_15_stops.json](file:///c:/Users/jhect/OneDrive/Documentos/GitHub/chapaturuta-frontend/sample_route_15_stops.json))*
```json
{
  "originDistrict": "Los Olivos",
  "destinationDistrict": "San Miguel",
  "price": 2.50,
  "durationMin": 65,
  "stops": [
    { "stopOrder": 1, "name": "Paradero Palmeras", "latitude": -11.979603, "longitude": -77.078233, "address": "Av. Las Palmeras" },
    { "stopOrder": 2, "name": "Paradero Izaguirre", "latitude": -11.989069, "longitude": -77.075486, "address": "Av. Carlos Izaguirre" },
    { "stopOrder": 3, "name": "Paradero Covida", "latitude": -11.996155, "longitude": -77.073351, "address": "Antunez de Mayolo" },
    { "stopOrder": 4, "name": "Cruce Universitaria / Tomas Valle", "latitude": -12.011681, "longitude": -77.077224, "address": "Av. Tomas Valle" },
    { "stopOrder": 5, "name": "Paradero Angélica Gamarra", "latitude": -12.016335, "longitude": -77.078715, "address": "Av. Angélica Gamarra" },
    { "stopOrder": 6, "name": "Paradero Anta", "latitude": -12.022718, "longitude": -77.080539, "address": "Av. Universitaria" },
    { "stopOrder": 7, "name": "Óvalo José Granda", "latitude": -12.030514, "longitude": -77.082771, "address": "Óvalo José Granda" },
    { "stopOrder": 8, "name": "Puente Bella Unión", "latitude": -12.040182, "longitude": -77.084922, "address": "Puente Bella Unión" },
    { "stopOrder": 9, "name": "Paradero Materiales", "latitude": -12.045617, "longitude": -77.085813, "address": "Av. Materiales" },
    { "stopOrder": 10, "name": "Cruce Argentina / Universitaria", "latitude": -12.049405, "longitude": -77.086438, "address": "Av. Argentina" },
    { "stopOrder": 11, "name": "Paradero Colonial", "latitude": -12.053704, "longitude": -77.086817, "address": "Av. Oscar R. Benavides" },
    { "stopOrder": 12, "name": "Paradero Venezuela (San Marcos)", "latitude": -12.059432, "longitude": -77.086208, "address": "Av. Venezuela, Puerta 3 UNMSM" },
    { "stopOrder": 13, "name": "Cruce Universitaria / Mariano Cornejo", "latitude": -12.067341, "longitude": -77.084051, "address": "Plaza de la Bandera" },
    { "stopOrder": 14, "name": "Paradero Bolívar", "latitude": -12.072836, "longitude": -77.082399, "address": "Av. Simón Bolívar" },
    { "stopOrder": 15, "name": "Paradero La Mar", "latitude": -12.077202, "longitude": -77.081155, "address": "Av. La Mar" }
  ]
}
```
- **Response Esperado (201 Created):**
Retorna la ruta creada con sus paraderos indexados y con IDs únicos asignados.
*Acción:* Guarda el campo `routeId` en la variable `route_id`.

---

### 5. Registrar y Asignar Conductor (Driver)
- **Método:** `POST`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/auth/register`
- **Request Body (JSON):**
```json
{
  "name": "Luis Chofer",
  "email": "luis@transporte.com",
  "password": "driver123",
  "role": "DRIVER",
  "companyId": "{{company_id}}",
  "routeId": "{{route_id}}"
}
```
*Acción:* Guarda el `id` retornado en la variable `driver_id`.

---

### 6. Login del Conductor
- **Método:** `POST`
- **Path:** `{{base_url}}/auth/login`
- **Request Body (JSON):**
```json
{
  "email": "luis@transporte.com",
  "password": "driver123"
}
```
*Acción:* Reemplaza la variable `token` con el nuevo token del conductor.

---

### 7. Conductor Check-In (Ubicación en Tiempo Real)
- **Método:** `POST`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/tracking/check-in`
- **Request Body (JSON):**
```json
{
  "driverId": "{{driver_id}}",
  "routeId": "{{route_id}}",
  "stopId": "paradero-1-id-del-json",
  "latitude": -11.979603,
  "longitude": -77.078233,
  "timestamp": 1782434722
}
```
*Nota:* Esto guardará las coordenadas en Redis y publicará el evento en RabbitMQ de manera asíncrona.

---

### 8. Registro del Pasajero
- **Método:** `POST`
- **Path:** `{{base_url}}/auth/register`
- **Request Body (JSON):**
```json
{
  "name": "Pasajero E2E",
  "email": "pasajero@e2e.com",
  "password": "secure123",
  "role": "PASSENGER"
}
```
*Acción:* Guarda el `id` retornado en `passenger_id`.

---

### 9. Login del Pasajero
- **Método:** `POST`
- **Path:** `{{base_url}}/auth/login`
- **Request Body (JSON):**
```json
{
  "email": "pasajero@e2e.com",
  "password": "secure123"
}
```
*Acción:* Reemplaza la variable `token` con el del Pasajero.

---

### 10. Pasajero Esperando en Paradero (Demanda Activa)
- **Método:** `POST`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/demand/wait`
- **Request Body (JSON):**
```json
{
  "routeId": "{{route_id}}",
  "stopId": "paradero-12-id-del-json",
  "passengerId": "{{passenger_id}}"
}
```
*Nota:* Inserta la clave correspondiente en Redis con TTL de 1 hora.

---

### 11. Consultar Demanda de Paraderos (Manager/Driver)
- **Método:** `GET`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/demand/route/{{route_id}}`
- **Response Esperado (200 OK):**
```json
{
  "paradero-12-id-del-json": 1
}
```
*(Muestra el recuento de pasajeros esperando activamente en cada paradero de la ruta).*

---

### 12. Reset / Borrado de Cuenta de Manager (Limpieza)
- **Método:** `DELETE`
- **Headers:** `Authorization: Bearer {{token}}`
- **Path:** `{{base_url}}/auth/profile/{{manager_id}}`
- **Response Esperado (204 No Content / 200 OK):**
Elimina el manager, la empresa vinculada, los choferes creados y los datos asociados permanentemente en cascada.
