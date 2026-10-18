#!/bin/bash
echo "Starting Ollama server..."
ollama serve &  # Запускаем сервер в фоновом режиме
SERVER_PID=$!   # Сохраняем PID сервера
ollama create saiga -f /tmp/saiga.modelfile
ollama create gigachat -f /tmp/gigachat.modelfile
echo "Ollama server started"

wait $SERVER_PID  # Ожидаем завершения сервера
# ДОЛЖНЫ БЫТЬ ПРОБЕЛЫ UNIX!!!
# Иначе ошибка bash: ./run_ollama.sh: /bin/bash^M: bad interpreter: No such file or directory
# Должны быть LF line separators!
