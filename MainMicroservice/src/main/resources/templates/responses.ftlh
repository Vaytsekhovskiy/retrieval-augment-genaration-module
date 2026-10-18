<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>ElectronHelper</title>
    <link rel="icon" href="/images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="/images/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="/css/responses.css" />
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
            href="https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap"
            rel="stylesheet" />
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
</head>

<body>
<div class="gradient-background">
    <div class="lines"></div>
    <div class="content-wrapper">
        <header class="header">
            <!-- Строка поиска -->



            <!-- Пользовательская панель -->
            <div class="user-panel" id="userPanel">
                <div class="user-avatar">
                    <img src="/images/user.png" alt="Аватар" />
                </div>
                <span class="user-name">
            ${user.username}
          </span>
                <svg class="dropdown-icon" width="12" height="8" viewBox="0 0 12 8" fill="none"
                     xmlns="http://www.w3.org/2000/svg">
                    <path d="M1 1L6 6L11 1" stroke="#333" stroke-width="1" stroke-linecap="round" />
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
                            <form action="/admin" method="get">
                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                <button type="submit" class="admin-mod">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" stroke="#000000" stroke-width="2" stroke-linejoin="round" />
                                    </svg>
                                    Панель администратора
                                </button>
                            </form>
                        </div>
                    </#if>

                    <div class="menu-item">
                        <form action="/logout" method="post">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                            <button type="submit" class="logout-btn">
                                <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path
                                            d="M6 14H3.333C2.98 14 2.667 13.86 2.393 13.58C2.119 13.3 2 12.98 2 12.62V3.38C2 3.02 2.119 2.7 2.393 2.42C2.667 2.14 2.98 2 3.333 2H6M11.333 11.333L14.666 8L11.333 4.666M14.666 8H6"
                                            stroke="#ff4d4f" stroke-width="2" stroke-linecap="round" />
                                </svg>
                                Выйти
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </header>

        <div class="dialogue-container">
            <#if dialogue??>
                <#list dialogue as message>
                    <div class="black-box-area">
                        <#if message.messageType?string == "USER">
                            <!-- Ответ от пользователя -->
                            <div class="black-box-container right">
                                <div class="black-box right" id="black-box-2">
                                    ${message.textContent}
                                </div>
                            </div>
                        <#else>
                            <div class="black-box-container left">
                                <div class="black-box left" id="black-box">
                                    <div class="markdown-content">${message.textContent}</div>
                                </div>
                            </div>
                        </#if>
                    </div>
                </#list>

                <#if request??>
                    <div id="currentRequestSection">
                        <div class="black-box-area">
                            <div class="black-box-container right">
                                <div class="black-box right" id="black-box-2">
                                    ${request}
                                </div>
                            </div>
                        </div>
                    </div>
                </#if>
            </#if>

        </div>

        <#if !appropriateFiles?? && !(dialogue?? && dialogue?has_content)>
            <div class="search-container">
                <form action="/" method="get" class="search-wrapper-main">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <div class="search-input-container">
                        <div class="search-input">
                            <svg class="search-icon" viewBox="0 0 24 24" fill="none">
                                <path
                                        d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z"
                                        stroke="#5F6368" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                <path d="M21 21L16.65 16.65" stroke="#5F6368" stroke-width="2" stroke-linecap="round"
                                      stroke-linejoin="round" />
                            </svg>
                            <input id="searchInput" type="text" placeholder="Ваш запрос..." name="request" />
                        </div>
                    </div>
                    <button type="submit" class="search-button"></button>
                    <#if dialogue?has_content>
                        <button type="button" class="clear-button" id="buttonClear"></button>
                    </#if>
                </form>
                <div id="mainRequestLengthError" style="display: none; color: #ff4d4f; margin-top: 8px; text-align: center;">
                    Пожалуйста, введите запрос короче 300 символов
                </div>

                <#if !dialogue?has_content>
                    <div class="popular-questions">
                        <div class="question-card" onclick="setSearchText('Что такое метрология?')">
                            <img src="/images/question-card.svg" alt="?" />
                            <p class="dynamic-font">Что такое метрология?</p>
                        </div>
                        <div class="question-card" onclick="setSearchText('Для чего нужны стандарты ISO?')">
                            <img src="/images/question-card.svg" alt="?" />
                            <p class="dynamic-font">Для чего нужны стандарты ISO?</p>
                        </div>
                        <div class="question-card" onclick="setSearchText('Что такое КД и для чего он нужен?')">
                            <img src="/images/question-card.svg" alt="?" />
                            <p class="dynamic-font">Что такое КД и для чего он нужен?</p>
                        </div>
                    </div>
                </#if>
            </div>
        </#if>





        <#if (appropriateFiles?? && appropriateFiles?is_boolean)>
            <div class="response-block" id="responseBlock" style="display: flex; align-items: center; gap: 8px;">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 8V12" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 16H12.01" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <p class="dynamic-font">Сервер по поиску файлов недоступен, попробуйте позже</p>
            </div>
        </#if>
        <#if appropriateFiles?? && !appropriateFiles?is_boolean>
        <#if user.authorities?seq_contains(adminRole)>
        <script>

            marked.setOptions({
                breaks: true,
                gfm: true,
            });

            // дедубликация при обновлении страницы
            (function() {
                const currentRequest = '${request!}';
                if (!currentRequest) return;

                const lastProcessedRequest = sessionStorage.getItem('lastProcessedRequest');

                if (lastProcessedRequest === currentRequest) {
                    window.skipGeneration = true;
                } else {
                    sessionStorage.setItem('lastProcessedRequest', currentRequest);
                    window.skipGeneration = false;
                }
            })();

            document.addEventListener('DOMContentLoaded', function() {
                if (!window.skipGeneration) {
                    initEventSource();
                } else {
                    // Skip generation - hide duplicate rendering
                    console.log('Skipping generation - request already processed');

                    const currentRequestSection = document.getElementById('currentRequestSection');
                    if (currentRequestSection) {
                        currentRequestSection.style.display = 'none';
                    }
                    const responseBlock = document.getElementById('responseBlock');
                    if (responseBlock) {
                        responseBlock.style.display = 'none';
                    }
                    document.getElementById('loadingAnswer').style.display = 'none';
                    const stopButton = document.getElementById('buttonStop');
                    const startButton = document.getElementById('buttonStart');
                    if (stopButton && startButton) {
                        stopButton.style.display = 'none';
                        startButton.style.display = 'block';
                    }
                }
            });

            function initEventSource() {

                let requestInput = document.getElementById("searchInput") ||
                    document.getElementById("headerSearchInput") ||
                    "";
                var sourceUrl = '${gatewayUrl}/api/bot/response/get'; // Базовый URL
                var source = new EventSource(sourceUrl + '?request=' + encodeURIComponent(requestInput.value.trim()) + '&x-user=${principal}'); // Добавляем параметр запроса
                let accumulatedText = '';
                let timerLenText;
                let generationFinished = false;
                const stopButton = document.getElementById('buttonStop');
                const startButton = document.getElementById('buttonStart');
                const messagesDiv = document.getElementById('responseText');
                let currentWrapperText = document.createElement('div')
                currentWrapperText.className = 'elResponseText';

                if (stopButton && startButton) {
                    stopButton.style.display = 'block';
                    startButton.style.display = 'none';
                }

                messagesDiv.appendChild(currentWrapperText);


                const connectionTimeout = setTimeout(() => {
                    if (source.readyState !== EventSource.OPEN) {
                        // Если соединение не открыто через 4 секунды, показываем сообщение
                        const queueMessage = document.getElementById('queueMessage');
                        queueMessage.style.display = 'flex';
                    }
                }, 4000);
                source.onopen = function(event) {

                    timerLenText = setTimeout(() => {
                        source.close();
                        stopButton.style.display = 'none';
                        startButton.style.display = 'block';
                        document.getElementById('reviewForm').style.display = 'block';
                    }, 120000);

                    clearTimeout(connectionTimeout);
                    stopButton.classList.add('active');
                    // Убираем сообщение загрузки, когда соединение установлено
                    document.getElementById('loadingAnswer').style.display = 'none';
                    document.getElementById('loadedAnswer').style.display = 'block';
                    startButton.style.display = 'none';
                    stopButton.style.display = 'block';
                };
                source.onerror = (e) => {
                    clearTimeout(connectionTimeout);
                    document.getElementById('loadingAnswer').style.display = 'none';
                    document.getElementById('eventSourceMessageAnswer').style.display = 'flex';
                    console.error("Ошибка SSE:", e);
                    source.close();
                };
                source.onmessage = (event) => {
                    if (event.data.toString().includes('data:image/png;base64,')) {
                        // Удаляем обрамляющие кавычки
                        const imgData = event.data.toString().replace(/^"|"$/g, '');
                        const imgElement = document.createElement('img');
                        imgElement.onload = function() {
                            const container = document.getElementById("responseText");
                            // Создаем элемент-обертку для картинки
                            const imgWrapper = document.createElement('div');
                            imgWrapper.style.display = 'flex';
                            imgWrapper.style.justifyContent = 'center';
                            imgWrapper.style.margin = '20px 0';
                            // Добавляем картинку в обертку
                            imgWrapper.appendChild(imgElement);
                            // Добавляем обертку в контейнер
                            container.appendChild(imgWrapper);
                            // Стилизация изображения
                            imgElement.style.maxWidth = "100%";
                            imgElement.style.height = "auto";
                            imgElement.style.borderRadius = "16px";
                            imgElement.style.display = 'block';
                            currentWrapperText = document.createElement('div')
                            currentWrapperText.className = 'elResponseText';
                            messagesDiv.appendChild(currentWrapperText);
                            accumulatedText = '';
                        };
                        imgElement.onerror = function() {
                            console.error('Ошибка загрузки изображения');
                        };
                        imgElement.src = imgData;
                        return;
                    }
                    // Проверяем, равно ли полученное сообщение "END"
                    if (event.data === "END") {
                        generationFinished = true;
                        clearTimeout(timerLenText);
                        source.close();
                        stopButton.style.display = 'none';
                        startButton.style.display = 'block';
                        document.getElementById('reviewForm').style.display = 'block';
                        invalidateDialogueCache();
                        return; // Прерываем выполнение функции
                    }
                    const cleanData = event.data.replace(/^'|'$/g, '');
                    accumulatedText += cleanData;
                    currentWrapperText.innerHTML = marked.parse(accumulatedText); // Перерендер всего текста
                };
                // Обработка кнопки для остановки генерации
                stopButton.addEventListener('click', function(event) {
                    if (source) {
                        clearTimeout(timerLenText);
                        source.close(); // Закрываем EventSource
                        source = null;
                        stopButton.style.display = 'none';
                        startButton.style.display = 'block';
                        document.getElementById('reviewForm').style.display = 'block';
                        if (!generationFinished) {
                            invalidateDialogueCache();
                        }
                    }
                });
                document.getElementById("headerSearchInput").value = "";
            }

        </script>
        </#if>

        <#if !(user.authorities?seq_contains(adminRole))>
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                const stopButton = document.getElementById('buttonStop');
                const startButton = document.getElementById('buttonStart');

                document.getElementById('loadingAnswer').style.display = 'none';
                document.getElementById('adminMessageAnswer').style.display = 'flex';
                startButton.style.display = 'block';
                stopButton.style.display = 'none';
                document.getElementById('reviewForm').style.display = 'block';
            })
        </script>
        </#if>

        <div class="response-block" id="responseBlock">
            <!-- Часть 1: Текст ответа -->
            <div class="response-text-section" id="loadedAnswer">
                <div class="header-text-response-block">Ответ нейросети:</div>
                <div id="responseText"></div>
                <div class="ai-warning">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FFA500" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 8V12" stroke="#FFA500" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        <path d="M12 16H12.01" stroke="#FFA500" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p class="dynamic-font">Этот ответ сгенерирован нейросетью. Ответ может содержать неточности. Пожалуйста, перепроверяйте предоставленную информацию.</p>
                </div>

            </div>

            <div class="response-text-section" id="adminMessageAnswer">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 8V12" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 16H12.01" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <p class="dynamic-font">Доступ к ответу нейросети временно отключен администратором</p>
            </div>

            <!-- Часть 1: Текст ответа -->
            <div class="response-text-section" id="loadingAnswer">
                <div class="header-text-response-block">
                    Ответ нейросети:
                    <div class="loading-ring"></div>
                </div>
                <div class="loading-line"></div>
                <div id=queueMessage>Вы в очереди, пожалуйста подождите...</div>
            </div>

            <div class="response-text-section" id="eventSourceMessageAnswer">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 8V12" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    <path d="M12 16H12.01" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <p class="dynamic-font">Ошибка при получении ответа от нейросети</p>
            </div>
            <#--  <div class="button-container">
              <button id="stopGenerationBtn" class="submit-button">Остановить</button>
            </div>  -->
            <!-- Часть 2: Карточки с документами -->
            <#assign hasRelatedDocs = false>
            <#list appropriateFiles as doc>
                <#if doc.relatedDocs?? && doc.relatedDocs?size gt 0>
                    <#assign hasRelatedDocs = true>
                    <#break>
                </#if>
            </#list>
            <div class="response-documents-section">
                <div class="header-text-response-block">Основан на документах:</div>
                <#if hasRelatedDocs>
                    <div class="documents-header">
                        <div class="main-docs-title">Основные документы</div>
                        <div class="related-docs-title">Приложения</div>
                    </div>
                </#if>
                <div class="document-cards">
                    <#list appropriateFiles as doc>
                        <#if doc?? && doc.id?? && doc.fileName??>
                            <#assign fileName = doc.fileName>
                            <#if doc.page??>
                                <#assign fileName = "[стр. " + doc.page + "] " + fileName>
                            </#if>
                            <div class="document-container <#if !hasRelatedDocs>full-width</#if>">
                                <div class="main-document <#if !hasRelatedDocs>full-width</#if>">
                                    <#if doc.youTrackId??>
                                    <a href="${youTrackUrl}/articles/${doc.youTrackId}" class="document-card" target="_blank">
                                        <#else>

                                        <a href="${gatewayUrl}/api/convert/download/${doc.id}?x-user=${principal}"
                                           class="download-btn"
                                           title="Скачать файл">
                                            <svg width="23" height="23" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="#333" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <path d="M7 10L12 15L17 10" stroke="#333" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <path d="M12 15V3" stroke="#333" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </a>

                                        <a href="${gatewayUrl}/api/convert/open/${doc.id}?x-user=${principal}#page=${doc.page}" class="document-card" target="_blank">
                                            </#if>
                                            <img src="/images/link-file.svg" alt="Document" />
                                            <div class="document-info-wrapper">
                                                <div class="document-title">${fileName}</div>
                                                <#if doc.relevantContent??>
                                                    <div class="document-content-preview">${doc.relevantContent}</div>
                                                </#if>
                                            </div>
                                            <div class="tooltip">
                                                ${fileName}
                                                <#if doc.relevantContent??>
                                                    <div class="tooltip-content">${doc.relevantContent}</div>
                                                </#if>
                                            </div>
                                        </a>
                                </div>
                                <#if hasRelatedDocs && doc.relatedDocs?? && doc.relatedDocs?size gt 0>
                                    <div class="document-links">
                                        <#list doc.relatedDocs as relatedDoc>
                                        <#if relatedDoc.youTrackId??>
                                        <a href="${youTrackUrl}/articles/${relatedDoc.youTrackId}" class="document-card" target="_blank">
                                            <#else>
                                            <a href="${gatewayUrl}/api/convert/open/${relatedDoc.id}?x-user=${principal}" class="link-card" target="_blank">
                                                </#if>
                                                <div class="document-info">
                                                    ${relatedDoc.fileName}
                                                </div>
                                                <div class="tooltip">
                                                    ${relatedDoc.fileName}
                                                </div>
                                            </a>
                                            </#list>
                                    </div>
                                </#if>
                            </div>
                        </#if>
                    </#list>
                </div>
                </#if>
            </div>
            <!-- Часть 3: Блок оценки ответа -->
            <div id='reviewForm' class="response-feedback-section">
                <div class="input-1-10" id="invitation1">Пожалуйста, оцените ответ по шкале от 1 до 10, где 1 - "неудовлетворительно" и 10 - "отлично":</div>



                <div class="feedback-success" id="feedbackSentSuccessfully" style="display: none;">
                    <div class="ok-icon" id="ok"></div>
                    <div class="success-message dynamic-font">Ваш комментарий успешно отправлен. Мы ценим ваш опыт и мнение! Спасибо за ваш вклад!</div>
                </div>
                <div class="rating-wrapper" id="ratingWrapper">
                    <div class="rating">
                        <input type="radio" id="rate1" name="rating" value="1"><label for="rate1" class="dynamic-font">1</label>
                        <input type="radio" id="rate2" name="rating" value="2"><label for="rate2" class="dynamic-font">2</label>
                        <input type="radio" id="rate3" name="rating" value="3"><label for="rate3" class="dynamic-font">3</label>
                        <input type="radio" id="rate4" name="rating" value="4"><label for="rate4" class="dynamic-font">4</label>
                        <input type="radio" id="rate5" name="rating" value="5"><label for="rate5" class="dynamic-font">5</label>
                        <input type="radio" id="rate6" name="rating" value="6"><label for="rate6" class="dynamic-font">6</label>
                        <input type="radio" id="rate7" name="rating" value="7"><label for="rate7" class="dynamic-font">7</label>
                        <input type="radio" id="rate8" name="rating" value="8"><label for="rate8" class="dynamic-font">8</label>
                        <input type="radio" id="rate9" name="rating" value="9"><label for="rate9" class="dynamic-font">9</label>
                        <input type="radio" id="rate10" name="rating" value="10"><label for="rate10" class="dynamic-font">10</label>
                    </div>
                </div>
                <div class="input-review-text" id="invitation2" >Если у вас есть что-то конкретное, что вы хотели бы отметить или изменить в ответе, пожалуйста, оставьте свои комментарии ниже:</div>
                <div class="textarea-wrapper" id="textareaWrapper">
                    <textarea id="reviewComment" class="dynamic-font" placeholder="Ваш комментарий"></textarea>
                </div>
                <div class="button-container">
                    <button id="feedbackSubmitBtn" class="submit-button">Отправить</button>
                </div>
            </div>
        </div>

        <div class="bottom-search-container">

            <div class="header-search">
                <form action="/" method="get" class="search-wrapper-header">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <div class="search-input-container">
                        <div class="search-input">
                            <svg class="search-icon" viewBox="0 0 24 24" fill="none">
                                <path
                                        d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z"
                                        stroke="#5F6368" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                                <path d="M21 21L16.65 16.65" stroke="#5F6368" stroke-width="2" stroke-linecap="round"
                                      stroke-linejoin="round" />
                            </svg>
                            <input id="headerSearchInput" type="text" placeholder="Ваш запрос..." name="request" value="${(request)!}" />
                        </div>
                    </div>
                    <button type="submit" class="search-button" id="buttonStart"></button>
                    <button type="button" class="search-button" id="buttonStop"></button>
                    <button type="button" class="clear-button" id="buttonClear"></button>

                </form>
                <div id="headerRequestLengthError" class="error-tooltip">
                    Пожалуйста, введите запрос короче 300 символов
                </div>
            </div>

        </div>

        <script>
            document.getElementById('buttonClear').addEventListener('click', function() {
                sessionStorage.removeItem('lastProcessedRequest');
                const xUser  = '${principal}';
                fetch('/clear-dialogue?x-user=' + encodeURIComponent(xUser), {
                    method: 'POST',
                    headers: {
                        'X-CSRF-TOKEN': '${_csrf.token}'
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            // кэш уже вычищается через ApiHandler.clearDialogue()
                            document.querySelectorAll('.black-box-container, .black-box-area').forEach(el => el.remove());
                            window.location.href = '/';
                        } else {
                            alert('Ошибка при очистке диалога');
                        }
                    })
                    .catch(error => {
                        console.error('Ошибка:', error);
                        alert('Ошибка при очистке диалога');
                    });
            });
        </script>

        <script>
            function invalidateDialogueCache() {
                fetch('/dialogue/cache/invalidate', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-CSRF-TOKEN': '${_csrf.token}'
                    },
                    body: 'x-user=' + encodeURIComponent('${principal}')
                }).catch(err => console.error('Cache invalidation failed', err));
            }
        </script>

        <#if request??>
            <script>
                document.getElementById('feedbackSubmitBtn').addEventListener('click', function() {
                    // Собираем данные формы
                    const rating = document.querySelector('input[name="rating"]:checked')?.value;
                    const comment = document.getElementById('reviewComment').value;
                    const submitBtn = document.getElementById('feedbackSubmitBtn');
                    // Проверяем, что оценка выбрана
                    if (!rating) {
                        ratingWrapper.style.animation = 'none';
                        // Принудительно вызываем перерисовку
                        void ratingWrapper.offsetWidth;
                        // Сбрасываем стили
                        ratingWrapper.style.transform = '';
                        ratingWrapper.style.boxShadow = '';
                        // Запускаем анимацию заново
                        ratingWrapper.style.animation = 'smoothBlink 0.5s ease-in-out 2';
                        return;
                    }

                    submitBtn.disabled = true;
                    submitBtn.classList.add('disabled');
                    // Создаем объект с данными
                    const formData = {
                        aiAnswer: decodeURIComponent(document.getElementById("responseText")?.innerText || ''),
                        eval: rating,
                        login: '${user.username}',
                        request: '${request}',
                        userReview: comment
                    };
                    // Отправляем данные
                    fetch("${gatewayUrl}/api/search/review?x-user=${principal}", {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(formData)
                    })
                        .then(response => {
                            if (response.ok) {
                                // Успешная отправка - скрываем форму и показываем сообщение об успехе
                                document.getElementById('ratingWrapper').style.display = 'none';
                                document.getElementById('textareaWrapper').style.display = 'none';
                                document.getElementById('invitation1').style.display = 'none';
                                document.getElementById('invitation2').style.display = 'none';
                                document.getElementById('feedbackSentSuccessfully').style.display = 'flex';
                                document.getElementById('ok').style.display = 'block';
                                submitBtn.textContent = '✓ Отправлено';

                            } else {
                                document.querySelector('.response-feedback-section').style.display = 'none';
                                throw new Error('Ошибка при отправке отзыва');
                            }
                        })
                        .catch(error => {
                            document.querySelector('.response-feedback-section').style.display = 'none';
                            console.error('Error:', error);
                        });
                });

            </script>
        </#if>
        <script>

            function updateAuthorities() {
                if (confirm('Обновить права доступа для всех документов? Это может занять некоторое время.')) {
                    fetch('/admin/update-authorities?x-user=${principal}', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded',
                            'X-CSRF-TOKEN': '${_csrf.token}',
                            'Authorization': 'Bearer ${principal}'
                        }
                    })
                        .then(response => {
                            alert(`Права доступа успешно обновлены`);
                        })
                        .catch(err => {
                            console.error('Ошибка при обновлении прав:', err);
                            alert('Ошибка при обновлении прав доступа');
                        });
                }
            }

            document.querySelectorAll('form[action="/"]').forEach(form => {
                form.onsubmit = function(e) {
                    const input = this.querySelector('input[name="request"]');

                    // Проверка на пустую строку
                    if (input && !input.value.trim()) {
                        e.preventDefault();
                        // Визуальная обратная связь
                        input.style.border = '2px solid #ff4d4f';
                        input.style.borderRadius = '4px';
                        input.focus();

                        // Убираем подсветку через 1.5 секунды
                        setTimeout(() => {
                            input.style.border = '';
                        }, 1500);

                        return false;
                    }

                    if (input && input.value.length > 300) {
                        e.preventDefault();
                        const errorId = this.closest('.header-search') ? 'headerRequestLengthError' : 'mainRequestLengthError';
                        document.getElementById(errorId).style.display = 'block';
                        input.focus();
                        return false;
                    }
                };
            });


            document.querySelectorAll('input[name="request"]').forEach(input => {
                input.addEventListener('keydown', function(event) {
                    if (event.key === 'Enter') {
                        event.preventDefault(); // Блокируем стандартное поведение Enter
                        return false;
                    }
                });
            });


            // Общая проверка при вводе для всех полей поиска
            document.querySelectorAll('input[name="request"]').forEach(input => {
                input.oninput = function() {
                    // Находим соответствующее сообщение об ошибке
                    const errorId = this.closest('.header-search') ? 'headerRequestLengthError' : 'mainRequestLengthError';
                    const error = document.getElementById(errorId);
                    if (error) {
                        error.style.display = this.value.length > 300 ? 'block' : 'none';
                    }
                };
            });
        </script>
    </div>
