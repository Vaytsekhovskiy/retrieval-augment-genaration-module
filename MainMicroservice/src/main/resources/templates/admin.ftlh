<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta charset="UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <title>ElectronHelper</title>
    <link rel="icon" href="/images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="/images/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="/css/admin.css"/>
    <link rel="preconnect" href="https://fonts.googleapis.com"/>
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link
            href="https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap"
            rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
</head>

<body>
<div class="gradient-background">
    <div class="lines"></div>
    <div class="content-wrapper">
        <header class="header">

            <!-- Пользовательская панель -->
            <div class="user-panel" id="userPanel">
                <div class="user-avatar">
                    <img src="/images/user.png" alt="Аватар"/>
                </div>
                <span class="user-name">
                    ${user.username}
                </span>
                <svg class="dropdown-icon" width="12" height="8" viewBox="0 0 12 8" fill="none"
                     xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 1L6 6L11 1" stroke="#333" stroke-width="1" stroke-linecap="round"/>
                </svg>
                <!-- Выпадающее меню -->
                <div class="dropdown-menu">
                    <div class="menu-item">
                        <span>Размер шрифта</span>
                        <div class="font-size-switcher">
                            <button class="font-size-btn">S</button>
                            <button class="font-size-btn active">M</button>
                            <button class="font-size-btn">L</button>
                        </div>
                    </div>
                    <#if user.authorities?seq_contains(adminRole)>
                        <div class="menu-item">
                            <span>Доступ к нейросети</span>
                            <label class="toggle-switch">
                                <input type="checkbox" id="aiAccessToggle"
                                       onchange="toggleAiAccess(this)"
                                        ${aiAccessEnabled?then('checked', '')} />
                                <span class="slider" id="sliderForAiAccessToggle"></span>
                            </label>
                        </div>
                        <div class="menu-item">
                            <form action="/admin" method="get">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                <button type="submit" class="admin-mod">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                                         xmlns="http://www.w3.org/2000/svg">
                                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="#000000"
                                              stroke-width="2" stroke-linejoin="round"/>
                                    </svg>
                                    Панель администратора
                                </button>
                            </form>
                        </div>
                    </#if>

                    <div class="menu-item">
                        <form action="/logout" method="post">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                            <button type="submit" class="logout-btn">
                                <svg width="16" height="16" viewBox="0 0 16 16" fill="none"
                                     xmlns="http://www.w3.org/2000/svg">
                                    <path
                                            d="M6 14H3.333C2.98 14 2.667 13.86 2.393 13.58C2.119 13.3 2 12.98 2 12.62V3.38C2 3.02 2.119 2.7 2.393 2.42C2.667 2.14 2.98 2 3.333 2H6M11.333 11.333L14.666 8L11.333 4.666M14.666 8H6"
                                            stroke="#ff4d4f" stroke-width="2" stroke-linecap="round"/>
                                </svg>
                                Выйти
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </header>
        <div class="settings-container">
            <div class="settings-section">
                <h3>Обрабатываемые папки</h3>
                <div id="folders" class="folder-container">
                    <#if foldersInfo.rootFoldersPaths??>
                        <#list foldersInfo.rootFoldersPaths as rootPath>
                            <div class="state-card">
                                <div class="state-card-header">
                                    <div class="state-value">
                                        ${rootPath}
                                    </div>
                                    <button class="delete-btn"
                                            onclick="removeFolder('${rootPath?js_string}', false)"
                                            title="Удалить">
                                        ✕
                                    </button>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
                Введите абсолютный путь папки для её обработки
                <form id="folderForm" class="form-example">
                    <div class="form-example">
                        <div class="path-input">
                            <span class="path-prefix">${foldersInfo.mountPoint}</span>
                            <input type="text"
                                   id="pathInput"
                                   placeholder="path/to/your/folder"
                                   required>
                        </div>
                    </div>

                    <div class="form-example">
                        <button type="submit">Отправить</button>
                    </div>

                    <div id="errorBox" style="color: red; margin-top: 10px;"></div>
                </form>
                <h3>Необрабатываемые папки</h3>
                <div id="exclude-folders" class="folder-container">
                    <#if foldersInfo.exceptFolderPaths??>
                        <#list foldersInfo.exceptFolderPaths as exceptPath>
                            <div class="state-card">
                                <div class="state-card-header">
                                    <div class="state-value">
                                        ${exceptPath}
                                    </div>
                                    <button class="delete-btn"
                                            onclick="removeFolder('${exceptPath?js_string}', true)"
                                            title="Удалить">
                                        ✕
                                    </button>
                                </div>
                            </div>
                        </#list>
                    </#if>
                </div>
                Введите абсолютный путь папки для её исключения из обработки
                <form id="folderExceptForm" class="form-example">
                    <div class="form-example">
                        <div class="path-input">
                            <span class="path-prefix">${foldersInfo.mountPoint}</span>
                            <input type="text"
                                   id="exceptPathInput"
                                   placeholder="path/to/your/folder"
                                   required>
                        </div>
                    </div>

                    <div class="form-example">
                        <button type="submit">Отправить</button>
                    </div>

                    <div id="errorBoxExcept" style="color: red; margin-top: 10px;"></div>
                </form>
                <div id="folders"></div>
            </div>
            <div class="settings-section">
                <h3>Обрабатываемые файлы</h3>
                <div id="states" class="states-grid">
                    <div style='opacity:0.6'>Нет активных задач</div>
                </div>
                <h3>Обработанные файлы</h3>
                <button onclick="getFilesStats()" class="delete-btn">↺</button>
                <div id="filesStats" class="json-viewer"></div>

                <div id="documentsTableContainer" class="table-container"></div>
                <div class="pagination-bar">
                    <button id="prevPageBtn">← Назад</button>
                    <span id="pageIndicator"></span>
                    <button id="nextPageBtn">Вперёд →</button>
                </div>

                <button onclick="resetDocs()">Заново обработать документы</button>
            </div>
            <#if services??>
                <div class="settings-section">
                    <h3>Логи микросервисов</h3>
                    <div id="log">
                        <ul>
                            <li>info - логи уровня INFO и выше</li>
                            <li>warn - логи уровня WARN и выше</li>
                            <li>error - логи уровня ERROR и выше</li>
                        </ul>
                        Выберите уровень логов
                        <p><label>
                                <select id="levelSelect">
                                    <option value="info">info</option>
                                    <option value="warn">warn</option>
                                    <option value="error">error</option>
                                </select>
                            </label>
                        </p>
                        Выберите микросервис
                        <p><label>
                                <select id="microserviceSelect">
                                    <#if services??>
                                        <#list services as service>
                                            <option value="${service}">${service}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </label>
                        </p>
                        <button onclick="getLogs()">Получить логи</button>
                        <script>
                            function getLogs() {
                                const service = document.getElementById('microserviceSelect').value;
                                const logLevel = document.getElementById('levelSelect').value;
                                const url = `${gatewayUrl}/api/` + service + `/log/` + logLevel + `?x-user=${principal}`;

                                // создаём временную ссылку и "нажимаем" её
                                const a = document.createElement('a');
                                a.href = url;
                                a.download = service + `_` + logLevel + `.log`;
                                document.body.appendChild(a);
                                a.click();
                                document.body.removeChild(a);
                            }
                        </script>


                    </div>
                </div>
            </#if>
            <div class="settings-section wide">
                <h3>Текущая конфигурация RAG-системы</h3>
                <div id="botConfigContent">
                    <div class="config-loading">Загрузка...</div>
                </div>
                <div id="embeddingConfigContent">
                </div>
                <h3>Список загруженных LLM</h3>
                <div id="modelsList" class="json-viewer"></div>
                <div id="model-current"></div>
                <div id="models-options"></div>
                <div id="model-changed"></div>
                <div id="model-info" class="json-viewer"></div>
            </div>
        </div>
    </div>
