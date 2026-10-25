FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src


RUN mvn -B -ntp package -DskipTests \
    && set -eu \
    && find target -maxdepth 1 -type f -name "*.jar" \
       ! -name '*-sources.jar' \
       ! -name '*-javadoc.jar' \
       -print \
       > /tmp/jars.txt \
    && test "$(wc -l < /tmp/jars.txt)" -eq 1 \
    && mv "$(cat /tmp/jars.txt)" target/app.jar

FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app

RUN groupadd --system --gid 1001 appgroup \
&& useradd --system \
    --uid 1001 \
    --gid appgroup \
    --no-create-home \
    --shell /usr/sbin/nologin \
    appuser 
COPY --from=builder --chown=1001:1001 /app/target/app.jar /app/app.jar

USER 1001:1001

EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]