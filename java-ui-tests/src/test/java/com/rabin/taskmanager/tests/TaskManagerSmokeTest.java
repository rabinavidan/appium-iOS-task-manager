package com.rabin.taskmanager.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Task Manager")
@Feature("Smoke")
@Tag("smoke")
class TaskManagerSmokeTest extends BaseIosTest {

    @Test
    @Story("Open task screen")
    void opensTasksScreen() {
        tasks.waitUntilReady();
    }

    @Test
    @Story("Add task")
    void addsTaskWithNotes() {
        String taskName = uniqueTaskName("Add");
        String notes = "Created by Appium";

        taskManager.addTask(taskName, notes);

        assertTrue(tasks.isTextVisible(taskName), "Expected the new task title to be visible");
        assertTrue(tasks.isTextVisible(notes), "Expected the new task notes to be visible");
    }

    @Test
    @Story("Complete and filter task")
    void completesTaskAndFiltersIt() {
        String taskName = uniqueTaskName("Done");

        taskManager.addTask(taskName, "Complete flow");
        taskManager.completeTask(taskName);

        tasks.tapFilter("Done");
        assertTrue(tasks.isTextVisible(taskName), "Expected completed task to be visible in Done");

        tasks.tapFilter("Open");
        assertFalse(tasks.isTextVisible(taskName), "Expected completed task to be hidden from Open");

        tasks.tapFilter("All");
    }
}
