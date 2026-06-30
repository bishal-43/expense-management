# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies for caching (this speeds up subsequent builds)
RUN mvn dependency:go-offline -B
COPY src ./src
COPY frontend ./frontend
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install native Tesseract OCR library (required by Tess4J JNA) and curl
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Pre-download Tesseract English training data into /app/tessdata to prevent network dependency at runtime
RUN mkdir -p /app/tessdata && \
    curl -L -o /app/tessdata/eng.traineddata https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/main/eng.traineddata

# Copy the built jar from the build stage
COPY --from=build /app/target/expense-management-*.jar app.jar

# Create directory for file uploads
RUN mkdir -p /app/uploads

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
