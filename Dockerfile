# Use the preferred base image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file to the container
COPY target/transaction-service-0.0.1-SNAPSHOT.jar /app/transaction-service.jar

# Expose the port the service will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "transaction-service.jar"]
