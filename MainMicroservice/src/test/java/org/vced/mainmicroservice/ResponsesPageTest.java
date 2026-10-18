//package org.vced.mainmicroservice;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.junit.jupiter.api.AfterEach;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
//@ActiveProfiles("test-with-ldap")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
//public class ResponsesPageTest {
//
//    private static final String VALID_USERNAME = "admin";
//    private static final String VALID_PASSWORD = "admin";
//    private static final String REGULAR_USERNAME = "bob";
//    private static final String REGULAR_PASSWORD = "bobspassword";
//
//    private WebDriver driver;
//    private WebDriverWait wait;
//    private final String BASE_URL = "http://localhost:8080";
//
//    private void loginIfNeeded() {
//    if (driver.getCurrentUrl().contains("/login")) {
//        driver.findElement(By.id("email")).sendKeys(VALID_USERNAME);
//        driver.findElement(By.id("password")).sendKeys(VALID_PASSWORD);
//        driver.findElement(By.cssSelector(".btn")).click();
//        wait.until(ExpectedConditions.urlContains("/"));
//        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
//    }
//}
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
//        loginIfNeeded();
//    }
//
//
//    @AfterEach
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
//
//    @Test
//    public void headerShouldContainLogoAndUserPanel() {
//        // Проверка логотипа
//        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".logo img")));
//        assertTrue(logo.isDisplayed());
//        assertTrue(logo.getAttribute("src").contains("/images/logo.png"));
//
//        // Проверка панели пользователя
//        WebElement userPanel = driver.findElement(By.id("userPanel"));
//        assertTrue(userPanel.isDisplayed());
//
//        // Проверка аватара
//        WebElement userAvatar = userPanel.findElement(By.cssSelector(".user-avatar img"));
//        assertTrue(userAvatar.isDisplayed());
//        assertTrue(userAvatar.getAttribute("src").contains("/images/user.png"));
//    }
//
//    @Test
//    public void userDropdownShouldContainAllItems() {
//        // Открываем меню
//        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userPanel")));
//        WebElement userPanel = driver.findElement(By.id("userPanel"));
//        userPanel.click();
//
//        // Ждем появления меню
//        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".dropdown-menu")));
//        assertTrue(dropdown.isDisplayed());
//
//        // Проверяем пункты меню
//        WebElement fontSizeSwitcher = dropdown.findElement(By.cssSelector(".font-size-switcher"));
//        assertEquals(3, fontSizeSwitcher.findElements(By.tagName("button")).size());
//
//        WebElement logoutBtn = dropdown.findElement(By.cssSelector(".logout-btn"));
//        assertEquals("Выйти", logoutBtn.getText());
//
//        // Проверяем кнопку администратора (если пользователь админ)
//        WebElement adminBtn = dropdown.findElement(By.cssSelector(".admin-mod"));
//            assertEquals("Панель администратора", adminBtn.getText());
//    }
//
//    @Test
//    public void fontSizeSwitcherShouldChangeDynamicFontElements() {
//        // Открываем выпадающее меню пользователя
//        WebElement userPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".user-panel")));
//        userPanel.click();
//
//
//        // Находим переключатель размера шрифта
//        WebElement fontSizeSwitcher = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".font-size-switcher")));
//
//        fontSizeSwitcher.findElement(By.xpath(".//button[contains(text(),'M')]")).click();
//
//        // Получаем все элементы с динамическим шрифтом
//        List<WebElement> dynamicFontElements = driver.findElements(By.cssSelector(".dynamic-font"));
//        assertFalse(dynamicFontElements.isEmpty(), "Должны быть элементы с dynamic-font");
//
//        // Запоминаем исходные размеры шрифта
//        Map<WebElement, String> originalFontSizes = new HashMap<>();
//        for (WebElement element : dynamicFontElements) {
//            originalFontSizes.put(element, element.getCssValue("font-size"));
//        }
//
//        // Нажимаем кнопку "L" (большой шрифт)
//        WebElement largeFontBtn = fontSizeSwitcher.findElement(By.xpath(".//button[contains(text(),'L')]"));
//        largeFontBtn.click();
//
//        // Проверяем что шрифт увеличился у всех элементов
//        for (WebElement element : dynamicFontElements) {
//            String newSize = element.getCssValue("font-size");
//            assertNotEquals(originalFontSizes.get(element), newSize,
//                "Размер шрифта должен измениться после нажатия L");
//        }
//
//        // Нажимаем кнопку "S" (маленький шрифт)
//        WebElement smallFontBtn = fontSizeSwitcher.findElement(By.xpath(".//button[contains(text(),'S')]"));
//        smallFontBtn.click();
//
//        // Проверяем что шрифт уменьшился
//        for (WebElement element : dynamicFontElements) {
//            String newSize = element.getCssValue("font-size");
//            assertTrue(parseFontSize(newSize) < parseFontSize(originalFontSizes.get(element)),
//                "Размер шрифта должен быть меньше исходного после нажатия S");
//        }
//    }
//
//    // Вспомогательный метод для преобразования размера шрифта (например "16px" → 16)
//    private float parseFontSize(String size) {
//        return Float.parseFloat(size.replace("px", ""));
//    }
//
//    @Test
//    public void testLogoutButton() {
//        WebElement userPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userPanel")));
//        userPanel.click();
//
//        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".dropdown-menu")));
//        assertTrue(dropdown.isDisplayed());
//
//        WebElement logoutButton = driver.findElement(By.cssSelector(".logout-btn")); // или By.cssSelector(".btn-logout")
//        logoutButton.click();
//        wait.until(ExpectedConditions.urlContains("/login"));
//
//        // Проверяем, что после выхода нас перенаправляет на страницу логина
//        assertTrue(driver.getCurrentUrl().contains("/login"), "После выхода не произошел переход на страницу входа");
//    }
//
//    @Test
//    public void testAdminPanelButton() {
//        WebElement userPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userPanel")));
//        userPanel.click();
//
//        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".dropdown-menu")));
//        assertTrue(dropdown.isDisplayed());
//
//        WebElement adminButton = driver.findElement(By.cssSelector(".admin-mod"));
//
//        assertTrue(adminButton.isDisplayed(), "Кнопка админки не отображается для администратора");
//
//        WebElement adminToggle = driver.findElement(By.cssSelector(".toggle-switch"));
//
//        assertTrue(adminToggle.isDisplayed(), "Кнопка контроля доступа к нс не отображается для администратора");
//    }
//
//    @Test
//    public void testClickOnCardFillsSearchInput() {
//        // Находим карточку и поле ввода
//        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".question-card")));
//        WebElement searchInput = driver.findElement(By.id("searchInput"));
//
//        // Получаем текст из карточки (из тега <p>)
//        String expectedText = card.findElement(By.tagName("p")).getText();
//
//        // Кликаем на карточку
//        card.click();
//
//        // Проверяем, что текст вставился в поле поиска
//        String actualText = searchInput.getAttribute("value");
//        assertEquals(expectedText, actualText,
//            "Текст карточки не совпадает с текстом в поле поиска");
//    }
//
//    @Test
//    public void testAiAccessToggleAndUserView() throws InterruptedException {
//        // 1. Логинимся как админ (уже выполнено в setUp/loginIfNeeded)
//
//        // 2. Открываем меню пользователя
//        WebElement userPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userPanel")));
//        userPanel.click();
//
//        // 3. Находим и кликаем переключатель доступа к нейросети
//        WebElement dropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(
//            By.cssSelector(".dropdown-menu")));
//        WebElement aiAccessToggle = wait.until(ExpectedConditions.elementToBeClickable(
//            By.id("sliderForAiAccessToggle")));
//
//        // Кликаем, чтобы изменить состояние
//        aiAccessToggle.click();
//
//        // Ждем немного для применения изменений
//        Thread.sleep(1000);
//
//        // 4. Выходим из системы
//        WebElement logoutButton = dropdown.findElement(By.cssSelector(".logout-btn"));
//        logoutButton.click();
//        wait.until(ExpectedConditions.urlContains("/login"));
//    }
//}
