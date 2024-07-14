package ru.practicum.ewm.service.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private String status;
    private final String message;
    private final String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private String[] errors;

    public ApiError(String reason, String message, String[] errors, String status,
                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp) {
        this.reason = reason;
        this.message = message;
        this.errors = errors;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ApiError(String reason, String message, String status,
                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp) {
        this.reason = reason;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
    }
}