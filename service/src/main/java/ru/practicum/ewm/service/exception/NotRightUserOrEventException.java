package ru.practicum.ewm.service.exception;

public class NotRightUserOrEventException extends RuntimeException {
    public NotRightUserOrEventException(String message) {
        super(message);
    }
}
