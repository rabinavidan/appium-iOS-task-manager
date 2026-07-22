package com.rabin.taskmanager.tests;

import com.rabin.taskmanager.hybrid.HybridMockApiServer;
import com.rabin.taskmanager.hybrid.HybridTaskApiClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

abstract class BaseHybridTest extends BaseIosTest {
    protected HybridTaskApiClient api;

    private final HybridMockApiServer mockServer = new HybridMockApiServer();

    // JUnit 5 calls parent @BeforeAll (BaseIosTest.setUp) first, then this one.
    @BeforeAll
    void setUpApi() throws Exception {
        int port = mockServer.start();
        api = new HybridTaskApiClient(mockServer.baseUri());
    }

    // JUnit 5 calls this @AfterAll first (child before parent), then BaseIosTest.tearDown.
    @AfterAll
    void tearDownApi() {
        mockServer.stop();
    }
}
