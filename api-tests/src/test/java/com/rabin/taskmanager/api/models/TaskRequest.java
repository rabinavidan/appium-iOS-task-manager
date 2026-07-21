package com.rabin.taskmanager.api.models;

public record TaskRequest(
        String title,
        String notes,
        String priority,
        String dueDate,
        Boolean isCompleted
) {
    public static TaskRequest create(String title, String notes, String priority) {
        return new TaskRequest(title, notes, priority, null, null);
    }
}