</div>
<script src="/js/responses.js"></script>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const dialogueContainer = document.querySelector('.dialogue-container');
        const headerSearch = document.querySelector('.header-search');
        const header = document.querySelector('header.header');
        const bottomContainer = document.querySelector('.bottom-search-container');
        const clearButton = document.querySelector('.clear-button');

        setTimeout(() => {
            if (dialogueContainer) {
                dialogueContainer.scrollIntoView({
                    behavior: 'smooth',
                    block: 'end',
                    inline: 'nearest'
                });
                window.scrollBy(0, 100);
            }
        }, 100); // Небольшая задержка для гарантии рендера

        if (dialogueContainer && headerSearch && header && bottomContainer && clearButton) {
            const hasMessages = dialogueContainer.querySelector('div');

            if (hasMessages) {
                // Есть сообщения — ПОКАЗЫВАЕМ bottom-search-container
                bottomContainer.style.display = 'flex';
                clearButton.style.display = 'block';
                document.querySelector('.content-wrapper').style.paddingBottom = '100px';

                // НЕ переносим headerSearch - он остается в header
                // headerSearch остается на своем месте в header

            } else {
                // Нет сообщений — СКРЫВАЕМ bottom-search-container
                bottomContainer.style.display = 'none';
                clearButton.style.display = 'none';
                document.querySelector('.content-wrapper').style.paddingBottom = '0';

                // headerSearch остается в header (ничего не делаем)
            }
        }
    });
