FROM maven:3.9.8-sapmachine-22 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package

FROM eclipse-temurin:22.0.2_9-jre as pre-final
WORKDIR /app
COPY MainMicroservice/target/MainMicroservice.jar /app/main.jar
COPY MainMicroservice/src/main/resources/static MainMicroservice/src/main/resources/static
COPY MainMicroservice/src/main/resources/templates MainMicroservice/src/main/resources/templates


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "main.jar"]