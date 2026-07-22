package com.rabin.taskmanager.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class HybridTaskApiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final URI baseUri;

    public HybridTaskApiClient(URI baseUri) {
        this.baseUri = baseUri;
    }

    @Step("API: create task '{0}' with priority '{2}'")
    public HybridTaskResponse createTask(String title, String notes, String priority)
            throws IOException, InterruptedException {
        String body = MAPPER.writeValueAsString(Map.of(
                "title", title,
                "notes", notes,
                "priority", priority));
        HttpResponse<String> resp = send(
                HttpRequest.newBuilder(baseUri.resolve("/tasks"))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json")
                        .build());
        if (resp.statusCode() != 201) {
            throw new AssertionError("Expected 201 from POST /tasks, got " + resp.statusCode() + ": " + resp.body());
        }
        return MAPPER.readValue(resp.body(), HybridTaskResponse.class);
    }

    @Step("API: get task '{0}'")
    public HybridTaskResponse getTask(String taskId) throws IOException, InterruptedException {
        HttpResponse<String> resp = send(
                HttpRequest.newBuilder(baseUri.resolve("/tasks/" + taskId)).GET().build());
        if (resp.statusCode() != 200) {
            throw new AssertionError("Expected 200 from GET /tasks/" + taskId + ", got " + resp.statusCode());
        }
        return MAPPER.readValue(resp.body(), HybridTaskResponse.class);
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
