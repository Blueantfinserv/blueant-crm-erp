FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and configuration
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Ensure execute permissions (both chmod and running via sh as fallback/double-guarantee)
RUN chmod +x mvnw

# Download dependencies offline (cache layer)
RUN sh ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the production jar package
RUN sh ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the exact compiled jar from builder stage
COPY --from=builder /app/target/blueant-crm-erp-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
