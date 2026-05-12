# syntax=docker/dockerfile:1.7

# ---- Stage 1: build ---------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies in a dedicated layer
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -q -B -DskipTests package && \
    cp target/sample-web-app-*.jar app.jar && \
    java -Djarmode=layertools -jar app.jar extract --destination extracted

# ---- Stage 2: runtime -------------------------------------------------------
FROM eclipse-temurin:21-jre-jammy AS runtime

RUN useradd --system --uid 10001 --create-home --shell /usr/sbin/nologin spring

WORKDIR /app
USER spring:spring

COPY --from=build --chown=spring:spring /workspace/extracted/dependencies/         ./
COPY --from=build --chown=spring:spring /workspace/extracted/spring-boot-loader/   ./
COPY --from=build --chown=spring:spring /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/extracted/application/          ./

ENV JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
