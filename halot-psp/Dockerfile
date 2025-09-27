# Use the OpenJDK image based on Alpine Linux
FROM eclipse-temurin:23-jre-alpine

# Copy the compiled JAR file into the container
COPY target/halot-psp-0.0.14.jar app.jar

# Specify the port that will be listened to.
EXPOSE 8080

# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]
