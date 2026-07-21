package com.rabin.taskmanager.api.support;

import com.rabin.taskmanager.api.config.ApiTestConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class MockApiServer {
    private final ApiTestConfig config;
    private Process process;
    private int port;

    public MockApiServer(ApiTestConfig config) {
        this.config = config;
    }

    public int start() throws IOException, InterruptedException {
        port = config.mockApiPort() == 0 ? findFreePort() : config.mockApiPort();
        Path serverPath = Path.of("..", "mock-api", "server.js").toAbsolutePath().normalize();

        ProcessBuilder builder = new ProcessBuilder(config.nodeCommand(), serverPath.toString());
        builder.redirectErrorStream(true);
        builder.environment().putAll(Map.of("MOCK_API_PORT", String.valueOf(port)));
        process = builder.start();

        waitUntilHealthy();
        return port;
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
        }
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void waitUntilHealthy() throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        ApiHealthProbe probe = new ApiHealthProbe(config.baseUri(port));

        while (Instant.now().isBefore(deadline)) {
            if (!process.isAlive()) {
                throw new IllegalStateException("Mock API process stopped before becoming healthy");
            }

            if (probe.isHealthy()) {
                return;
            }

            Thread.sleep(250);
        }

        throw new IllegalStateException("Mock API did not become healthy on port " + port);
    }
}

