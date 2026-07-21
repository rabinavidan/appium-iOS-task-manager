package com.rabin.taskmanager.api.tests;

import com.rabin.taskmanager.api.client.ApiResponse;
import com.rabin.taskmanager.api.models.TaskRequest;
import com.rabin.taskmanager.api.models.TaskResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("Task Manager API")
@Feature("Smoke")
@Tag("api")
@Tag("smoke")
class TaskApiSmokeTest extends BaseApiTest {

    @Test
    @Story("Health")
    void healthEndpointReturnsOk() throws Exception {
        ApiResponse<Map<String, Object>> response = tasks.health();

        assertEquals(200, response.statusCode());
        assertEquals("ok", response.body().get("status"));
    }

    @Test
    @Story("Create task")
    void createsTaskWithDefaultCompletionState() throws Exception {
        ApiResponse<TaskResponse> response = tasks.createTask(TaskRequest.create("API smoke task", "Created by API test", "High"));

        assertEquals(201, response.statusCode());
        assertNotNull(response.body().id());
        assertEquals("API smoke task", response.body().title());
        assertEquals("Created by API test", response.body().notes());
        assertEquals("High", response.body().priority());
        assertFalse(response.body().isCompleted());
    }
}

