# syntax=docker/dockerfile:1

# ---- Build stage: compile the app and split the jar into layers ----
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Download Maven and the dependencies first. Docker caches this layer,
# so it only runs again when pom.xml changes.
COPY .mvn/ .mvn/
COPY --chmod=755 mvnw ./
COPY pom.xml ./
RUN ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

# The tests run in CI instead: Testcontainers needs Docker, which a Docker build doesn't have.
COPY src/ src/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package \
    && cp target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---- Run stage: a JRE and the app, nothing else ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as an ordinary user, not root.
RUN addgroup -S app && adduser -S -G app app
USER app

# Dependencies change least often, so they go in first and stay cached.
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/actuator/health"]

# Let the JVM use up to 75% of the container's memory, rather than the default 25%.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "application.jar"]