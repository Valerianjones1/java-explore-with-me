package ru.practicum.ewm.server.error;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValid(final MethodArgumentNotValidException e) {
        log.error("Ошибка с валидацией", e);
        return new ApiError(e.getMessage(), "Ошибка с валидацией",
                ExceptionUtils.getStackFrames(e), HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(final DataIntegrityViolationException e) {
        log.info("Нарушение целостности данных");
        return new ApiError(e.getMessage(), "Нарушение целостности данных",
                ExceptionUtils.getStackFrames(e), HttpStatus.CONFLICT.toString(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Exception e) {
        log.error("Произошла непредвиденная ошибка", e);
        return new ApiError(e.getMessage(), "Произошла непредвиденная ошибка",
                ExceptionUtils.getStackFrames(e), HttpStatus.INTERNAL_SERVER_ERROR.toString(), LocalDateTime.now());
    }
}
