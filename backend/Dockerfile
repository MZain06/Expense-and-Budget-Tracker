# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

COPY .mvn/ .mvn/
COPY --chmod=755 mvnw ./
COPY pom.xml ./
RUN ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src/ src/
# Testcontainers can't run inside a Docker build, so the tests run in CI instead.
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app
USER app

COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/actuator/health"]

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]
