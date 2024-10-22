#!/bin/bash

# Step 1: Clean and package the application using Maven
echo "Building the project..."
mvn clean package -DskipTests

# Check if the JAR was built successfully
if [ $? -ne 0 ]; then
    echo "Maven build failed. Exiting..."
    exit 1
fi

# Step 2: Build Docker Images
echo "Building Docker images..."
docker-compose build

# Check if Docker images were built successfully
if [ $? -ne 0 ]; then
    echo "Docker image build failed. Exiting..."
    exit 1
fi

# Step 3: Start Docker containers using Docker Compose
echo "Starting Docker containers..."
docker-compose up -d

# Give some time for the services to start
sleep 10

# Step 4: Run API tests
echo "Running API tests..."
mvn test

# Check if the CURL command was successful
if [ $? -ne 0 ]; then
    echo "API test failed. Exiting..."
    docker-compose down
    exit 1
fi

echo "API tests passed!"

# Step 5: Check container status
echo "Checking container status..."
docker ps

# Step 6: Output success message
echo "Docker containers are up and running. You can now use Postman to call your APIs."
echo "Automation script completed successfully!"