# Informe Técnico de Arquitectura: ChapaTuRuta

Este informe describe los detalles técnicos, patrones de diseño, elección de tecnologías, decisiones de arquitectura y la infraestructura configurada para la plataforma **ChapaTuRuta**.

---

## 1. Patrón Arquitectónico y Estilo de Microservicios

### 1.1 Microservicios
La plataforma se encuentra distribuida en microservicios independientes, desacoplados por dominio y base de datos propia, lo que facilita el escalado horizontal y el despliegue independiente:
- **`api-gateway`**: El punto de entrada centralizado de la plataforma que actúa como proxy inverso y distribuye las peticiones HTTP a los servicios correspondientes (`routing-service`, `identity-service`, `tracking-service`).
- **`identity-service`**: Microservicio encargado del registro, autenticación (generación de tokens JWT) y gestión del ciclo de vida de los usuarios (Pasajeros, Conductores y Managers) y empresas.
- **`routing-service`**: Microservicio para la gestión del catálogo de rutas y paraderos geolocalizados, así como del motor de búsqueda de trayectos directos o con trasbordos.
- **`tracking-service`**: Microservicio encargado del tracking GPS de vehículos en tiempo real, cálculo de la demanda de pasajeros esperando en los paraderos y cálculo del tiempo estimado de llegada (ETA).

### 1.2 Arquitectura Hexagonal (Puertos y Adaptadores)
Cada microservicio está estructurado siguiendo los principios de la Arquitectura Hexagonal para aislar las reglas de negocio del software de los detalles de infraestructura (base de datos, controladores REST, colas de mensajería):
1. **Domain (Núcleo)**: Contiene los modelos de negocio puros (`User`, `Route`, `RouteStop`, `CheckInEvent`) y las interfaces de los repositorios sin dependencias de frameworks.
2. **Application (Casos de Uso)**: Define las interfaces e implementaciones de los casos de uso (`RegisterUserUseCase`, `ManageRouteUseCase`, `TrackingCommandService`) y los DTOs que fluyen hacia afuera.
3. **Infrastructure (Adaptadores)**: Implementa los detalles técnicos:
   - **Adaptadores de Entrada (Primary)**: Controladores REST que manejan las peticiones HTTP (`UserController`, `RouteController`).
   - **Adaptadores de Salida (Secondary)**: Adaptadores de persistencia JPA (`UserRepositoryAdapter`), clientes de caché y publicadores/suscriptores de mensajería.

### 1.3 Patrones de Diseño Utilizados
- **Repository Pattern**: Implementado a través de adapters para desacoplar el acceso a datos.
- **CQRS (Command Query Responsibility Segregation) básico**: Visible en el `tracking-service` dividido en `TrackingCommandService` (para escrituras/check-ins) y `TrackingQueryService` (para lecturas de ETA y ubicaciones).
- **Builder Pattern**: Utilizado extensamente para la instanciación inmutable de entidades y DTOs.
- **Cascade Save y Orphan Removal**: Configurado en JPA para permitir que la ruta funcione como raíz del agregado y persista sus paradas de manera atómica.

---

## 2. Tecnologías de Almacenamiento y Mensajería

### 2.1 Uso de Redis (Caching de Alta Velocidad y Demanda Activa)
El microservicio `tracking-service` utiliza **Redis** (integrado mediante Spring Data Redis / Jedis) como almacén in-memory clave-valor debido a su necesidad de lecturas y escrituras de latencia sub-milisegundo.
- **Tracking en Tiempo Real:** Las coordenadas GPS del último Check-In del conductor se guardan con la clave `route:{routeId}:driver:{driverId}:location`, lo que permite que el mapa del pasajero obtenga la posición actual del bus instantáneamente.
- **Demanda Activa de Pasajeros:** Cuando un pasajero pulsa "Esperar Bus", se inserta una llave en Redis con la estructura `route:{routeId}:stop:{stopId}:passenger:{passengerId}` con un TTL (tiempo de vida) específico. El backend calcula la demanda activa buscando las llaves activas mediante patrones de coincidencia rápida (`keys`).
- **Cálculo de ETA Ultrarrápido:** Se recuperan las últimas ubicaciones desde Redis y se calcula el ETA combinándolo con APIs de geolocalización.

### 2.2 Uso de RabbitMQ (Desacoplamiento Orientado a Eventos - EDA)
**RabbitMQ** se utiliza como broker de mensajería AMQP para implementar una arquitectura orientada a eventos. Esto evita que llamadas de sincronización bloqueen el hilo de ejecución principal y permite notificaciones reactivas.
- **Flujo de Eventos:**
  1. Cuando un conductor realiza un **Check-In** en un paradero, el `TrackingCommandService` procesa el comando, actualiza Redis y publica un evento `VehicleCheckInEvent` serializado a JSON en el exchange `tracking.exchange` usando la clave de enrutamiento `tracking.routing.key`.
  2. El exchange distribuye el evento a la cola `tracking.queue`.
  3. El `NotificationWorker` en el mismo o en otros microservicios consume de forma asíncrona de `tracking.queue` (`@RabbitListener`) y se encarga de disparar las notificaciones de proximidad a los dispositivos de los pasajeros que están esperando en ese paradero.

---

## 3. Contenedores y Configuración de Despliegue

### 3.1 Uso de Docker
Cada microservicio cuenta con su respectivo archivo `Dockerfile` utilizando imágenes base optimizadas de Java (como `eclipse-temurin` y `corretto` sobre Alpine) para compilar y empaquetar la aplicación en un artefacto JAR ejecutable. Esto permite que los servicios corran exactamente igual en local que en la nube, aislando las variables del sistema operativo.

### 3.2 Pipeline de Despliegue Configurado (Render)
El despliegue está automatizado e integrado de forma continua (CI/CD) en la nube a través de **Render**:
- **Monolito de Código / Repositorio Único:** Al subir código al repositorio principal de GitHub en la rama `develop`, Render detecta los cambios.
- **Servicios Web Independientes:** Cada microservicio está configurado en Render como un servicio web independiente con su propia ruta de origen (`identity-service`, `routing-service`, `tracking-service`).
- **Variables de Entorno Inyectadas:** Render inyecta dinámicamente las credenciales necesarias en producción:
  - `DATABASE_URL`, `DATABASE_USER` y `DATABASE_PASSWORD` para las conexiones PostgreSQL.
  - `REDIS_HOST`, `REDIS_PORT` y `REDIS_PASSWORD` para conectar con el clúster de caché Redis (alojado en Upstash).
  - `RABBITMQ_HOST`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD` y `RABBITMQ_VHOST` para conectar con el broker RabbitMQ (alojado en CloudAMQP).
  - El puerto dinámico `${PORT}` que expone cada contenedor de forma pública.
