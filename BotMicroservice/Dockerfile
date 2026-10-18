FROM maven:3.9.8-sapmachine-22 AS builder
WORKDIR /app
COPY RagUtilLib /app/RagUtilLib
COPY BotMicroservice /app/BotMicroservice
RUN mvn -f /app/RagUtilLib/pom.xml -DskipTests install && \
    mvn -f /app/BotMicroservice/pom.xml -DskipTests package

FROM eclipse-temurin:22.0.2_9-jre AS final
COPY .djl.ai /root/.djl.ai
WORKDIR /app
COPY --from=builder /app/BotMicroservice/target/BotMicroservice.jar /app/bot.jar
COPY BotMicroservice/src/main/resources/sysprompt.txt /app/sysprompt.txt
COPY BotMicroservice/src/main/resources/ragprompt.txt /app/ragprompt.txt
COPY model.onnx_data /app/model.onnx_data
COPY multilingual /app/multilingual
ENTRYPOINT ["java", "-jar", "bot.jar"]
