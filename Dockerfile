# ---- build stage ----
FROM gradle:8.9-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle gradle
RUN gradle --version
COPY . .
# compila sin tests para el contenedor (puedes quitar -x test si ya te pasan en CI)
RUN ./gradlew clean bootJar -x test

# ---- runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=America/Argentina/Buenos_Aires
# Render expone el puerto en la var $PORT. No hace falta EXPOSE, pero no molesta:
EXPOSE 8080
# Copiamos el jar construido
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar /app/app.jar

# Importante: Spring debe escuchar en 0.0.0.0:$PORT en Render
ENV JAVA_TOOL_OPTIONS="-Dserver.port=${PORT:-8080}"
ENTRYPOINT ["java","-jar","/app/app.jar"]
