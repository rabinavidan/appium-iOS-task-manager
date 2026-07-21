package com.rabin.taskmanager.api.tests;

import com.rabin.taskmanager.api.client.ApiResponse;
import com.rabin.taskmanager.api.models.ErrorResponse;
import com.rabin.taskmanager.api.models.TaskRequest;
import com.rabin.taskmanager.api.models.TaskResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Task Manager API")
@Feature("Functional Coverage")
@Tag("api")
@Tag("feature")
class TaskApiFeatureTest extends BaseApiTest {

    @Test
    @Story("Validation")
    void rejectsTaskWithoutTitle() throws Exception {
        ApiResponse<ErrorResponse> response = tasks.createInvalidTask(TaskRequest.create(" ", "Missing title", "Medium"));

        assertEquals(400, response.statusCode());
        assertEquals("Task title is required", response.body().error());
    }

    @Test
    @Story("Read task")
    void readsTaskById() throws Exception {
        TaskResponse created = tasks.createTask(TaskRequest.create("Read API task", "Read flow", "Low")).body();

        ApiResponse<TaskResponse> response = tasks.getTask(created.id());

        assertEquals(200, response.statusCode());
        assertEquals(created.id(), response.body().id());
        assertEquals("Read API task", response.body().title());
    }

    @Test
    @Story("Complete task")
    void completesTask() throws Exception {
        TaskResponse created = tasks.createTask(TaskRequest.create("Complete API task", "Complete flow", "Medium")).body();

        ApiResponse<TaskResponse> response = tasks.completeTask(created.id());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().isCompleted());
    }

    @Test
    @Story("Delete task")
    void deletesTask() throws Exception {
        TaskResponse created = tasks.createTask(TaskRequest.create("Delete API task", "Delete flow", "Medium")).body();

        assertEquals(200, tasks.deleteTask(created.id()).statusCode());
        assertEquals(404, tasks.getTask(created.id()).statusCode());
    }

    @Test
    @Story("Clear all tasks")
    void clearsAllTasks() throws Exception {
        tasks.createTask(TaskRequest.create("First API task", "Clear flow", "Medium"));
        tasks.createTask(TaskRequest.create("Second API task", "Clear flow", "High"));

        assertEquals(200, tasks.clearTasks().statusCode());
        ApiResponse<List<TaskResponse>> response = tasks.listTasks();

        assertEquals(200, response.statusCode());
        assertTrue(response.body().isEmpty());
    }
}

