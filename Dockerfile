# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first for faster incremental builds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as an unprivileged user and ensure the working dir (incl. the H2 data
# directory used by the default profile) is writable by it.
RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/animal-app-*.jar app.jar
RUN mkdir -p /app/data && chown -R appuser:appuser /app
USER appuser

# The app honours the PORT env var (set by Railway/Render/etc.), defaulting to 8080.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
