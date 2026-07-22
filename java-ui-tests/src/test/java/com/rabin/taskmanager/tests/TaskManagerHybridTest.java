package com.rabin.taskmanager.tests;

import com.rabin.taskmanager.hybrid.HybridTaskResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Hybrid test: both layers share a task name so we can assert the same business
 * rule (default priority = "Medium", new task starts open) holds independently in
 * the API and the iOS app.  Neither system talks to the other — this is a
 * cross-channel contract check, not an integration between them.
 *
 * Flow:
 *   1. API   — POST /tasks → confirm server defaults (priority, completion state)
 *   2. UI    — add an identical task through the iOS app
 *   3. UI    — complete the task, verify it moves to the Done filter
 *   4. API   — GET /tasks/:id → confirm the API task is still retrievable
 */
@Epic("Task Manager")
@Feature("Hybrid")
@Tag("hybrid")
class TaskManagerHybridTest extends BaseHybridTest {

    @Test
    @Story("Cross-layer contract: default priority and open state")
    void apiAndUiAgreeOnDefaultsAndCompletionFlow() throws Exception {
        String baseName = uniqueTaskName("Hybrid");
        String apiTitle = baseName + " via API";
        String uiTitle  = baseName + " via UI";

        // ── API layer ─────────────────────────────────────────────────────────
        // Create a task without explicit defaults; the server fills them in.
        HybridTaskResponse apiTask = api.createTask(apiTitle, "Seeded by API", "Medium");

        assertEquals("Medium",  apiTask.priority(),    "API default priority should be Medium");
        assertFalse(apiTask.isCompleted(),              "API task should start as not completed");
        assertNotNull(apiTask.id(),                     "API task should have a generated ID");

        // ── UI layer ──────────────────────────────────────────────────────────
        // Add a task using the same default priority (no explicit selection in the app).
        taskManager.addTask(uiTitle, "Created via Appium");

        assertTrue(tasks.isTextVisible(uiTitle),   "UI task should appear in the task list");
        assertTrue(tasks.isTextVisible("Medium"),  "UI should show Medium as the default priority");

        tasks.tapFilter("Open");
        assertTrue(tasks.isTextVisible(uiTitle),   "UI task should be in the Open filter by default");
        tasks.tapFilter("All");

        // Complete the UI task and verify the Done filter.
        taskManager.completeTask(uiTitle);
        tasks.tapFilter("Done");
        assertTrue(tasks.isTextVisible(uiTitle),   "Completed UI task should appear in Done filter");
        tasks.tapFilter("All");

        // ── API data-integrity check ──────────────────────────────────────────
        // The task we created via API should still be retrievable by its ID.
        HybridTaskResponse retrieved = api.getTask(apiTask.id());
        assertEquals(apiTitle, retrieved.title(), "API task title should survive a GET by ID");
    }
}
