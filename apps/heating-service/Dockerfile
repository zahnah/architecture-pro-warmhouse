FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/heating-service.jar .

# 8082 - HTTP (actuator health), 9090 - gRPC
EXPOSE 8082 9090

CMD ["java", "-jar", "heating-service.jar"]
