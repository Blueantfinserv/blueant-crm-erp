FROM eclipse-temurin:21-jdk-alpine as builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make Maven wrapper executable inside the container
RUN chmod +x mvnw

# Download dependencies offline (cache layer)
RUN ./mvnw dependency:go-offline

# Copy source code and build the package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/blueant-crm-erp-0.0.1-SNAPSHOT.jar app.jar

# Expose server port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
