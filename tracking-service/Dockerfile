# 1. Usamos una imagen oficial de Java 21 (Temurin) optimizada
FROM eclipse-temurin:21-jdk

# 2. Definimos el directorio de trabajo dentro del contenedor de Render
WORKDIR /app

# 3. Copiamos todos los archivos del microservicio al contenedor
COPY . .

# 4. Damos permisos de ejecución al wrapper de Maven (evita fallos en Render)
RUN chmod +x ./mvnw

# 5. Compilamos el proyecto empaquetándolo en un archivo JAR (omitiendo pruebas para mayor velocidad)
RUN ./mvnw clean package -DskipTests

# 6. Comando final que Render ejecutará para levantar la aplicación
ENTRYPOINT ["sh", "-c", "java -jar target/*-SNAPSHOT.jar"]