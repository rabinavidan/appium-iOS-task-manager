package com.rabin.taskmanager.api.tests;

import com.rabin.taskmanager.api.client.TaskApiClient;
import com.rabin.taskmanager.api.config.ApiTestConfig;
import com.rabin.taskmanager.api.support.MockApiServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseApiTest {
    protected TaskApiClient tasks;

    private MockApiServer mockApiServer;

    @BeforeAll
    void startMockApi() throws IOException, InterruptedException {
        ApiTestConfig config = new ApiTestConfig();
        mockApiServer = new MockApiServer(config);
        int port = mockApiServer.start();
        tasks = new TaskApiClient(config.baseUri(port));
    }

    @BeforeEach
    void cleanState() throws IOException, InterruptedException {
        tasks.clearTasks();
    }

    @AfterAll
    void stopMockApi() {
        if (mockApiServer != null) {
            mockApiServer.stop();
        }
    }
}