</script>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const markdownElements = document.querySelectorAll('.markdown-content');
        markdownElements.forEach(element => {
            const originalText = element.textContent;
            element.innerHTML = marked.parse(originalText);
        });
    });
</script>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Обработка переключателя размера шрифта
        const fontSizeButtons = document.querySelectorAll('.font-size-btn');

        // Функция для применения размера шрифта
        function applyFontSize(size) {
            // Определяем размер
            let fontSize;
            switch(size) {
                case 'S': fontSize = '14px'; break;
                case 'M': fontSize = '16px'; break;
                case 'L': fontSize = '18px'; break;
                default: fontSize = '16px';
            }

            // Применяем размер ко всем сообщениям в диалоге
            const blackBoxElements = document.querySelectorAll('.black-box');
            const markdownElements = document.querySelectorAll('.markdown-content');
            const dynamicFontElements = document.querySelectorAll('.dynamic-font');
            const responseTextElements = document.querySelectorAll('#responseText, .elResponseText');

            // Применяем ко всем элементам
            blackBoxElements.forEach(el => el.style.fontSize = fontSize);
            markdownElements.forEach(el => el.style.fontSize = fontSize);
            dynamicFontElements.forEach(el => el.style.fontSize = fontSize);

            // Для responseText и его дочерних элементов
            responseTextElements.forEach(el => {
                el.style.fontSize = fontSize;
                // Также применяем ко всем вложенным элементам
                el.querySelectorAll('*').forEach(child => {
                    child.style.fontSize = fontSize;
                });
            });

        }

        fontSizeButtons.forEach(button => {
            button.addEventListener('click', function() {
                // Убираем класс active у всех кнопок
                fontSizeButtons.forEach(btn => btn.classList.remove('active'));

                // Добавляем класс active нажатой кнопке
                this.classList.add('active');

                // Определяем размер
                const size = this.textContent.trim();

                // Применяем размер шрифта
                applyFontSize(size);

                // Сохраняем в localStorage
                localStorage.setItem('preferredFontSize', size);
            });
        });

        // Восстанавливаем сохраненный размер
        const savedSize = localStorage.getItem('preferredFontSize');
        if (savedSize) {
            const buttonToActivate = Array.from(fontSizeButtons).find(
                btn => btn.textContent.trim() === savedSize
            );
            if (buttonToActivate) {
                // Убираем active у всех кнопок
                fontSizeButtons.forEach(btn => btn.classList.remove('active'));

                // Добавляем active к сохраненной кнопке
                buttonToActivate.classList.add('active');

                // ПРИМЕНЯЕМ размер шрифта!
                applyFontSize(savedSize);

            }
        }
    });
</script>


</body>

</html>
