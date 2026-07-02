# Informe de Arquitectura: ChapaTuRuta

## 1. Visión General de la Arquitectura
El sistema **ChapaTuRuta** está diseñado utilizando una **Arquitectura de Microservicios**. Esta elección permite escalar diferentes partes del sistema de manera independiente (por ejemplo, el servicio de rastreo en tiempo real requiere más recursos que el de identidad) y facilita el desarrollo paralelo.

### Microservicios Principales:
1. **API Gateway**: Actúa como el punto de entrada único para todas las solicitudes de los clientes (frontend/móvil). Se encarga del enrutamiento de peticiones, manejo de CORS y validación inicial de tokens JWT.
2. **Identity Service**: Gestiona la autenticación, autorización (RBAC: Manager, Driver, Passenger) y el registro de usuarios y empresas. Genera los tokens JWT.
3. **Routing Service**: Administra las rutas estáticas y paraderos. Gestiona la información geográfica y el orden de los paraderos.
4. **Tracking Service**: Maneja la ubicación en tiempo real de los vehículos, check-ins en paraderos y cálculo de tiempos de llegada estimados (ETA).

---

## 2. Decisiones de Arquitectura

### Arquitectura Hexagonal (Ports and Adapters)
Dentro de cada microservicio, se implementa la Arquitectura Hexagonal para asegurar un alto desacoplamiento entre la lógica de negocio y las tecnologías externas.

- **Capa de Dominio (Domain)**: Contiene las entidades del negocio (ej. `User`, `Route`, `Location`) y las interfaces (Puertos) de repositorios. No tiene dependencias de frameworks externos (como Spring o JPA).
- **Capa de Aplicación (Application / Use Cases)**: Orquesta la lógica de negocio utilizando los puertos del dominio. Aquí residen servicios como `RegisterUserUseCase` o `ManageRouteUseCase`.
- **Capa de Infraestructura (Infrastructure)**: Implementa los puertos (Adaptadores). Contiene los repositorios JPA (`RouteRepositoryAdapter`), Controladores REST (`UserController`), configuraciones de seguridad y conexiones a bases de datos o brokers de mensajes.

### Patrones Utilizados
- **API Gateway Pattern**: Centraliza el acceso y la seguridad perimetral.
- **Database per Service**: Cada microservicio (Identity, Routing) tiene su propia base de datos (o esquema lógico separado) en PostgreSQL, garantizando autonomía de datos.
- **Token-based Authentication (JWT)**: `Identity Service` emite tokens firmados y el `API Gateway` / microservicios los validan, permitiendo autenticación sin estado (stateless).
- **Publish-Subscribe / Asynchronous Messaging**: Usado para actualizaciones en tiempo real.

---

## 3. Uso de Redis y RabbitMQ en el Tracking Service

El rastreo en tiempo real de los vehículos es un desafío de alta concurrencia. Para ello, se emplean dos tecnologías clave:

### RabbitMQ (Mensajería Asíncrona)
- **Propósito**: Actúa como un *Message Broker*. Cuando el frontend del conductor emite una actualización de ubicación (Check-in o GPS tracking), la petición se encola en RabbitMQ.
- **Ventaja**: Evita sobrecargar la base de datos principal con escrituras continuas. Permite procesar picos de actualizaciones geográficas de manera asíncrona, encolándolas de forma segura hasta que el servicio de rastreo pueda procesarlas y guardarlas en caché o base de datos.

### Redis (Caché y Real-time)
- **Propósito**: Base de datos en memoria clave-valor.
- **Uso**: Almacena la *última ubicación conocida* de cada conductor y el estado temporal de los paraderos. 
- **Ventaja**: Las lecturas del pasajero (que consulta constantemente "¿dónde está mi bus?") se dirigen a Redis, proporcionando tiempos de respuesta de milisegundos en lugar de consultar costosas tablas relacionales en PostgreSQL.

---

## 4. Elección de Tecnologías

- **Backend Framework**: **Spring Boot 3 (Java 21)**. Elegido por su robustez, excelente ecosistema para microservicios (Spring Cloud Gateway, Spring Data JPA) y fuerte tipado.
- **Frontend**: **React.js**. Utilizado para las interfaces web del Manager, Driver y Passenger. Elegido por su reactividad y gran ecosistema de librerías.
- **Base de Datos Relacional**: **PostgreSQL**. Utilizado para datos estructurados, relaciones complejas y transaccionales (Usuarios, Rutas, Paraderos).
- **Mapas y Geocoding**: **Mapbox GL JS**. Integrado en el frontend para renderizar mapas interactivos, trazar rutas personalizadas y visualización en tiempo real con alto rendimiento.
- **Gestión de Dependencias y Build**: Maven.

---

## 5. Deploy Configurado y Uso de Docker

### Dockerización
El sistema está completamente contenerizado. Cada microservicio cuenta con su propio `Dockerfile` que define un proceso de construcción en dos fases (Multi-stage build):
1. **Fase de Build**: Compila el código Java usando Maven.
2. **Fase de Runtime**: Ejecuta el `.jar` compilado sobre una imagen ligera de Java (`eclipse-temurin:21-jre-alpine` o similar), reduciendo el tamaño de la imagen final y mejorando la seguridad.

Para desarrollo local, se dispone de un archivo `docker-compose.yml` que levanta las bases de datos (PostgreSQL, Redis, RabbitMQ) y, opcionalmente, los servicios interconectados.

### Despliegue en la Nube (Render)
El despliegue está optimizado para plataformas PaaS como **Render**.
- **Web Services**: Cada microservicio se despliega como un "Web Service" independiente.
- **Variables de Entorno (Environment Variables)**: La configuración de conexiones (DB URL, Redis URL, JWT Secret) se inyecta mediante variables de entorno de Render, evitando hardcodear credenciales en el código fuente.
- **Sincronización de Base de Datos**: Spring Data JPA (`ddl-auto=update` o migraciones SQL manuales en el arranque) asegura que la estructura de la base de datos en la nube esté sincronizada con las entidades de código de la rama `develop`.

