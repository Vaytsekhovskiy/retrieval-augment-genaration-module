FROM maven:3.9.8-sapmachine-22 AS builder
WORKDIR /app
COPY DiscoveryServer /app/DiscoveryServer
RUN mvn -f /app/DiscoveryServer/pom.xml -DskipTests package

FROM eclipse-temurin:22.0.2_9-jre AS final
WORKDIR /app
COPY --from=builder /app/DiscoveryServer/target/DiscoveryServer.jar /app/discovery.jar


EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery.jar"]
