FROM gradle:8.9-jdk21 AS builder
WORKDIR /app

# Copiá solo si EXISTEN; si no, eliminá la línea
COPY build.gradle.kts ./
# Quita esta si NO tenés /gradle en el repo
# COPY gradle gradle
# Quita esta si no tenés gradle.properties
# COPY gradle.properties gradle.properties

# Luego el código
COPY src ./src

RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-Dserver.port=${PORT:-8080}"
COPY --from=builder /app/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
