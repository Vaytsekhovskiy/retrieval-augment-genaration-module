<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ElectronHelper</title>
    <link rel="icon" href="/images/favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="/images/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="/css/login.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Inter:ital,opsz,wght@0,14..32,100..900;1,14..32,100..900&display=swap"
        rel="stylesheet">
</head>

<body>
    <div class="gradient-background">
        <div class="lines">
            <div class="content">
                <div class="login-form">
                    <form action="/login" method="post">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <div class="input-box">
                            <#--  <label for="email" id="label-for-email">Рабочий e-mail</label>  -->
                            <input type="text" name="username" id="email" placeholder="LDAP Username" required>
                        </div>

                        <div class="input-box">
                            <div class="label-container">
                                <#--  <label for="password" id="label-for-password">Пароль</label>  -->
                                <#--  <a href="#" class="forgot-password">Забыли пароль?</a>  -->
                            </div>
                            <div class="password-container">
                                <input type="password" name="password" id="password" placeholder="Password" required>
                                <svg id="togglePassword" width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 5C5 5 1 12 1 12C1 12 5 19 12 19C19 19 23 12 23 12C23 12 19 5 12 5Z" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <path d="M12 15C13.6569 15 15 13.6569 15 12C15 10.3431 13.6569 9 12 9C10.3431 9 9 10.3431 9 12C9 13.6569 10.3431 15 12 15Z" stroke="#6A6A6A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </div>
                        </div>
                        <#if errorMessage??>
                            <div class="error-message">
                                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <path d="M12 8V12" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    <path d="M12 16H12.01" stroke="#FF4D4F" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                                <span>${errorMessage}</span>
                            </div>
                        </#if>
                        <button type="submit" class="btn">Войти</button>
                        <div class="register-link">
<#--                            <p>Нет аккаунта? <a href="/registration">Зарегистрироваться</a></p>-->
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="/js/login.js"></script>
</body>

</html>