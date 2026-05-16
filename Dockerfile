# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

# Give execute permission to mvnw
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/studycollabration-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]