package com.rabin.taskmanager.screens;

import com.rabin.taskmanager.core.AppiumActions;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TasksScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger(TasksScreen.class);

    private final AppiumActions actions;

    public TasksScreen(AppiumActions actions) {
        this.actions = actions;
    }

    @Step("Wait for Tasks screen")
    public void waitUntilReady() {
        actions.byAccessibilityIdOrText("tasks-screen", "Tasks");
    }

    @Step("Find task title field")
    public WebElement titleField() {
        return actions.textField("task-title-field", "Task name");
    }

    @Step("Find task notes field")
    public WebElement notesField() {
        return actions.textField("task-notes-field", "Notes");
    }

    @Step("Find Add Task button")
    public WebElement addTaskButton() {
        return actions.button("add-task-button", "Add Task");
    }

    @Step("Find Clear All Tasks button")
    public WebElement clearAllTasksButton() {
        return actions.button("clear-all-tasks-button", "Clear");
    }

    @Step("Tap Add Task")
    public void tapAddTask() {
        LOGGER.info("Tapping Add Task");
        addTaskButton().click();
    }

    @Step("Tap Clear All Tasks")
    public void tapClearAllTasks() {
        LOGGER.info("Tapping Clear All Tasks");
        clearAllTasksButton().click();
    }

    @Step("Confirm Clear All Tasks")
    public void confirmClearAllTasks() {
        LOGGER.info("Confirming Clear All Tasks");
        actions.button("confirm-clear-all-tasks-button", "Clear All Tasks").click();
    }

    @Step("Select priority: {0}")
    public void selectPriority(String priority) {
        LOGGER.info("Selecting priority '{}'", priority);
        actions.button("priority-picker", "Medium").click();
        actions.byText(priority).click();
    }

    @Step("Tap filter: {0}")
    public void tapFilter(String filterName) {
        LOGGER.info("Tapping filter '{}'", filterName);
        tapFilterWithoutReport(filterName);
    }

    public void tapFilterWithoutReport(String filterName) {
        String accessibilityId = "filter-" + filterName.toLowerCase();
        try {
            actions.byAccessibilityIdOrText(accessibilityId, filterName).click();
        } catch (RuntimeException ignored) {
            actions.byText(filterName).click();
        }
    }

    @Step("Enter task title: {0}")
    public void enterTaskTitle(String title) {
        LOGGER.info("Entering task title '{}'", title);
        titleField().sendKeys(title);
    }

    @Step("Enter task notes")
    public void enterNotes(String notes) {
        LOGGER.info("Entering task notes");
        notesField().sendKeys(notes);
    }

    @Step("Close keyboard if visible")
    public void closeKeyboard() {
        actions.hideKeyboardIfPresent();
    }

    @Step("Wait for text: {0}")
    public void waitForText(String text) {
        LOGGER.info("Waiting for text '{}'", text);
        actions.waitUntilTextVisible(text);
    }

    @Step("Check text is visible: {0}")
    public boolean isTextVisible(String text) {
        LOGGER.info("Checking visible text '{}'", text);
        return actions.scrollToText(text);
    }

    @Step("Check task list is empty")
    public boolean isEmpty() {
        return actions.isAccessibilityIdVisible("empty-state") || actions.isTextVisible("No tasks yet");
    }

    @Step("Clear all tasks from screen")
    public void clearAllTasks() {
        clearAllTasksWithoutReport();
    }

    public void clearAllTasksWithoutReport() {
        tapFilterWithoutReport("All");
        if (isEmpty()) {
            LOGGER.info("Task list is already empty");
            return;
        }

        tapClearAllTasks();
        confirmClearAllTasks();
        actions.waitUntilTextVisible("No tasks yet");
    }

    @Step("Tap text: {0}")
    public void tapText(String text) {
        LOGGER.info("Tapping text '{}'", text);
        actions.byText(text).click();
    }

    @Step("Delete visible task: {0}")
    public void deleteTask(String taskName) {
        LOGGER.info("Deleting visible task '{}'", taskName);
        actions.scrollToText(taskName);
        actions.byText("Delete " + taskName).click();
        actions.waitUntilTextHidden(taskName);
    }
}
