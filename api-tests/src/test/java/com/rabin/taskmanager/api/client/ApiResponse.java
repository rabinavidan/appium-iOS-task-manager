package com.rabin.taskmanager.api.client;

public record ApiResponse<T>(int statusCode, T body) {
}

