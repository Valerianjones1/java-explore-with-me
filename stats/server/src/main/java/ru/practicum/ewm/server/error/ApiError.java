package ru.practicum.ewm.server.error;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private String status;
    private final String message;
    private final String reason;
    private LocalDateTime timestamp;
    private String[] errors;

    public ApiError(String reason, String message, String[] errors, String status, LocalDateTime timestamp) {
        this.reason = reason;
        this.message = message;
        this.errors = errors;
        this.status = status;
        this.timestamp = timestamp;
    }
}