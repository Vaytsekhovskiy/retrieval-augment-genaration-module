FROM maven:3.9.8-sapmachine-22 as builder
WORKDIR /app
COPY . /app/.
RUN mvn -f /app/pom.xml clean package

FROM eclipse-temurin:22.0.2_9-jre as final
COPY ../.djl.ai /root/.djl.ai
WORKDIR /app
COPY ConvertMicroservice/target/ConvertMicroservice.jar /app/convert.jar
COPY ConvertMicroservice/src/main/resources/credentials.txt /app/credentials.txt
COPY ../model.onnx_data /app/model.onnx_data
COPY ../multilingual /app/multilingual
COPY ImageClassifier.py /app/ImageClassifier.py
COPY requirements.txt /app/requirements.txt
COPY ImageClassifier.pth /app/ImageClassifier.pth
COPY arial.ttf /app/arial.ttf

RUN apt-get update && apt-get -y upgrade
RUN apt-get install -y --no-install-recommends smbclient
# Method1 - installing LibreOffice and java
RUN apt-get --no-install-recommends install libreoffice -y
RUN apt-get install -y libreoffice-java-common

RUN apt-get install -y python3
RUN apt -y install python3-pip
RUN pip install --break-system-packages -r requirements.txt

ENTRYPOINT ["java", "-jar", "convert.jar"]
