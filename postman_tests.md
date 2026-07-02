# Guía de Pruebas Postman: ChapaTuRuta API

Esta guía detalla los pasos para probar los flujos principales de la API de ChapaTuRuta en Postman.
Asumimos que el backend está corriendo localmente en el puerto `8080` (API Gateway) o en producción (ej. `https://api-gateway-pet3.onrender.com`). Para esta guía usaremos la variable `{{base_url}}`.

## Configuración Inicial
Crea un "Environment" en Postman con las siguientes variables:
- `base_url`: `http://localhost:8080/api/v1` (o URL de Render)
- `manager_token`: (dejar vacío)
- `company_id`: (dejar vacío)
- `route_id`: (dejar vacío)
- `driver_token`: (dejar vacío)
- `passenger_token`: (dejar vacío)

---

## Flujo 1: Configuración de Empresa (Manager)

### 1.1 Registrar Manager
- **Método**: POST
- **URL**: `{{base_url}}/auth/register`
- **Body** (JSON):
```json
{
  "name": "Gerente Test",
  "email": "gerente@test.com",
  "password": "secure123",
  "role": "MANAGER"
}
```

### 1.2 Iniciar Sesión como Manager
- **Método**: POST
- **URL**: `{{base_url}}/auth/login`
- **Body** (JSON):
```json
{
  "email": "gerente@test.com",
  "password": "secure123"
}
```
*Acción posterior*: Copia el valor del token devuelto (`eyJhb...`) en la variable `manager_token`.

### 1.3 Registrar Empresa
- **Método**: POST
- **URL**: `{{base_url}}/companies/register`
- **Headers**: `Authorization`: `Bearer {{manager_token}}`
- **Body** (JSON):
```json
{
  "socialReason": "Transportes Test S.A.",
  "ruc": "10123456789"
}
```
*Acción posterior*: Copia el ID de la empresa creada (`id` en la respuesta) en la variable `company_id`.

---

## Flujo 2: Creación de Rutas

### 2.1 Crear Ruta (JSON upload simulado)
- **Método**: POST
- **URL**: `{{base_url}}/routes`
- **Headers**: `Authorization`: `Bearer {{manager_token}}`
- **Body** (JSON):
```json
{
  "originDistrict": "Los Olivos",
  "destinationDistrict": "San Miguel",
  "price": 2.50,
  "durationMin": 45,
  "stops": [
    {
      "name": "Paradero Inicial",
      "latitude": -11.987,
      "longitude": -77.086,
      "address": "Av. Universitaria Cdra 30",
      "stopOrder": 1
    },
    {
      "name": "Paradero Final",
      "latitude": -12.076,
      "longitude": -77.080,
      "address": "Av. Universitaria Cdra 10",
      "stopOrder": 2
    }
  ]
}
```
*Acción posterior*: Copia el ID de la ruta creada (`id` en la respuesta) en la variable `route_id`.

---

## Flujo 3: Registro de Conductor (Por el Manager)

### 3.1 Registrar Conductor
- **Método**: POST
- **URL**: `{{base_url}}/auth/register`
- **Headers**: `Authorization`: `Bearer {{manager_token}}`
- **Body** (JSON):
```json
{
  "name": "Chofer Test",
  "email": "chofer@test.com",
  "password": "driver123",
  "role": "DRIVER",
  "companyId": "{{company_id}}",
  "routeId": "{{route_id}}"
}
```

### 3.2 Iniciar Sesión como Conductor
- **Método**: POST
- **URL**: `{{base_url}}/auth/login`
- **Body** (JSON):
```json
{
  "email": "chofer@test.com",
  "password": "driver123"
}
```
*Acción posterior*: Copia el valor del token devuelto en la variable `driver_token`.

---

## Flujo 4: Registro de Pasajero y Búsqueda

### 4.1 Registrar Pasajero
- **Método**: POST
- **URL**: `{{base_url}}/auth/register`
- **Body** (JSON):
```json
{
  "name": "Pasajero Test",
  "email": "pasajero@test.com",
  "password": "secure123",
  "role": "PASSENGER"
}
```

### 4.2 Iniciar Sesión como Pasajero
- **Método**: POST
- **URL**: `{{base_url}}/auth/login`
- **Body** (JSON):
```json
{
  "email": "pasajero@test.com",
  "password": "secure123"
}
```
*Acción posterior*: Copia el valor del token devuelto en la variable `passenger_token`.

### 4.3 Buscar Rutas (Pasajero)
- **Método**: GET
- **URL**: `{{base_url}}/routes/search?origin=Los Olivos&destination=San Miguel`
- **Headers**: `Authorization`: `Bearer {{passenger_token}}`
- **Resultado Esperado**: Debería retornar una lista con la ruta creada en el paso 2.1, incluyendo sus paraderos.

---

## Flujo 5: Tracking (Real-time Simulation)

*(Nota: En producción, el Frontend usa RabbitMQ mediante webstomp para envíos asíncronos en tiempo real, pero el Tracking Service también suele exponer APIs REST para telemetría o histórico de la ubicación. Verifica los endpoints de tracking disponibles si necesitas simular esto vía REST POST)*.

