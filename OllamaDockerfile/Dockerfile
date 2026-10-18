FROM ollama/ollama

COPY ../run_ollama.sh /tmp/run_ollama.sh

COPY ../saiga.gguf /tmp/saiga.gguf

COPY ../saiga.modelfile /tmp/saiga.modelfile

COPY ../gigachat.gguf /tmp/gigachat.gguf

COPY ../gigachat.modelfile /tmp/gigachat.modelfile

# /lib/systemd/system/ollama.service
# [Service]
# Environment="OLLAMA_MAX_LOADED_MODELS=3"
# Environment="OLLAMA_NUM_PARALLEL=3"

# По дефолту OLLAMA_MAX_LOADED_MODELS=3 но это может привести к попыткам запустить 3 копии модели разом и OOM error
# да, в олламе стоят защиты от такого, но а вдруг
ENV OLLAMA_MAX_LOADED_MODELS=1
# OLLAMA_NUM_PARALLEL для гигачата можно спокойно ставить 4, для сайги вообще по-моему больше чем 1 будет утекать в ОЗУ
ENV OLLAMA_NUM_PARALLEL=3

WORKDIR /tmp

EXPOSE 11434
