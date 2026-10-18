FROM maven:3.9.8-sapmachine-22 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package

FROM eclipse-temurin:22.0.2_9-jre as final
COPY ../.djl.ai /root/.djl.ai
WORKDIR /app
COPY BotMicroservice/target/BotMicroservice.jar /app/bot.jar
COPY BotMicroservice/src/main/resources/sysprompt.txt /app/sysprompt.txt
COPY BotMicroservice/src/main/resources/ragprompt.txt /app/ragprompt.txt
COPY ../model.onnx_data /app/model.onnx_data
COPY ../multilingual /app/multilingual
ENTRYPOINT ["java", "-jar", "bot.jar"]
