FROM maven:3.9.8-sapmachine-22 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package

FROM eclipse-temurin:22.0.2_9-jre as pre-final
WORKDIR /app
COPY DiscoveryServer/target/DiscoveryServer.jar /app/discovery.jar


EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery.jar"]