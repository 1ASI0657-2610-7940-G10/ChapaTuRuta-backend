# Informe de Arquitectura: ChapaTuRuta

## 1. Visión General de la Arquitectura
El sistema **ChapaTuRuta** ha sido diseñado adoptando una **Arquitectura de Microservicios (Cloud-Native)** desde su concepción. 

**Decisión frente a la Arquitectura Monolítica:**
Se descartó inicialmente construir la solución como un Monolito debido a la gran asimetría de escalabilidad entre los diferentes dominios del negocio. Mientras que la autenticación de usuarios (*Identity Service*) presenta una carga predecible, la recepción continua de coordenadas GPS de los vehículos (*Tracking Service*) requiere una altísima capacidad de concurrencia y escalabilidad horizontal. Al empezar como un ecosistema fragmentado, maximizamos los **beneficios** de los microservicios (despliegue independiente, aislamiento de fallos, bases de datos separadas) y evitamos a futuro el costoso proceso de *Monolithic decomposition* o *Refactoring*, asumiendo de forma proactiva sus **desventajas** (complejidad transaccional y monitoreo distribuido).

### Microservicios Principales:
1. **API Gateway**: Actúa como el punto de entrada único para todas las solicitudes de los clientes (SPA frontend). Se encarga del enrutamiento de peticiones, manejo de CORS y validación inicial de tokens JWT.
2. **Identity Service**: Gestiona la autenticación, autorización (RBAC: Manager, Driver, Passenger) y el registro de usuarios y empresas.
3. **Routing Service**: Administra las rutas estáticas y paraderos, gestionando la información geográfica estructural.
4. **Tracking Service**: Maneja la ubicación en tiempo real de los vehículos, los check-ins en paraderos y el cálculo de tiempos de llegada estimados (ETA).

---

## 2. Decisiones de Arquitectura

### Arquitectura Hexagonal y Clean Architecture
Dentro de cada microservicio, se implementa de manera estricta la **Arquitectura Hexagonal (Ports and Adapters)** alineada con los principios de **Clean Architecture**. Esto asegura un alto desacoplamiento entre la lógica de negocio y las tecnologías externas.

- **Capa de Dominio (Domain)**: Contiene las entidades del negocio puras y las interfaces (Puertos). Al no depender de frameworks externos (como Spring o JPA), permite realizar *Pruebas Unitarias exhaustivas (Testing de microservicios)* validando reglas de negocio sin infraestructura.
- **Capa de Aplicación (Use Cases)**: Orquesta la lógica de negocio consumiendo los puertos.
- **Capa de Infraestructura (Adapters)**: Contiene los repositorios JPA, Controladores REST, y conexiones a Message Brokers.

### Patrones de Microservicios Aplicados
- **API Gateway Pattern**: Centraliza el acceso y la seguridad perimetral.
- **Database per Service**: Cada microservicio (Identity, Routing) tiene su propia base de datos (o esquema lógico aislado), garantizando completa autonomía y evitando cuellos de botella compartidos.
- **Transacciones Distribuidas y Sagas (Coreografía)**: Debido a la separación de bases de datos, las operaciones que involucran a múltiples microservicios evitan bloqueos sincrónicos y utilizan el **Patrón Saga**. Esto se logra emitiendo eventos asíncronos que permiten la *consistencia eventual (Eventual Consistency)* y la ejecución de transacciones compensatorias en caso de fallos.

---

## 3. Manejo de Datos: CQRS y Event Sourcing

El rastreo en tiempo real representa el mayor desafío algorítmico y de datos. Para ello, se emplean patrones avanzados de arquitectura guiados por eventos (**Event-Driven Architecture**):

### CQRS (Command Query Responsibility Segregation)
En el *Tracking Service*, hemos separado drásticamente los modelos de escritura y lectura:
- **Modelo de Comando (Write)**: Cuando un conductor emite su ubicación GPS, la escritura no va directamente a una base de datos relacional. La petición se encola asíncronamente en un *Message Broker* (**RabbitMQ**). Esto evita saturar el sistema de almacenamiento con escrituras concurrentes intensivas.
- **Modelo de Consulta (Read)**: Un worker procesa los mensajes de RabbitMQ y actualiza el estado temporal en **Redis**, una base de datos en memoria clave-valor. Cuando un pasajero consulta "¿A qué hora llega mi bus?", el sistema realiza una lectura a velocidad sub-milisegundo directamente desde Redis, aislada del tráfico de escritura.

### Event Sourcing
La transmisión de las ubicaciones GPS de los conductores asimila principios de **Event Sourcing**. Cada reporte o 'check-in' no simplemente sobrescribe el campo de "ubicación actual", sino que es concebido como un evento inmutable añadido a un log histórico. Esto permite auditar la ruta seguida por el vehículo, reconstruir incidentes pasados y medir la demanda en diferido.

---

## 4. Elección de Tecnologías
- **Backend Framework**: **Spring Boot 3 (Java 21)** (Soporte nativo para Virtual Threads, ideal para alta concurrencia).
- **Frontend**: **React.js** (Interfaces reactivas) y **Mapbox GL JS** (Renderizado WebGL de alto rendimiento para los mapas).
- **Relacional**: **PostgreSQL** (Persistencia estructurada y transaccional).
- **Memoria y Caché**: **Redis** (Consultas masivas en tiempo real).
- **Message Broker**: **RabbitMQ** (Integración asíncrona mediante Publish-Subscribe).

---

## 5. Despliegue, DevOps y Estrategia Cloud

### Dockerización
Se implementa una estrategia nativa de contenedores (*Containerized Deployment*). Cada microservicio utiliza *Multi-stage builds* en su `Dockerfile`.
1. **Fase de Build**: Compila con Maven de forma aislada.
2. **Fase de Runtime**: Ejecuta sobre imágenes ligeras (`eclipse-temurin:21-jre-alpine`), reduciendo drásticamente la superficie de ataque y el tamaño.

### Serverless Architecture y Multi-Cloud
En lugar de aprovisionar infraestructura tradicional (IaaS, ej. instancias EC2 manuales) que exige mantenimiento operativo, la solución de despliegue se apoya en servicios en la nube gestionados (**Serverless Platform Categories**), conformando un entorno **Multi-Cloud**:

- **PaaS (Platform as a Service)**: Usamos **Render.com** para desplegar los contenedores Docker de Spring Boot. Render provee orquestación, auto-escalado horizontal y balanceo de carga sin preocuparnos por el sistema operativo.
- **BaaS (Backend as a Service)**: La persistencia de datos delega la gestión a bases de datos *Serverless*:
  - **Supabase** para PostgreSQL (Escalamiento automático de conexiones JDBC).
  - **Upstash** para Redis (Facturación por request y despliegue distribuido).
  - **CloudAMQP** para RabbitMQ (Message broker as a Service).

### Infrastructure as Code (IaC) - Trabajo Futuro
Actualmente, la infraestructura Multi-Cloud (Render, Supabase, Upstash) está provisionada mediante configuración declarativa desde los paneles de los proveedores. Como evolución en la *Gestión de la Configuración del Entorno*, el próximo paso arquitectónico incluye adoptar **Infrastructure as Code (IaC)** mediante herramientas como **Terraform**. Esto permitirá versionar la infraestructura como código, facilitando la replicación exacta de entornos (Desarrollo, QA, Producción) y agilizando la recuperación ante desastres de manera automatizada.