</div>
</div>
<script>
    async function loadBotConfig() {
        const container = document.getElementById("botConfigContent");
        const embeddingContainer = document.getElementById("embeddingConfigContent");
        let data;
        try {
            const response = await fetch('${gatewayUrl}/api/bot/config?x-user=${principal}', {
                headers: {
                    "Accept": "application/json"
                }
            });
            if (!response.ok) throw new Error("Ошибка загрузки конфигурации");

            data = await response.json();
            <#noparse> // Всё, что внутри не парсится freemarker
            // document.getElementById('model-current').innerHTML = `На данный момент используется: ${escapeHtml(data.botName)}`
            container.innerHTML = `
    <div class="state-card">
        <div class="state-value">Кол-во последних сообщений, которая модель хранит в памяти</div>
        <input id="chatMemoryInput" type="number" min="0" step="2" class="config-input" value="${data.chatMemoryMessages}">
    </div>

    <div class="state-card">
        <div class="state-value">Системный промпт</div>
        <textarea id="systemPromptInput" class="json-viewer">${escapeHtml(data.systemPrompt)}</textarea>
    </div>
    <div class="state-card">
        <div class="state-value">RAG-промпт</div>
        <textarea id="ragPromptInput" class="json-viewer">${escapeHtml(data.ragPrompt)}</textarea>
    </div>

`;
            //private int topK;
            //private boolean ignoreAuthorities;
            //private double similarityThreshold;
            embeddingContainer.innerHTML = `
    <div class="state-card">
        <div class="state-value">Глубина поиска (top-K)</div>
        <input id="topK" type="number" min="0" step="1" class="config-input" value="${data.topK}">
    </div>

    <div class="state-card">
        <div class="state-value">Игнорировать права доступа к документам</div>

        <label class="switch">
            <input id="ignoreAuthorities" type="checkbox" ${data.ignoreAuthorities ? "checked" : ""}>
            <span class="slider"></span>
        </label>

        <div class="config-hint">
            Вкл — доступны все документы<br>
            Выкл — только разрешённые пользователю
        </div>
    </div>
    <div class="state-card">
        <div class="state-value">Минимальный порог схожести (similarityThreshold)</div>
        <input id="similarityThreshold"
           type="number"
           class="config-input"
           value="${data.similarityThreshold}"
           min="0"
           max="1"
           step="0.0001">
    </div>

<!-- Группа 1: Основные параметры -->
<div class="settings-group">
    <h4>Основные параметры модели</h4>

    <div class="state-card">
        <div class="state-value">Температура (temperature)</div>
        <input id="temperature"
           type="number"
           class="config-input"
           value="${data.temperature !== null && data.temperature !== undefined ? data.temperature : 0.8}"
           min="0"
           max="2"
           step="0.05">
        <div class="config-hint">
            Контролирует креативность ответов (0 = детерминировано, 2 = максимально креативно)
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Максимальное количество токенов (max_tokens)</div>
        <input id="maxTokens"
           type="number"
           class="config-input"
           value="${data.maxTokens !== null && data.maxTokens !== undefined ? data.maxTokens : 4096}"
           min="1"
           max="131072"
           step="1">
        <div class="config-hint">
            Максимальная длина ответа модели в токенах
        </div>
    </div>
</div>

<!-- Группа 2: Параметры сэмплирования -->
<div class="settings-group">
    <h4>Параметры сэмплирования</h4>

    <div class="state-card">
        <div class="state-value">Top-K</div>
        <input id="topKModel"
           type="number"
           class="config-input"
           value="${data.topKModel !== null && data.topKModel !== undefined ? data.topKModel : 40}"
           min="0"
           max="1000"
           step="1">
        <div class="config-hint">
            Ограничивает выборку K наиболее вероятными токенами
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Top-P</div>
        <input id="topP"
           type="number"
           class="config-input"
           value="${data.topP !== null && data.topP !== undefined ? data.topP : 0.9}"
           min="0"
           max="1"
           step="0.01">
        <div class="config-hint">
            Ядерная сэмплирование, ограничивает выборку токенами с кумулятивной вероятностью p
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Min-P</div>
        <input id="minP"
           type="number"
           class="config-input"
           value="${data.minP !== null && data.minP !== undefined ? data.minP : 0.0}"
           min="0"
           max="1"
           step="0.01">
        <div class="config-hint">
            Минимальная вероятность токена относительно наиболее вероятного
        </div>
    </div>
</div>

<!-- Группа 3: Пенальти и штрафы -->
<div class="settings-group">
    <h4>Пенальти и штрафы</h4>

    <div class="state-card">
        <div class="state-value">Frequency Penalty</div>
        <input id="frequencyPenalty"
           type="number"
           class="config-input"
           value="${data.frequencyPenalty !== null && data.frequencyPenalty !== undefined ? data.frequencyPenalty : 1.1}"
           min="-2"
           max="2"
           step="0.1">
        <div class="config-hint">
            Штраф за частоту появления токенов
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Presence Penalty</div>
        <input id="presencePenalty"
           type="number"
           class="config-input"
           value="${data.presencePenalty !== null && data.presencePenalty !== undefined ? data.presencePenalty : 0.0}"
           min="-2"
           max="2"
           step="0.1">
        <div class="config-hint">
            Штраф за присутствие токенов
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Repeat Penalty</div>
        <input id="repeatPenalty"
           type="number"
           class="config-input"
           value="${data.repeatPenalty !== null && data.repeatPenalty !== undefined ? data.repeatPenalty : 1.1}"
           min="-2"
           max="2"
           step="0.1">
        <div class="config-hint">
            Штраф за повторение токенов
        </div>
    </div>
</div>

<!-- Группа 4: Mirostat параметры -->
<div class="settings-group">
    <h4>Mirostat параметры</h4>

    <div class="state-card">
        <div class="state-value">Mirostat</div>
        <input id="mirostat"
           type="number"
           class="config-input"
           value="${data.mirostat !== null && data.mirostat !== undefined ? data.mirostat : 0}"
           min="0"
           max="2"
           step="1">
        <div class="config-hint">
            Алгоритм сэмплирования Mirostat (0=выкл, 1=v1, 2=v2)
        </div>
    </div>

<!-- Группа 5: Параметры повторения -->
<div class="settings-group">
    <h4>Параметры повторения</h4>

    <div class="state-card">
        <div class="state-value">Repeat Last N</div>
        <input id="repeatLastN"
           type="number"
           class="config-input"
           value="${data.repeatLastN !== null && data.repeatLastN !== undefined ? data.repeatLastN : 64}"
           min="-1"
           max="128"
           step="1">
        <div class="config-hint">
            Сколько последних токенов учитывать при штрафе за повторение (-1=все контекстное окно)
        </div>
    </div>

<!-- Группа 6: Продвинутые параметры -->
<div class="settings-group">
    <h4>Продвинутые параметры</h4>

    <div class="state-card">
        <div class="state-value">Use MMAP</div>
        <label class="switch">
            <input id="useMmap" type="checkbox" ${data.useMmap ? "checked" : ""}>
            <span class="slider"></span>
        </label>
        <div class="config-hint">
            Использовать memory-mapped файлы
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Use MLock</div>
        <label class="switch">
            <input id="useMlock" type="checkbox" ${data.useMlock ? "checked" : ""}>
            <span class="slider"></span>
        </label>
        <div class="config-hint">
            Блокировать модель в памяти
        </div>
    </div>
</div>

<!-- Группа 7: Ollama-specific параметры -->
<div class="settings-group">
    <h4>Параметры Ollama</h4>

    <div class="state-card">
        <div class="state-value">Format</div>
        <input id="format"
           type="text"
           class="config-input"
           value="${data.format || ''}"
           placeholder="json или JSON схема">
        <div class="config-hint">
            Формат вывода (например: json)
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Num Keep</div>
        <input id="numKeep"
           type="number"
           class="config-input"
           value="${data.numKeep !== null && data.numKeep !== undefined ? data.numKeep : 24}"
           min="-1"
           max="10240000"
           step="1">
        <div class="config-hint">
            Количество токенов для сохранения при обновлении контекста
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Num Ctx</div>
        <input id="numCtx"
           type="number"
           class="config-input"
           value="${data.numCtx !== null && data.numCtx !== undefined ? data.numCtx : 2048}"
           min="-1"
           max="10240000"
           step="1">
        <div class="config-hint">
            Размер контекстного окна
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Num Batch</div>
        <input id="numBatch"
           type="number"
           class="config-input"
           value="${data.numBatch !== null && data.numBatch !== undefined ? data.numBatch : 512}"
           min="256"
           max="8192"
           step="256">
        <div class="config-hint">
            Размер батча
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Num Thread</div>
        <input id="numThread"
           type="number"
           class="config-input"
           value="${data.numThread !== null && data.numThread !== undefined ? data.numThread : 2}"
           min="1"
           max="256"
           step="1">
        <div class="config-hint">
            Количество потоков
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Num GPU</div>
        <input id="numGpu"
           type="number"
           class="config-input"
           value="${data.numGpu !== null && data.numGpu !== undefined ? data.numGpu : 0}"
           min="0"
           max="256"
           step="1">
        <div class="config-hint">
            Количество слоев, которые модель будет использовать на GPU (Для каждой модели рабочее значение параметра разное).
        </div>
    </div>

    <div class="state-card">
        <div class="state-value">Keep Alive</div>
        <input id="keepAlive"
           type="text"
           class="config-input"
           value="${data.keepAlive || '5m'}"
           placeholder="пример: 5m, 30s, 10h">
        <div class="config-hint">
            Время хранения модели в памяти после запроса
        </div>
    </div>
</div>

<button class="save-button" onclick="saveBotConfig(false)">Сохранить все параметры</button>
<button class="save-button" onclick="resetBotConfig()">Восстановить настройки по умолчанию</button>
`;
        } catch (e) {
            container.innerHTML = `<div style="color:red;">Ошибка: ${e.message}</div>`;
        }
        </#noparse>
        return escapeHtml(data.botName);
    }

    function escapeHtml(text) {
        return String(text)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;");
    }

    async function saveBotConfig(isSavingModelName) {
        let modelName;
        if (isSavingModelName) {
            const selectedModel = document.querySelector('input[name="modelSelect"]:checked');
            if (!selectedModel) {
                alert('Выберите модель');
                return;
            }
            modelName = selectedModel.value;
        } else {
            modelName = null;
        }

        const payload = {
            botName: modelName,
            chatMemoryMessages: parseInt(document.getElementById("chatMemoryInput").value),
            systemPrompt: document.getElementById("systemPromptInput").value,
            ragPrompt: document.getElementById("ragPromptInput").value,
            topK: parseInt(document.getElementById("topK").value),
            ignoreAuthorities: document.getElementById("ignoreAuthorities").checked,
            similarityThreshold: parseFloat(document.getElementById("similarityThreshold").value),

            // Основные параметры
            temperature: parseFloat(document.getElementById("temperature").value),
            maxTokens: parseInt(document.getElementById("maxTokens").value),

            // Параметры сэмплирования
            topKModel: parseInt(document.getElementById("topKModel").value),
            topP: parseFloat(document.getElementById("topP").value),
            minP: parseFloat(document.getElementById("minP").value),

            // Пенальти и штрафы
            frequencyPenalty: parseFloat(document.getElementById("frequencyPenalty").value),
            presencePenalty: parseFloat(document.getElementById("presencePenalty").value),
            repeatPenalty: parseFloat(document.getElementById("repeatPenalty").value),

            // Mirostat параметры
            mirostat: parseInt(document.getElementById("mirostat").value),

            // Параметры повторения
            repeatLastN: parseInt(document.getElementById("repeatLastN").value),

            // Продвинутые параметры
            useMmap: document.getElementById("useMmap").checked,
            useMlock: document.getElementById("useMlock").checked,

            // Ollama-specific параметры
            format: document.getElementById("format").value,
            numKeep: parseInt(document.getElementById("numKeep").value),
            numCtx: parseInt(document.getElementById("numCtx").value),
            numBatch: parseInt(document.getElementById("numBatch").value),
            numThread: parseInt(document.getElementById("numThread").value),
            numGpu: parseInt(document.getElementById("numGpu").value),
            keepAlive: document.getElementById("keepAlive").value
        };

        // Проверки на валидность
        if (payload.temperature < 0 || payload.temperature > 2) {
            alert("Температура должна быть между 0 и 2");
            return;
        }
        if (payload.maxTokens < 1) {
            alert("Максимальное количество токенов должно быть больше 0");
            return;
        }
        if (payload.topKModel < 0) {
            alert("Top-K не может быть отрицательным");
            return;
        }
        if (payload.topP < 0 || payload.topP > 1) {
            alert("Top-P должен быть между 0 и 1");
            return;
        }
        if (payload.minP < 0 || payload.minP > 1) {
            alert("Min-P должен быть между 0 и 1");
            return;
        }
        if (payload.frequencyPenalty < -2 || payload.frequencyPenalty > 2) {
            alert("Frequency Penalty должен быть между -2 и 2");
            return;
        }
        if (payload.presencePenalty < -2 || payload.presencePenalty > 2) {
            alert("Presence Penalty должен быть между -2 и 2");
            return;
        }
        if (payload.mirostat < 0 || payload.mirostat > 2) {
            alert("Mirostat должен быть между 0 и 2");
            return;
        }
        if (payload.repeatLastN < -1) {
            alert("Repeat Last N не может быть меньше -1");
            return;
        }
        if (payload.repeatPenalty < -2 || payload.repeatPenalty > 2) {
            alert("Repeat Penalty должен быть между -2 и 2");
            return;
        }
        if (payload.numKeep < -1) {
            alert("Num Keep не может быть меньше -1");
            return;
        }
        if (payload.numCtx < -1) {
            alert("Num Ctx не может быть меньше -1");
            return;
        }
        if (payload.numBatch < 256) {
            alert("Num Batch должен быть не менее 256");
            return;
        }
        if (payload.numThread < 1) {
            alert("Num Thread должен быть не менее 1");
            return;
        }
        if (payload.numGpu < 0) {
            alert("Num GPU не может быть отрицательным");
            return;
        }

        try {
            const response = await fetch('${gatewayUrl}/api/bot/config?x-user=${principal}', {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Ошибка сохранения");
            }

            alert("✅ Конфигурация сохранена");

        } catch (e) {
            alert("❌ Ошибка: " + e.message);
        }
    }

    async function resetBotConfig() {
        try {
            const response = await fetch('${gatewayUrl}/api/bot/config/reset?x-user=${principal}', {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Ошибка сохранения");
            }

            alert("✅ Конфигурация сохранена. Настройки успешно сброшены");

        } catch (e) {
            alert("❌ Ошибка: " + e.message);
        }
    }

    async function removeFolder(path, isExceptFolder) {
        if (!confirm("Удалить папку?\n" + path)) return;

        try {
            const response = await fetch(`${gatewayUrl}/api/convert/folder?x-user=${principal}`, {
                method: "DELETE",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({path: path, isExceptFolder: isExceptFolder}),
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Ошибка удаления");
            }

        } catch (e) {
            alert("Ошибка при удалении:\n" + e.message);
        }
    }

    const evtSource = new EventSource("${gatewayUrl}/api/convert/states?x-user=${principal}");

    evtSource.onmessage = (event) => {
        const stateMap = JSON.parse(event.data);
        console.log("Обновление:", stateMap);
        renderStates(stateMap);
    };

    async function resetDocs() {
        if (!confirm("Заново обработать документы?\n" +
            "Все документы, включая их чанки, будут удалены из базы данных и заново обработаны")) return;
        try {
            const response = await fetch(`${gatewayUrl}/api/convert/reset?x-user=${principal}`, {
                method: "POST"
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Ошибка обработки");
            }

        } catch (e) {
            alert("Ошибка при обработки\n" + e.message);
        }
    }

    function renderStates(stateMap) {
        const container = document.getElementById("states");
        container.innerHTML = ""; // очищаем

        const entries = Object.entries(stateMap);

        if (entries.length === 0) {
            container.innerHTML = "<div style='opacity:0.6'>Нет активных задач</div>";
            return;
        }

        for (const [file, state] of entries) {
            const card = document.createElement("div");
            card.className = "state-card";

            const filename = document.createElement("div");
            filename.className = "state-filename";
            filename.textContent = file;

            const status = document.createElement("div");
            status.className = "state-status state-" + state;
            status.textContent = state;

            card.appendChild(filename);
            card.appendChild(status);

            container.appendChild(card);
        }
    }

    function selectModel() {
        const selectedModel = document.querySelector('input[name="modelSelect"]:checked');

        if (!selectedModel) {
            alert('Выберите модель');
            return;
        }
        const modelName = selectedModel.value;
        fetch('${gatewayUrl}/api/bot/ollama/show?x-user=${principal}', {
            headers: {
                'Content-Type': 'application/json',
            },
            method: 'POST',
            body: JSON.stringify({
                model: modelName
            })
        })
            .then(response => response.json())
            .then(result => {
                document.getElementById('model-info').innerHTML =  JSON.stringify(result, null, 2)
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ошибка при выборе модели');
            });
    }

    async function getFilesStats() {
        fetch('${gatewayUrl}/api/convert/stats?x-user=${principal}', {
            method: 'GET'
        })
            .then(response => response.json())
            .then(result => {
                document.getElementById('filesStats').innerHTML = '<pre>' + JSON.stringify(result, null, 2) + '</pre>'
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Ошибка при выборе модели');
            });
    }

    let currentPage = 0;
    const pageSize = 20;

    async function loadDocuments(page) {
        const res = await fetch(`${gatewayUrl}/api/convert/documents?x-user=${principal}&page=` + page + `&size=` + pageSize);
        const data = await res.json();

        renderDocumentsTable(data);
        <#noparse >
        document.getElementById("pageIndicator").innerText = `Страница ${page + 1}`;
        currentPage = page;
        </#noparse>

    }

    function renderDocumentsTable(data) {
        const container = document.getElementById("documentsTableContainer");

        if (!data || data.length === 0) {
            container.innerHTML = "<div class='state-card'>Документов нет</div>";
            return;
        }

        // Берём поля из первого объекта
        const columns = Object.keys(data[0]);

        let html = `<table class="documents-table"><thead><tr>`;

        for (const col of columns) {
            html += `<th>` + humanize(col) + `</th>`;
        }

        html += `</tr></thead><tbody>`;
        <#noparse >
        for (const row of data) {
            html += `<tr>`;
            for (const col of columns) {
                html += `<td>${escapeHtml(String(row[col]))}</td>`;
            }
            html += `</tr>`;
        }
        </#noparse >
        html += `</tbody></table>`;

        container.innerHTML = html;
    }

    function humanize(str) {
        return str
            .replace(/([A-Z])/g, " $1")
            .replace(/^./, s => s.toUpperCase());
    }

    fetch('${gatewayUrl}/api/bot/ollama/tags?x-user=${principal}', {
        headers: {
            'Content-Type': 'application/json',
        },
        method: 'POST'
    })
        .then(response => response.json())
        .then(async result => {
            const modelNames = result.models.map(model => model.name);

            const currentModel = await loadBotConfig();

            document.getElementById('modelsList').innerHTML =
                '<pre>' + JSON.stringify(result, null, 2) + '</pre>';

            <#noparse >
            const container = document.getElementById('models-options');

            container.innerHTML =
                `<div id="models-metainfo">Всего моделей: ${result.models.length}</div>` +
                '<div id="modelsContainer">' +
                modelNames.map(name => {

                    const isCurrent = name === currentModel;
                    return `
            <div class="model-option">
                <input type="radio"
                       onclick="selectModel()"
                       name="modelSelect"
                       value="${name}"
                       id="model-${name}"
                       ${isCurrent ? "checked" : ""}>

                <label for="model-${name}">
                    ${name}
                    ${isCurrent ? '<span class="model-active"> — используется сейчас</span>' : ''}
                </label>
            </div>
        `;
                }).join('') +
                '</div>' +
                '<button onclick="saveBotConfig(true)">Использовать данную модель</button>';
            </#noparse>

        })
        .catch(error => {
            console.error('Error:', error);
        });
    document.getElementById("folderForm").addEventListener("submit", async (e) => {
        e.preventDefault();

        const path = document.getElementById("pathInput").value;
        const errorBox = document.getElementById("errorBox");
        errorBox.textContent = "";

        try {
            const response = await fetch(
                `${gatewayUrl}/api/convert/folder?x-user=${principal}&path=` + encodeURIComponent(path),
                {method: "POST"}
            );

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Ошибка запроса");
            }

            alert("Папка успешно добавлена");

        } catch (err) {
            errorBox.textContent = err.message;
        }
    });
    document.getElementById("folderExceptForm").addEventListener("submit", async (e) => {
        e.preventDefault();

        const path = document.getElementById("exceptPathInput").value;
        const errorBox = document.getElementById("errorBoxExcept");
        errorBox.textContent = "";

        try {
            const response = await fetch(
                `${gatewayUrl}/api/convert/folder-except?x-user=${principal}&path=` + encodeURIComponent(path),
                {method: "POST"}
            );

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Ошибка запроса");
            }

            alert("Папка успешно исключена из обработки");

        } catch (err) {
            errorBox.textContent = err.message;
        }
    });
    getFilesStats()
    loadDocuments(0);
    // Кнопки переключения страниц
    document.getElementById("prevPageBtn").onclick = () => {
        if (currentPage > 0) {
            loadDocuments(currentPage - 1);
        }
    };

    document.getElementById("nextPageBtn").onclick = () => {
        loadDocuments(currentPage + 1);
    };
    // Кнопки переключения страниц (закончились)
</script>
</body>
</html>