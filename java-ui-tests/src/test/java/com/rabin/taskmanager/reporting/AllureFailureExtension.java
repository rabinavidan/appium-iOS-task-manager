package com.rabin.taskmanager.reporting;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

public final class AllureFailureExtension implements TestExecutionExceptionHandler, LifecycleMethodExecutionExceptionHandler {
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        AllureArtifacts.attachFailureArtifacts(throwable);
        throw throwable;
    }

    @Override
    public void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        AllureArtifacts.attachFailureArtifacts(throwable);
        throw throwable;
    }

    @Override
    public void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        AllureArtifacts.attachFailureArtifacts(throwable);
        throw throwable;
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        AllureArtifacts.attachFailureArtifacts(throwable);
        throw throwable;
    }

    @Override
    public void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        AllureArtifacts.attachFailureArtifacts(throwable);
        throw throwable;
    }
}
