# Step 1: Build the application
FROM amazoncorretto:21-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Step 2: Run the application
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=build /app/target/studycollabration-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]