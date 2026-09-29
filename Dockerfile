# ---- build: compile the Spring Boot jar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dependencies first, so Docker caches them and only re-downloads when pom.xml changes.
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
# Tests are skipped here: the only test boots the whole app and needs a live Postgres.
RUN mvn -q clean package -DskipTests

# ---- run: just a Java runtime and the jar ----
FROM eclipse-temurin:21-jre-jammy

# fontconfig + a font: TicketPdfGenerator draws the attendee's name with Java2D, and a
# bare image has no fonts - the PDF would crash or come out without a name.
# curl: used by the docker compose health check.
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --uid 1001 spring
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
USER spring

# headless: no screen on a server (needed for Java2D). MaxRAMPercentage: size the heap
# from the container's memory limit instead of the whole machine's RAM.
ENV JAVA_TOOL_OPTIONS="-Djava.awt.headless=true -XX:MaxRAMPercentage=75"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
