//package org.vced.mainmicroservice;
//
//import java.time.Duration;
//
//import org.junit.jupiter.api.AfterEach;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ActiveProfiles("test-with-ldap")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//public class LoginPageTest {
//
//    private static final String VALID_USERNAME = "admin";
//    private static final String VALID_PASSWORD = "admin";
//    private static final String INVALID_USERNAME = "invalid@example.com";
//    private static final String INVALID_PASSWORD = "wrongpassword";
//
//    private static ConfigurableApplicationContext server;
//    private WebDriver driver;
//    private WebDriverWait wait;
//    private final String BASE_URL = "http://localhost:8080";
//
//    @BeforeEach
//    public void setUp() {
//        WebDriverManager.chromedriver().setup();
//
//        // Настройка ChromeOptions для headless-режима
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless"); // Включение headless-режима
//        options.addArguments("--disable-gpu"); // Отключение GPU (может потребоваться для некоторых систем)
//        options.addArguments("--window-size=1920,1080"); // Установка размера окна (даже в headless-режиме)
//
//        driver = new ChromeDriver(options);
//        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
//        driver.manage().deleteAllCookies();
//        driver.get(BASE_URL + "/login");
//    }
//
//    @AfterEach
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
//
//    @Test
//    public void pageTitleShouldBeCorrect() {
//        assertEquals("ElectronHelper", driver.getTitle());
//    }
//
//    @Test
//    public void logoShouldBeDisplayed() {
//        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".logo img")));
//        assertTrue(logo.isDisplayed());
//        assertTrue(logo.getAttribute("src").contains("/images/logo.png"));
//    }
//
//    @Test
//    public void loginFormElementsShouldBePresent() {
//        // Проверка заголовка поля email
//        // WebElement emailLabel = driver.findElement(By.id("label-for-email"));
//        // assertEquals("Рабочий e-mail", emailLabel.getText());
//
//        // Проверка поля email
//        WebElement emailInput = driver.findElement(By.id("email"));
//        assertEquals("LDAP Username", emailInput.getAttribute("placeholder"));
//        assertTrue(emailInput.isDisplayed());
//
//        // Проверка заголовка поля пароля
//        // WebElement passwordLabel = driver.findElement(By.id("label-for-password"));
//        // assertEquals("Пароль", passwordLabel.getText());
//
//        // Проверка поля пароля
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        assertEquals("Password", passwordInput.getAttribute("placeholder"));
//        assertTrue(passwordInput.isDisplayed());
//
//        // Проверка кнопки показать/скрыть пароль
//        WebElement togglePassword = driver.findElement(By.id("togglePassword"));
//        assertTrue(togglePassword.isDisplayed());
//
//        // Проверка кнопки входа
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        assertEquals("Войти", loginButton.getText());
//        assertTrue(loginButton.isDisplayed());
//        assertEquals("submit", loginButton.getAttribute("type"));
//    }
//
//    @Test
//    public void passwordToggleShouldChangeInputType() {
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        WebElement togglePassword = driver.findElement(By.id("togglePassword"));
//
//        // По умолчанию тип password
//        assertEquals("password", passwordInput.getAttribute("type"));
//
//        // Клик и проверка что тип изменился на text
//        togglePassword.click();
//        assertEquals("text", passwordInput.getAttribute("type"));
//
//        // Ещё клик - возврат к password
//        togglePassword.click();
//        assertEquals("password", passwordInput.getAttribute("type"));
//    }
//
//    @Test
//    public void csrfTokenShouldBePresent() {
//        WebElement csrfToken = driver.findElement(
//            By.xpath("//input[@name='_csrf'] | //input[@name='${_csrf.parameterName}']"));
//        assertNotNull(csrfToken);
//        assertFalse(csrfToken.getAttribute("value").isEmpty());
//    }
//
//    @Test
//    public void loginWithEmptyCredentialsShouldShowValidation() {
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        // Проверяем что остались на той же странице
//        assertTrue(driver.getCurrentUrl().endsWith("/login"));
//
//        // Проверяем что поля подсвечены как невалидные
//        WebElement emailInput = driver.findElement(By.id("email"));
//        assertTrue(emailInput.getAttribute("required").equals("true"));
//
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        assertTrue(passwordInput.getAttribute("required").equals("true"));
//    }
//
//    @Test
//    public void loginWithValidCredentialsShouldRedirectToHomePage() {
//        WebElement emailInput = driver.findElement(By.id("email"));
//        emailInput.sendKeys(VALID_USERNAME);
//
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        passwordInput.sendKeys(VALID_PASSWORD);
//
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
//
//        assertFalse(driver.getCurrentUrl().contains("/login"));
//    }
//
//    @Test
//    public void loginWithInvalidCredentialsShouldShowErrorMessage() {
//        WebElement emailInput = driver.findElement(By.id("email"));
//        emailInput.sendKeys(INVALID_USERNAME);
//
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        passwordInput.sendKeys(INVALID_PASSWORD);
//
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        assertTrue(driver.getCurrentUrl().contains("/login"));
//    }
//
//    @Test
//    public void loginWithInvalidUsernameShouldShowErrorMessage() {
//        WebElement emailInput = driver.findElement(By.id("email"));
//        emailInput.sendKeys(INVALID_USERNAME);
//
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        passwordInput.sendKeys(VALID_PASSWORD);
//
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        // Ждем появления сообщения об ошибке
//        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
//
//        assertTrue(errorMessage.isDisplayed());
//        assertTrue(errorMessage.getText().contains("Неверный логин или пароль"));
//        assertTrue(driver.getCurrentUrl().contains("/login?error=true"));
//    }
//
//    @Test
//    public void loginWithInvalidPasswordShouldShowErrorMessage() {
//        WebElement emailInput = driver.findElement(By.id("email"));
//        emailInput.sendKeys(VALID_USERNAME);
//
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        passwordInput.sendKeys(INVALID_PASSWORD);
//
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        // Ждем появления сообщения об ошибке
//        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
//
//        assertTrue(errorMessage.isDisplayed());
//        assertTrue(errorMessage.getText().contains("Неверный логин или пароль"));
//        assertTrue(driver.getCurrentUrl().contains("/login?error=true"));
//    }
//
//    @Test
//    public void errorMessageShouldDisappearAfterNewLoginAttempt() {
//        // Первая попытка с неверными данными
//        WebElement emailInput = driver.findElement(By.id("email"));
//        emailInput.sendKeys(INVALID_USERNAME);
//        WebElement passwordInput = driver.findElement(By.id("password"));
//        passwordInput.sendKeys(INVALID_PASSWORD);
//        WebElement loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        // Ждем появления ошибки
//        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".error-message")));
//        assertTrue(errorMessage.isDisplayed());
//
//        // Вторая попытка с правильными данными
//        emailInput = driver.findElement(By.id("email"));
//        emailInput.clear();
//        passwordInput = driver.findElement(By.id("password"));
//        passwordInput.clear();
//        emailInput.sendKeys(VALID_USERNAME);
//        passwordInput.sendKeys(VALID_PASSWORD);
//        loginButton = driver.findElement(By.cssSelector(".btn"));
//        loginButton.click();
//
//        // Ждем редиректа
//        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
//    }
//}
