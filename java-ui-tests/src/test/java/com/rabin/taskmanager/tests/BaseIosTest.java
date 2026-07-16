package com.rabin.taskmanager.tests;

import com.rabin.taskmanager.config.TestConfig;
import com.rabin.taskmanager.core.AppiumActions;
import com.rabin.taskmanager.driver.IosDriverFactory;
import com.rabin.taskmanager.flows.TaskManagerFlow;
import com.rabin.taskmanager.reporting.AllureArtifacts;
import com.rabin.taskmanager.reporting.AllureFailureExtension;
import com.rabin.taskmanager.screens.TasksScreen;
import io.appium.java_client.ios.IOSDriver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(AllureFailureExtension.class)
abstract class BaseIosTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseIosTest.class);

    protected TestConfig config;
    protected IOSDriver driver;
    protected AppiumActions actions;
    protected TasksScreen tasks;
    protected TaskManagerFlow taskManager;

    @BeforeAll
    void setUp() throws MalformedURLException {
        config = new TestConfig();
        LOGGER.info("Creating iOS driver for bundle {}", config.bundleId());
        driver = new IosDriverFactory(config).create();
        AllureArtifacts.setDriver(driver);
        actions = new AppiumActions(driver, config.waitTimeout());
        tasks = new TasksScreen(actions);
        taskManager = new TaskManagerFlow(config, actions, tasks);
        taskManager.clearAllTasksIfPossible();
    }

    @BeforeEach
    void cleanStateBeforeTest() {
        taskManager.clearAllTasksIfPossible();
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            if (taskManager != null) {
                taskManager.clearAllTasksIfPossible();
            }

            LOGGER.info("Closing iOS driver");
            driver.quit();
        }
        AllureArtifacts.clearDriver();
    }

    protected String uniqueTaskName(String prefix) {
        return prefix + " task " + System.currentTimeMillis();
    }
}
