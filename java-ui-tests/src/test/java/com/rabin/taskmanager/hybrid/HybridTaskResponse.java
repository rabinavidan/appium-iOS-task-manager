package com.rabin.taskmanager.hybrid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HybridTaskResponse(
        String id,
        String title,
        String notes,
        String priority,
        boolean isCompleted
) {
}
