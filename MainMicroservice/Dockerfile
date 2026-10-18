FROM maven:3.9.8-sapmachine-22 AS builder
WORKDIR /app
COPY RagUtilLib /app/RagUtilLib
COPY MainMicroservice /app/MainMicroservice
RUN mvn -f /app/RagUtilLib/pom.xml -DskipTests install && \
    mvn -f /app/MainMicroservice/pom.xml -DskipTests package

FROM eclipse-temurin:22.0.2_9-jre AS final
WORKDIR /app
COPY --from=builder /app/MainMicroservice/target/MainMicroservice.jar /app/main.jar
COPY MainMicroservice/src/main/resources/static MainMicroservice/src/main/resources/static
COPY MainMicroservice/src/main/resources/templates MainMicroservice/src/main/resources/templates


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "main.jar"]
