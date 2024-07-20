package ru.practicum.ewm.server.exception;

public class DateValidationException extends RuntimeException {
    public DateValidationException(String message) {
        super(message);
    }
}