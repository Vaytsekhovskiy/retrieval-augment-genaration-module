## Запуск

### 1. Скачать postgreSQL (желательно 16 версии). Установить пароль 
```
89534536403
```
### 2. Установить pgvector расширение на postgreSQL

#### Установка PgVector

Для того, чтобы обеспечить поддержку хранения и поиска многомерных векторов, представленных в базе данных PostgreSQL, необходимо установить расширение [PgVector](https://github.com/pgvector/pgvector). После установки [PostgreSQL](https://www.postgresql.org/download/) (*тестировалось на версии 16.10*), перейдем к непосредственной установке расширения:
- Открываем от **имени администратора** 'x64 Native Tools Command Prompt for VS' через поиск ПК
- Вводим следующие команды в командную строку:

```
set "PGROOT=C:\Program Files\PostgreSQL\16"
cd %TEMP%
git clone --branch v0.8.1 https://github.com/pgvector/pgvector.git
cd pgvector
nmake /F Makefile.win
nmake /F Makefile.win install
```

- При отсутствии каких либо ошибок в командной строке, перезапустим PostgreSQL и перейдем в левом меню в *Servers > PostgreSQL > Databases > postgres > Extensions (ПКМ по этой вкладке) > Create > Extension > В поле Name вводим 'vector' > Save*

После этого наша БД может работать с приложением ElectronHelper

#### Troubleshooting & FAQ по установке PgVector

При возникновении каких-либо ошибок пройдитесь по всем этапам этого списка и выполните:

- Запуск именно **x64** Native Tools Command Prompt for VS, НЕ x86, обязательно **от имени администратора**.

- Введем следующую команду.

```
cd %TEMP%
cd pgvector
nmake /F Makefile.win clean
```

- Убедимся, что в set "PGROOT=C:\Program Files\PostgreSQL\(==ВЕРСИЯ==)" - стоит корректная версия нашей программы, а после введем снова.  

```
set "PGROOT=C:\Program Files\PostgreSQL\16"
cd %TEMP%
git clone --branch v0.8.1 https://github.com/pgvector/pgvector.git
cd pgvector
nmake /F Makefile.win
nmake /F Makefile.win install
```

- Перезапустим postgreSQL, для отображения нашего расширения во вкладке.

*При невозможности открыть x64 Native Tools Command Prompt for VS необходимо:* 
- Убедиться что Visual Studio установлен
- Открыть Visual Studio Installer; В установленном продукте рабочей версии Visual Studio нажать ЛКМ на *Изменить*; 
- Во вкладке *Классические и мобильные приложения* выбрать и установить следующие компоненты: **Разработка классических приложений на C++** , **Разработка классический приложений.NET**

### 3. Скачать jdk, настроить path переменную (JAVA_HOME), а также установить переменную среды PYTORCH_FLAVOR=cpu. 
Стандартный путь: C:\Program Files\Java\jdk-v (лучше скачивай 22 версию)
### 4. Скачать мавен

### 5. Скачать kafka вместе с zookeeper или KRaft (без разницы), проверенная версия 2.13-2.6.0

#### 5.1. Настроить, папки, где будет кеш контролера и брокера. (Например, для kafka и zookepeer необходимо открыть config/zookepeer.properties и config/server.properties внутри файла вместо tmp директории указать свою директорию)

#### 5.2. В папке с kafka (Windows) выполнить следующие команды
```
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties.
```
```
.\bin\windows\kafka-server-start.bat .\config\server.properties
```
Для Linux
```
./bin/zookeeper-server-start.sh ./config/zookeeper.properties
```
```
 ./bin/kafka-server-start.sh ./config/server.properties
```
### 6. Скачать intfloat/multilingual-e5-large (папка onnx): 
 [https://huggingface.co/intfloat/multilingual-e5-large/tree/main/onnx](https://huggingface.co/intfloat/multilingual-e5-large-instruct/tree/main/onnx)
### 7. Поместить model.onnx_data в корневую папку проекта
### 8. Создать папку multilingual в корне проекта и поместить все скачанные файлы (intfloat/multilingual-e5-large-instruct (папка onnx))
### 9. Скачать Ollama:
```
https://ollama.com/download
```

### 10. Запустить олламу.
### 11. Скачать любую gguf модель и переименовать её в saiga.gguf, засунуть в корень проекта 
 (для тестирования хорошо себя показала https://huggingface.co/IlyaGusev/saiga_llama3_8b_gguf/blob/main/model-q4_K.gguf)
### 12. После добавить модель ИИ в контекст олламы через .modelfile:
```
ollama create saiga -f <path_to_your_.modelfile>
```

### 13. Скачиваем питон
### 14. Скачиваем pip 
### 15. Скачиваем все нужные либы, запустив комманду
```
pip install -r requirements.txt
```
### 16.  Если не работает команда python3, 
 то находим папку, где установлен питон (по умолчанию это: C:\Users\username\AppData\Local\Programs\Python) и, если используешь Windows, создай копию python.exe и переименуй её в python3.exe
### 17. working directory ВЕЗДЕ УКАЗЫВАЕМ КОРНЕВУЮ ПАПКУ ПРОЕКТА (ElectronHelper)
### 18. скачать и запустить Docker для тестов: https://www.docker.com/
### 19. Если сборка на личном компьютере С ДОСТУПОМ В ИНТЕРНЕТ, то создать папку .djl.ai в корне проекта. Иначе попросить у Егора папку .djl.ai и засунуть в корень проекта
### 20. Установить LibreOffice

## Мониторинг Prometheus и Grafana

Все пять Java-сервисов отдают метрики Micrometer по адресу `/actuator/prometheus` на **внутреннем порту 9090**. Prometheus опрашивает их каждые 15 секунд, а Grafana автоматически загружает источник данных и дашборд **RAG system**. Порт метрик сервисов на хосте не публикуется.

Перед сборкой должны быть подготовлены файлы моделей, перечисленные выше (`.djl.ai`, `multilingual`, `model.onnx_data`), а также используемые проектом образы `ollama` и `pgvector`. Java-образы теперь собираются из текущих исходников многоэтапными Dockerfile, поэтому изменения в метриках попадают в запускаемую систему:

```powershell
$env:GRAFANA_ADMIN_PASSWORD = "задайте-свой-пароль"
docker compose up --build -d
```

- Grafana: http://localhost:3000 (пользователь `admin`, пароль из `GRAFANA_ADMIN_PASSWORD`)
- Prometheus: http://localhost:9090/targets — здесь все пять целей должны стать `UP` после запуска сервисов.
- Дашборд: **RAG system** в папке **RAG system**.

На дашборде отображаются доступность сервисов, количество HTTP-запросов и ошибок 5xx, 95-й процентиль времени ответа, среднее время потоковой генерации ответа LLM, счётчики обработки документов и использование памяти JVM. Счётчик `rag_documents_processed_total` увеличивается после завершения обработки документа (в том числе когда документ сохранён как «странный»); пропущенные временные и пустые файлы не учитываются. `rag_documents_failed_total` отражает ошибки обработки, которые дошли до внешнего обработчика, и некорректные сообщения Kafka. Таймер LLM записывается после завершения или отмены потока ответа.

Конфигурация сбора находится в `monitoring/prometheus.yml`, настройки Grafana и дашборд — в `monitoring/grafana/`. Данные Prometheus и Grafana сохраняются в отдельных Docker-томах.

## Непрерывная интеграция (CI)

[GitHub Actions](.github/workflows/ci.yml) запускает workflow **CI** при `push` в `main`, открытии или обновлении pull request в `main`, а также вручную через **Actions → CI → Run workflow** после появления workflow в основной ветке.

- **Maven build and tests** использует Temurin JDK 22. Сначала выполняется `clean install` для `RagUtilLib`, затем `clean verify` для `DiscoveryServer`, `ApiGateway`, `MainMicroservice`, `ConvertMicroservice` и `BotMicroservice`. Maven запускает активные тесты; интеграционные сценарии, закомментированные в исходниках, в этот прогон не входят.
- **Compose and monitoring smoke test** проверяет Compose и конфигурацию Prometheus (`promtool check config`), собирает образ Eureka и запускает `discovery-server`, `prometheus`, `grafana`. Проверка проходит, когда Prometheus возвращает метрику памяти JVM от Eureka, а Grafana через API подтверждает наличие источника Prometheus и дашборда `RAG system`. При сбое workflow выводит логи, затем удаляет контейнеры и тома стенда. Остальные Java сервисы в этом стенде не запускаются, поэтому их цели Prometheus здесь могут быть `DOWN`.

## Полная проверка перед демонстрацией

1. Подготовьте файлы моделей из инструкции выше, доступный каталог с документацией и среду Docker с доступом к NVIDIA GPU для Ollama. В Compose проверьте источник монтирования `convert-microservice` и то, что настроенные каталоги внутри `/app/data` реально существуют. Для проверки ответа используйте учётную запись, которой разрешено чтение тестового документа.
2. Если локальных образов `pgvector:latest` и `ollama:latest` ещё нет, соберите их из корня проекта. Для второго Dockerfile нужны **оба** файла — `saiga.gguf` и `gigachat.gguf` — и соответствующие `*.modelfile`.

   ```powershell
   docker build -t pgvector:latest -f DockerFileForDb/Dockerfile .
   docker build -t ollama:latest -f OllamaDockerfile/Dockerfile .
   ```

3. Задайте свой `GRAFANA_ADMIN_PASSWORD`, затем выполните:

   ```powershell
   docker compose config --quiet
   docker compose up --build -d
   docker compose ps --all
   docker compose exec ollama ollama list
   ```

   Если запуск останавливается с сообщением `Ports are not available`, проверьте, свободны ли опубликованные порты, в том числе `9090` для Prometheus и `3000` для Grafana.

4. В веб-интерфейсе (`http://localhost:8080`) войдите в систему, добавьте или измените тестовый документ в наблюдаемом каталоге и дождитесь его индексации. Задайте вопрос, на который можно ответить по этому документу; проверьте найденный документ и содержание ответа.
5. Откройте `http://localhost:9090/targets`: в полном стенде все пять целей `spring-services` должны быть `UP`. В Grafana (`http://localhost:3000`) откройте `RAG system` после обработки документа и запроса к модели, чтобы на панелях появились данные от этих операций.
6. Для отчёта сохраните скриншоты `docker compose ps --all`, ответа с найденным документом, пяти целей Prometheus, дашборда Grafana и страницы запуска **CI** с результатами обоих заданий. Сделайте снимок `docker compose ps --all` после запуска модели и прикладных сервисов, чтобы он показывал итоговое состояние стенда.
