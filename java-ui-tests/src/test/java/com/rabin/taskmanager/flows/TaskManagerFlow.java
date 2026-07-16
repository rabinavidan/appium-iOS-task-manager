package com.rabin.taskmanager.flows;

import com.rabin.taskmanager.config.TestConfig;
import com.rabin.taskmanager.core.AppiumActions;
import com.rabin.taskmanager.screens.TasksScreen;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TaskManagerFlow {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskManagerFlow.class);

    private final TestConfig config;
    private final AppiumActions actions;
    private final TasksScreen tasks;

    public TaskManagerFlow(TestConfig config, AppiumActions actions, TasksScreen tasks) {
        this.config = config;
        this.actions = actions;
        this.tasks = tasks;
    }

    @Step("Add task: {0}")
    public void addTask(String taskName, String notes) {
        LOGGER.info("Adding task '{}'", taskName);
        tasks.tapFilter("All");
        tasks.enterTaskTitle(taskName);
        tasks.enterNotes(notes);
        tasks.closeKeyboard();
        tasks.tapAddTask();
        tasks.waitForText(taskName);
    }

    @Step("Add {2} priority task: {0}")
    public void addTask(String taskName, String notes, String priority) {
        LOGGER.info("Adding task '{}' with priority '{}'", taskName, priority);
        tasks.selectPriority(priority);
        addTask(taskName, notes);
    }

    @Step("Complete task: {0}")
    public void completeTask(String taskName) {
        LOGGER.info("Completing task '{}'", taskName);
        tasks.waitForText(taskName);
        tasks.tapText("Mark " + taskName + " complete");
        tasks.waitForText("Mark " + taskName + " incomplete");
    }

    @Step("Reopen task: {0}")
    public void reopenTask(String taskName) {
        LOGGER.info("Reopening task '{}'", taskName);
        tasks.waitForText(taskName);
        tasks.tapText("Mark " + taskName + " incomplete");
        tasks.waitForText("Mark " + taskName + " complete");
    }

    @Step("Restart app")
    public void restartApp() {
        LOGGER.info("Restarting app '{}'", config.bundleId());
        actions.terminateApp(config.bundleId());
        actions.activateApp(config.bundleId());
        tasks.waitUntilReady();
    }

    @Step("Delete task: {0}")
    public void deleteTask(String taskName) {
        LOGGER.info("Deleting task '{}'", taskName);
        tasks.deleteTask(taskName);
    }

    @Step("Clear all tasks")
    public void clearAllTasks() {
        LOGGER.info("Clearing all tasks");
        tasks.clearAllTasks();
    }

    public void clearAllTasksIfPossible() {
        try {
            LOGGER.info("Clearing remaining task data if possible");
            tasks.clearAllTasksWithoutReport();
        } catch (RuntimeException error) {
            LOGGER.warn("Could not clear remaining task data during cleanup", error);
        }
    }
}
