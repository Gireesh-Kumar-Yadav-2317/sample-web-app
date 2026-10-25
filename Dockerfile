
# Stage 1: Build the Spring Boot application
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependency downloads unless pom.xml changes
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# Copy application source
COPY src ./src

# Build a clean executable JAR.
# Tests are run separately in the CI test job.
RUN mvn -B -ntp clean package -DskipTests \
    && set -eu \
    && find target -maxdepth 1 -type f -name "*.jar" \
       ! -name '*-sources.jar' \
       ! -name '*-javadoc.jar' \
       -print > /tmp/jars.txt \
    && test "$(wc -l < /tmp/jars.txt)" -eq 1 \
    && mv "$(cat /tmp/jars.txt)" target/app.jar

# Stage 2: Minimal Java runtime
FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

# Refresh OS security patches and create a dedicated non-root user
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends ca-certificates \
    && groupadd --system --gid 1001 appgroup \
    && useradd --system \
       --uid 1001 \
       --gid appgroup \
       --no-create-home \
       --shell /usr/sbin/nologin \
       appuser \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder --chown=1001:1001 \
    /app/target/app.jar /app/app.jar

USER 1001:1001

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
