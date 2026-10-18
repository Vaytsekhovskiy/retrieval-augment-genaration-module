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