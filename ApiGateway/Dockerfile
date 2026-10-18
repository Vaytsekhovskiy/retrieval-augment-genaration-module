FROM maven:3.9.8-sapmachine-22 AS builder
WORKDIR /app
COPY RagUtilLib /app/RagUtilLib
COPY ApiGateway /app/ApiGateway
RUN mvn -f /app/RagUtilLib/pom.xml -DskipTests install && \
    mvn -f /app/ApiGateway/pom.xml -DskipTests package

FROM eclipse-temurin:22.0.2_9-jre AS final
WORKDIR /app
COPY --from=builder /app/ApiGateway/target/ApiGateway.jar /app/gateway.jar


EXPOSE 8765
ENTRYPOINT ["java", "-jar", "gateway.jar"]
