# Etapa 1: compilar con Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Etapa 2: runtime con JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Djava.security.egd=file:/dev/./urandom -Djava.net.preferIPv4Stack=true -XX:MaxRAMPercentage=75.0 -XX:+TieredCompilation -XX:TieredStopAtLevel=1"
ENTRYPOINT ["java", "-jar", "app.jar"]
