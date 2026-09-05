# ============================================================
# BUILD STAGE
# ============================================================

FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Give Maven wrapper execute permission
RUN chmod +x mvnw

# Download dependencies first
RUN ./mvnw dependency:go-offline -DskipTests

# Copy source code
COPY src src

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests


# ============================================================
# RUN STAGE
# ============================================================

FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy generated JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]