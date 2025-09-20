# ---- build stage ----
FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

# Copiá sólo lo necesario para resolver dependencias y cachear
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle gradle  # si tenés directorio gradle/ (plugins, etc)
RUN gradle --version

# Copiá el resto del proyecto
COPY . .

# Compilar el JAR (sin tests dentro del contenedor)
RUN gradle clean bootJar -x test --no-daemon

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Render inyecta $PORT; hacemos que Spring escuche ahí
ENV JAVA_TOOL_OPTIONS="-Dserver.port=${PORT:-8080}"
# (opcional) zona horaria para que coincida con tu config
ENV TZ=America/Argentina/Buenos_Aires

# Copiamos el JAR construido
COPY --from=builder /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
