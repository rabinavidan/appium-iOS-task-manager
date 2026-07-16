package com.rabin.taskmanager.reporting;

import io.appium.java_client.ios.IOSDriver;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AllureArtifacts {
    private static final Logger LOGGER = LoggerFactory.getLogger(AllureArtifacts.class);
    private static final ThreadLocal<IOSDriver> DRIVER = new ThreadLocal<>();

    private AllureArtifacts() {
    }

    public static void setDriver(IOSDriver driver) {
        DRIVER.set(driver);
    }

    public static void clearDriver() {
        DRIVER.remove();
    }

    public static void attachFailureArtifacts(Throwable error) {
        LOGGER.error("Test failed", error);
        attachText("failure-message", error.getMessage());

        IOSDriver driver = DRIVER.get();
        if (driver != null) {
            attachScreenshot(driver);
            attachPageSource(driver);
        }

        attachLogFile(Path.of("target", "logs", "ui-tests.log"));
    }

    public static void attachText(String name, String content) {
        if (content == null) {
            return;
        }
        Allure.addAttachment(name, "text/plain", content);
    }

    private static void attachScreenshot(IOSDriver driver) {
        try {
            byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("screenshot", "image/png", new ByteArrayInputStream(screenshot), ".png");
        } catch (RuntimeException error) {
            LOGGER.warn("Could not attach screenshot", error);
        }
    }

    private static void attachPageSource(IOSDriver driver) {
        try {
            Allure.addAttachment("page-source", "application/xml", driver.getPageSource(), ".xml");
        } catch (RuntimeException error) {
            LOGGER.warn("Could not attach page source", error);
        }
    }

    private static void attachLogFile(Path logFile) {
        if (!Files.exists(logFile)) {
            return;
        }

        try (InputStream stream = Files.newInputStream(logFile)) {
            Allure.addAttachment("ui-tests.log", "text/plain", stream, ".log");
        } catch (IOException error) {
            LOGGER.warn("Could not attach log file {}", logFile, error);
        }
    }
}
