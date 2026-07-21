package com.rabin.taskmanager.api.config;

import java.net.URI;

public final class ApiTestConfig {
    public int mockApiPort() {
        return Integer.parseInt(property("mock.api.port", "0"));
    }

    public URI baseUri(int port) {
        return URI.create("http://127.0.0.1:" + port);
    }

    public String nodeCommand() {
        return property("node.command", "node");
    }

    private String property(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank() || value.startsWith("${")) {
            return defaultValue;
        }
        return value;
    }
}

