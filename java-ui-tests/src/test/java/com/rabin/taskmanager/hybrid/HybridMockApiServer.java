package com.rabin.taskmanager.hybrid;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public final class HybridMockApiServer {
    private Process process;
    private int port;

    public int start() throws IOException, InterruptedException {
        port = findFreePort();
        Path serverPath = Path.of("..", "mock-api", "server.js").toAbsolutePath().normalize();
        ProcessBuilder builder = new ProcessBuilder("node", serverPath.toString());
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

    public URI baseUri() {
        return URI.create("http://127.0.0.1:" + port);
    }

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void waitUntilHealthy() throws InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .build();
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline)) {
            if (!process.isAlive()) {
                throw new IllegalStateException("Mock API process stopped before becoming healthy");
            }
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/health"))
                        .timeout(Duration.ofSeconds(1))
                        .GET()
                        .build();
                if (client.send(req, HttpResponse.BodyHandlers.ofString()).statusCode() == 200) {
                    return;
                }
            } catch (Exception ignored) {
            }
            Thread.sleep(250);
        }
        throw new IllegalStateException("Mock API did not become healthy on port " + port);
    }
}
