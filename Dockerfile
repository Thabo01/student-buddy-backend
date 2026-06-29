# ---- Build stage ----
# Uses a full JDK + Maven image to compile and package the app into a jar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the build descriptor first and pre-fetch dependencies so this layer is
# cached and only re-runs when pom.xml changes (faster rebuilds).
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy sources and build. Skip tests in the image build (run them in CI).
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
# Slim JRE-only image: smaller and a reduced attack surface.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user for safety.
RUN useradd --system --no-create-home appuser
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Activate the production profile by default; everything else comes from env vars.
ENV SPRING_PROFILES_ACTIVE=prod

# Keep heap small enough for Render's free 512 MB container.
ENTRYPOINT ["java", \
  "-Xms128m", "-Xmx400m", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
