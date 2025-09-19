FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy only POM to cache dependencies
COPY ./pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY ./ .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Create directory for data storage
RUN mkdir -p /app/data

COPY --from=build /app/target/contract-analysis-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]