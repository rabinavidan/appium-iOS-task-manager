package com.rabin.taskmanager.core;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class AppiumActions {
    private final IOSDriver driver;
    private final WebDriverWait wait;

    public AppiumActions(IOSDriver driver, Duration waitTimeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, waitTimeout);
    }

    @Step("Find by accessibility id: {0}")
    public WebElement byAccessibilityId(String accessibilityId) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(AppiumBy.accessibilityId(accessibilityId)));
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Could not find accessibility id: " + accessibilityId, error);
        }
    }

    @Step("Find by accessibility id or text: {0} / {1}")
    public WebElement byAccessibilityIdOrText(String accessibilityId, String text) {
        try {
            return wait.until(driver -> {
                WebElement byId = firstVisible(AppiumBy.accessibilityId(accessibilityId));
                if (byId != null) {
                    return byId;
                }
                return firstVisible(textLocator(text));
            });
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Could not find accessibility id '" + accessibilityId + "' or text '" + text + "'", error);
        }
    }

    @Step("Find text field: {0} / {1}")
    public WebElement textField(String accessibilityId, String placeholder) {
        try {
            return wait.until(driver -> {
                WebElement byId = firstVisible(AppiumBy.accessibilityId(accessibilityId));
                if (byId != null) {
                    return byId;
                }

                WebElement byPlaceholder = firstVisible(AppiumBy.iOSNsPredicateString(
                        "type == 'XCUIElementTypeTextField' AND " +
                                "(name == '" + escapePredicateValue(placeholder) + "' OR " +
                                "label == '" + escapePredicateValue(placeholder) + "' OR " +
                                "value == '" + escapePredicateValue(placeholder) + "' OR " +
                                "placeholderValue == '" + escapePredicateValue(placeholder) + "')"
                ));
                if (byPlaceholder != null) {
                    return byPlaceholder;
                }

                return firstVisible(AppiumBy.iOSClassChain("**/XCUIElementTypeTextField"));
            });
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Could not find text field: " + accessibilityId + " / " + placeholder, error);
        }
    }

    @Step("Find button: {0} / {1}")
    public WebElement button(String accessibilityId, String text) {
        try {
            return wait.until(driver -> {
                WebElement byId = accessibilityId.isBlank() ? null : firstVisible(AppiumBy.accessibilityId(accessibilityId));
                if (byId != null) {
                    return byId;
                }

                WebElement byText = firstVisible(textLocator(text));
                if (byText != null) {
                    return byText;
                }

                return firstVisible(AppiumBy.iOSNsPredicateString(
                        "type == 'XCUIElementTypeButton' AND " +
                                "(name CONTAINS '" + escapePredicateValue(text) + "' OR " +
                                "label CONTAINS '" + escapePredicateValue(text) + "')"
                ));
            });
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Could not find button: " + accessibilityId + " / " + text, error);
        }
    }

    @Step("Find text: {0}")
    public WebElement byText(String text) {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(textLocator(text)));
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Could not find text: " + text, error);
        }
    }

    @Step("Check current screen contains text: {0}")
    public boolean isTextVisible(String text) {
        return !driver.findElements(textLocator(text)).isEmpty();
    }

    @Step("Check current screen contains accessibility id: {0}")
    public boolean isAccessibilityIdVisible(String accessibilityId) {
        return firstVisible(AppiumBy.accessibilityId(accessibilityId)) != null;
    }

    @Step("Wait until text is visible: {0}")
    public void waitUntilTextVisible(String text) {
        try {
            wait.until(driver -> scrollToText(text));
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Text did not become visible: " + text, error);
        }
    }

    @Step("Wait until text is hidden: {0}")
    public void waitUntilTextHidden(String text) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(textLocator(text)));
        } catch (TimeoutException error) {
            throw timeoutWithPageSource("Text did not become hidden: " + text, error);
        }
    }

    @Step("Scroll to text: {0}")
    public boolean scrollToText(String text) {
        if (isTextVisible(text)) {
            return true;
        }

        for (int attempt = 0; attempt < 8; attempt++) {
            Map<String, Object> args = new HashMap<>();
            args.put("direction", "up");
            try {
                driver.executeScript("mobile: swipe", args);
            } catch (RuntimeException ignored) {
                return isTextVisible(text);
            }

            if (isTextVisible(text)) {
                return true;
            }
        }

        return isTextVisible(text);
    }

    @Step("Swipe element left")
    public void swipeElementLeft(WebElement element) {
        Map<String, Object> args = new HashMap<>();
        args.put("direction", "left");
        args.put("elementId", ((RemoteWebElement) element).getId());
        driver.executeScript("mobile: swipe", args);
    }

    @Step("Hide keyboard if present")
    public void hideKeyboardIfPresent() {
        try {
            driver.hideKeyboard();
        } catch (RuntimeException ignored) {
            // Keyboard may already be closed.
        }
    }

    @Step("Activate app: {0}")
    public void activateApp(String bundleId) {
        driver.activateApp(bundleId);
    }

    @Step("Terminate app: {0}")
    public void terminateApp(String bundleId) {
        driver.terminateApp(bundleId);
    }

    private By textLocator(String text) {
        return AppiumBy.iOSNsPredicateString(textPredicate(text));
    }

    private String textPredicate(String text) {
        String escapedText = escapePredicateValue(text);
        return "label == '" + escapedText + "' OR name == '" + escapedText + "' OR value == '" + escapedText + "'";
    }

    private WebElement firstVisible(By locator) {
        try {
            for (WebElement element : driver.findElements(locator)) {
                if (element.isDisplayed()) {
                    return element;
                }
            }
            return null;
        } catch (NoSuchElementException | TimeoutException ignored) {
            return null;
        }
    }

    private TimeoutException timeoutWithPageSource(String message, TimeoutException error) {
        String pageSource = driver == null ? "" : driver.getPageSource();
        String clippedSource = pageSource.length() > 4000 ? pageSource.substring(0, 4000) + "\n..." : pageSource;
        return new TimeoutException(message + "\nVisible page source excerpt:\n" + clippedSource, error);
    }

    private String escapePredicateValue(String value) {
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
