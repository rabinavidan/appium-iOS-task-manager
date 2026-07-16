package com.rabin.taskmanager.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Task Manager")
@Feature("Functional Coverage")
@Tag("feature")
class TaskManagerFeatureTest extends BaseIosTest {

    @Test
    @Story("Core controls")
    void showsCoreTaskControls() {
        tasks.waitUntilReady();

        tasks.titleField();
        tasks.notesField();
        tasks.addTaskButton();
        tasks.waitForText("Medium");
        tasks.waitForText("All");
    }

    @Test
    @Story("Validation")
    void addButtonRequiresTaskTitle() {
        WebElement addButton = tasks.addTaskButton();
        assertFalse(Boolean.parseBoolean(addButton.getAttribute("enabled")), "Add Task should be disabled without a title");

        tasks.enterTaskTitle(uniqueTaskName("Validation"));
        addButton = tasks.addTaskButton();
        assertTrue(Boolean.parseBoolean(addButton.getAttribute("enabled")), "Add Task should be enabled after entering a title");
        addButton.click();
    }

    @Test
    @Story("Default metadata")
    void addsTaskWithDefaultPriorityAndDueDate() {
        String taskName = uniqueTaskName("Default");

        taskManager.addTask(taskName, "Default metadata");

        tasks.waitForText(taskName);
        tasks.waitForText("Default metadata");
        tasks.waitForText("Medium");
    }

    @Test
    @Story("Priority")
    void addsTaskWithHighPriority() {
        String taskName = uniqueTaskName("High");

        taskManager.addTask(taskName, "High priority flow", "High");

        tasks.waitForText(taskName);
        tasks.waitForText("High");
        tasks.tapFilter("All");
    }

    @Test
    @Story("Completion toggle")
    void completesAndReopensTask() {
        String taskName = uniqueTaskName("Toggle");

        taskManager.addTask(taskName, "Toggle completion");
        taskManager.completeTask(taskName);
        taskManager.reopenTask(taskName);

        tasks.tapFilter("Open");
        assertTrue(tasks.isTextVisible(taskName), "Expected reopened task to be visible in Open");
        tasks.tapFilter("All");
    }

    @Test
    @Story("Filters")
    void filtersCompletedTasksBetweenDoneOpenAndAll() {
        String taskName = uniqueTaskName("Filter");

        taskManager.addTask(taskName, "Filter flow");
        taskManager.completeTask(taskName);

        tasks.tapFilter("Done");
        assertTrue(tasks.isTextVisible(taskName), "Expected completed task to be visible in Done");

        tasks.tapFilter("Open");
        assertFalse(tasks.isTextVisible(taskName), "Expected completed task to be hidden from Open");

        tasks.tapFilter("All");
        assertTrue(tasks.isTextVisible(taskName), "Expected completed task to be visible in All");
    }

    @Test
    @Story("Persistence")
    void persistsTaskAfterAppRestart() {
        String taskName = uniqueTaskName("Persist");

        taskManager.addTask(taskName, "Persistence flow");
        taskManager.restartApp();
        tasks.tapFilter("All");

        assertTrue(tasks.isTextVisible(taskName), "Expected task to remain visible after app restart");
    }

    @Test
    @Story("Delete")
    void deletesTaskFromList() {
        String taskName = uniqueTaskName("Delete");

        taskManager.addTask(taskName, "Delete flow");
        taskManager.deleteTask(taskName);

        assertFalse(tasks.isTextVisible(taskName), "Expected deleted task to be removed");
    }

    @Test
    @Story("Clear all")
    void clearsAllTasksFromList() {
        String firstTaskName = uniqueTaskName("Clear One");
        String secondTaskName = uniqueTaskName("Clear Two");

        taskManager.addTask(firstTaskName, "Clear all flow");
        taskManager.addTask(secondTaskName, "Clear all flow");
        taskManager.clearAllTasks();

        assertTrue(tasks.isEmpty(), "Expected the task list to be empty after clearing all tasks");
        assertFalse(tasks.isTextVisible(firstTaskName), "Expected first cleared task to be removed");
        assertFalse(tasks.isTextVisible(secondTaskName), "Expected second cleared task to be removed");
    }
}
