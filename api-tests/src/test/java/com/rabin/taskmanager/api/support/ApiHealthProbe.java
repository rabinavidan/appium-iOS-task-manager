package com.rabin.taskmanager.api.support;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class ApiHealthProbe {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(1))
            .build();
    private final URI baseUri;

    ApiHealthProbe(URI baseUri) {
        this.baseUri = baseUri;
    }

    boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder(baseUri.resolve("/health"))
                    .timeout(Duration.ofSeconds(1))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception ignored) {
            return false;
        }
    }
}

