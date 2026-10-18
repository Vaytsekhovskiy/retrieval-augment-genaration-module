FROM maven:3.9.8-sapmachine-22 AS builder
WORKDIR /app
COPY RagUtilLib /app/RagUtilLib
COPY ConvertMicroservice /app/ConvertMicroservice
RUN mvn -f /app/RagUtilLib/pom.xml -DskipTests install && \
    mvn -f /app/ConvertMicroservice/pom.xml -DskipTests package

FROM eclipse-temurin:22.0.2_9-jre AS final
WORKDIR /app
COPY requirements.txt /app/requirements.txt
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
      smbclient libreoffice libreoffice-java-common python3 python3-pip && \
    rm -rf /var/lib/apt/lists/*

# The image classifier uses the CPU; Ollama uses the GPU in its own container.
RUN python3 -m pip install --break-system-packages --no-cache-dir \
    --index-url https://download.pytorch.org/whl/cpu -r requirements.txt

COPY .djl.ai /root/.djl.ai
COPY --from=builder /app/ConvertMicroservice/target/ConvertMicroservice.jar /app/convert.jar
COPY ConvertMicroservice/src/main/resources/credentials.txt /app/credentials.txt
COPY model.onnx_data /app/model.onnx_data
COPY multilingual /app/multilingual
COPY ImageClassifier.py /app/ImageClassifier.py
COPY ImageClassifier.pth /app/ImageClassifier.pth
COPY arial.ttf /app/arial.ttf

ENTRYPOINT ["java", "-jar", "convert.jar"]
