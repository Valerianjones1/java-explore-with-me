package ru.practicum.ewm.server.error;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String error;
    private final String description;
    private String stacktrace;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }

    public ErrorResponse(String error, String description, String stacktrace) {
        this.error = error;
        this.description = description;
        this.stacktrace = stacktrace;
    }
}