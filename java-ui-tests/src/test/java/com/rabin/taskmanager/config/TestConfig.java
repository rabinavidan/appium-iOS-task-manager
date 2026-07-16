package com.rabin.taskmanager.config;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public final class TestConfig {
    public String appiumServerUrl() {
        return property("appium.server.url", "http://127.0.0.1:4723");
    }

    public URL appiumServer() throws MalformedURLException {
        return URI.create(appiumServerUrl()).toURL();
    }

    public String deviceName() {
        return property("ios.device.name", "iPhone");
    }

    public String platformVersion() {
        return property("ios.platform.version", "");
    }

    public String udid() {
        return property("ios.udid", "");
    }

    public String bundleId() {
        return property("ios.bundle.id", "rubisoft.rabin-task-manager");
    }

    public boolean noReset() {
        return Boolean.parseBoolean(property("ios.no.reset", "true"));
    }

    public Duration waitTimeout() {
        return Duration.ofSeconds(Long.parseLong(property("ios.wait.seconds", "30")));
    }

    public String property(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank() || value.startsWith("${")) {
            return defaultValue;
        }
        return value;
    }
}
