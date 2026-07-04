# Guía de Pruebas de Interfaz de Usuario (End-to-End) 🚍

Esta guía describe los pasos exactos para probar el flujo completo de **ChapaTuRuta** utilizando la interfaz web (Frontend) y los microservicios (Backend) desde cero hasta la finalización.

---

## 🛠️ Requisitos Previos y Preparación

1. **Iniciar el Backend**:
   - Asegúrate de que los microservicios (`identity-service`, `routing-service`, `tracking-service`, `api-gateway`) estén corriendo localmente (puertos por defecto o configurados mediante Render).
   - Redis y RabbitMQ deben estar activos.
2. **Iniciar el Frontend**:
   - Ejecuta `npm start` en el directorio de `chapaturuta-frontend`.
   - Abre la aplicación en `http://localhost:3000`.

---

## 📋 Flujo de Pruebas Paso a Paso

### Paso 1: Limpieza del Estado de Prueba (Recomendado)
1. Inicia sesión como **Manager** (si ya tienes una cuenta) o crea una nueva.
2. Ve al panel de control y haz clic en **"Resetear Base de Datos"** (si está disponible) o utiliza el botón para eliminar tu cuenta actual. Esto garantiza comenzar la prueba sin interferencias de ejecuciones anteriores.

### Paso 2: Registro del Manager y Creación de la Empresa
1. Dirígete a la ruta de registro (`http://localhost:3000/register`).
2. Completa los campos:
   - **Nombre**: `Manager E2E`
   - **Correo**: `manager@e2e.com`
   - **Contraseña**: `manager123`
   - **Rol**: `MANAGER`
3. Haz clic en **Registrarse**. Serás redirigido al formulario de Onboarding.
4. Registra tu empresa de transporte:
   - **Nombre de Empresa**: `Transportes Lima Metropolitana`
   - **RUC**: `20123456789`
5. Haz clic en **Crear Empresa**. Ahora verás el panel de control del Manager.

### Paso 3: Cargar Ruta Detallada con Paraderos (Archivo JSON)
1. En el panel del Manager, busca la sección **"Crear Ruta"** o **"Subir Ruta"**.
2. Haz clic en el botón para subir un archivo.
3. Selecciona el archivo de ejemplo `sample_route_15_stops.json` localizado en la raíz del frontend.
4. El sistema procesará el archivo y renderizará la ruta completa con sus 15 paraderos en el mapa interactivo.
5. Copia el **ID de la Ruta** (por ejemplo: `8a7b6c5d-...`) que aparece en la tabla de rutas creadas.

### Paso 4: Registrar y Asignar Conductor
1. Aún en el panel del Manager, ve a la sección **"Gestión de Conductores"**.
2. Completa el formulario de registro para un nuevo chofer:
   - **Nombre**: `Luis Chofer`
   - **Correo**: `luis@transporte.com`
   - **Contraseña**: `driver123`
   - **Ruta Asignada**: Pega el **ID de la Ruta** copiado en el paso anterior.
3. Haz clic en **Registrar Conductor**.

### Paso 5: Registro del Pasajero y Simulación de Espera
1. Abre una ventana de navegador en modo incógnito (o usa otro navegador) y ve a `http://localhost:3000/register`.
2. Registra un Pasajero:
   - **Nombre**: `Pasajero E2E`
   - **Correo**: `pasajero@e2e.com`
   - **Contraseña**: `passenger123`
   - **Rol**: `PASSENGER`
3. Al registrarse, verás el mapa de pasajeros.
4. Selecciona la ruta creada (`Transportes Lima Metropolitana`).
5. En el menú desplegable de paraderos, elige un paradero intermedio (por ejemplo: `Paradero Izaguirre` o el paradero número 5).
6. Haz clic en **"Esperar Bus"**. El marcador del paradero cambiará de color o mostrará una alerta indicando que estás esperando activamente en esa ubicación.

### Paso 6: Monitorear Demanda en el Panel del Manager
1. Regresa a la pestaña del **Manager**.
2. En la lista de rutas o mapa de demanda, verifica que el paradero seleccionado por el pasajero ahora muestra un recuento de **1 Pasajero Esperando** en tiempo real.

### Paso 7: Consola de Seguimiento del Conductor
1. Cierra sesión en el navegador principal del Manager o abre otra pestaña de incógnito e ingresa a `http://localhost:3000/login`.
2. Inicia sesión con las credenciales del conductor:
   - **Correo**: `luis@transporte.com`
   - **Contraseña**: `driver123`
3. Serás redirigido a la **Consola del Conductor**. Verás la ruta asignada y un botón para **"Iniciar Ruta"**.
4. Haz clic en **Iniciar Ruta**. La consola mostrará la lista de paraderos y tu posición actual.

### Paso 8: Check-In del Conductor y Desaparición de Demanda
1. El conductor simula avanzar por el recorrido.
2. Cuando el conductor llegue cerca del paradero seleccionado por el pasajero (Paso 5), haz clic en el botón **"Check-In"** de ese paradero específico en la consola del chofer.
3. Esto enviará las coordenadas a Redis y notificará al sistema.
4. **Verificación**: 
   - **En la interfaz del Pasajero**: El pasajero recibirá una notificación visual flotante en su mapa y aparecerá de inmediato una pantalla de abordaje exitoso: **"¡Buen viaje!"**.
   - **En el Dashboard del Manager**: El recuento de demanda de pasajeros esperando en ese paradero **se reducirá a 0 de forma inmediata**, ya que el abordaje del pasajero elimina automáticamente la demanda en espera en Redis.

### Paso 9: Finalización y Limpieza
1. El conductor sigue registrando check-ins hasta llegar al final de la ruta y hace clic en **"Finalizar Recorrido"**.
2. Si deseas realizar más pruebas limpias, el Pasajero y el Manager pueden borrar sus cuentas de prueba haciendo clic en **"Eliminar Cuenta"** desde sus respectivos paneles de configuración para purgar la base de datos PostgreSQL.

---
Guía generada de manera oficial para la validación E2E de ChapaTuRuta.
