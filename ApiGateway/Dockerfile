FROM maven:3.9.8-sapmachine-22 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package

FROM eclipse-temurin:22.0.2_9-jre as pre-final
WORKDIR /app
COPY ApiGateway/target/ApiGateway.jar /app/gateway.jar


EXPOSE 8765
ENTRYPOINT ["java", "-jar", "gateway.jar"]