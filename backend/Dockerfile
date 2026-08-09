# ---- build stage -------------------------------------------------------
# Full Maven + JDK image. Nothing from this stage ends up in the final image.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the POM alone first and resolve dependencies. Docker caches this layer,
# so editing source code does not re-download the internet on every build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
# Tests are skipped here on purpose: they need a Docker daemon for
# Testcontainers, which is not available inside an image build. CI runs them.
RUN mvn -B clean package -DskipTests

# ---- runtime stage -----------------------------------------------------
# JRE only, on Alpine. Roughly 200MB instead of ~800MB.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as a non-root user. If the app is ever compromised, the attacker lands
# as "spring" with no privileges rather than as root.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
