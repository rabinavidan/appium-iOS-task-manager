package com.rabin.taskmanager.api.models;

public record TaskResponse(
        String id,
        String title,
        String notes,
        String priority,
        String dueDate,
        boolean isCompleted,
        String createdAt
) {
}

