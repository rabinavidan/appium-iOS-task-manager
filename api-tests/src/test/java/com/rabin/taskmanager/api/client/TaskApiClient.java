package com.rabin.taskmanager.api.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabin.taskmanager.api.models.ErrorResponse;
import com.rabin.taskmanager.api.models.TaskRequest;
import com.rabin.taskmanager.api.models.TaskResponse;
import io.qameta.allure.Step;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class TaskApiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final URI baseUri;

    public TaskApiClient(URI baseUri) {
        this.baseUri = baseUri;
    }

    @Step("Check API health")
    public ApiResponse<Map<String, Object>> health() throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/health").GET().build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), new TypeReference<>() {}));
    }

    @Step("Create task through API: {0.title}")
    public ApiResponse<TaskResponse> createTask(TaskRequest task) throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(task)))
                .build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), TaskResponse.class));
    }

    @Step("Create invalid task through API")
    public ApiResponse<ErrorResponse> createInvalidTask(TaskRequest task) throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(task)))
                .build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), ErrorResponse.class));
    }

    @Step("Get task through API: {0}")
    public ApiResponse<TaskResponse> getTask(String taskId) throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks/" + taskId).GET().build());
        if (response.statusCode() != 200) {
            return new ApiResponse<>(response.statusCode(), null);
        }
        return new ApiResponse<>(response.statusCode(), read(response.body(), TaskResponse.class));
    }

    @Step("List tasks through API")
    public ApiResponse<List<TaskResponse>> listTasks() throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks").GET().build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), new TypeReference<>() {}));
    }

    @Step("Complete task through API: {0}")
    public ApiResponse<TaskResponse> completeTask(String taskId) throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks/" + taskId)
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{\"isCompleted\":true}"))
                .build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), TaskResponse.class));
    }

    @Step("Delete task through API: {0}")
    public ApiResponse<Map<String, Object>> deleteTask(String taskId) throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks/" + taskId).DELETE().build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), new TypeReference<>() {}));
    }

    @Step("Clear all tasks through API")
    public ApiResponse<Map<String, Object>> clearTasks() throws IOException, InterruptedException {
        HttpResponse<String> response = send(request("/tasks").DELETE().build());
        return new ApiResponse<>(response.statusCode(), read(response.body(), new TypeReference<>() {}));
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private <T> T read(String body, Class<T> type) throws IOException {
        return MAPPER.readValue(body, type);
    }

    private <T> T read(String body, TypeReference<T> type) throws IOException {
        return MAPPER.readValue(body, type);
    }
}
