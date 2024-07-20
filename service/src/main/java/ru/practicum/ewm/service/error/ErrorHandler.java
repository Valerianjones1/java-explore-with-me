package ru.practicum.ewm.service.error;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.service.exception.DateValidationException;
import ru.practicum.ewm.service.exception.NotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParam(final MissingServletRequestParameterException e) {
        log.error("Ошибка с параметрами запроса", e);
        return new ApiError(e.getMessage(), "Ошибка с параметрами запроса",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateParams(final DateValidationException e) {
        log.error("Ошибка с параметрами даты и времени", e);
        return new ApiError(e.getMessage(), "Ошибка с параметрами даты и времени",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValid(final MethodArgumentNotValidException e) {
        log.error("Ошибка с валидацией", e);
        return new ApiError(e.getMessage(), "Ошибка с валидацией",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        log.info("Не найдено");
        return new ApiError(e.getMessage(), "Не найдено",
                HttpStatus.NOT_FOUND.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrity(final DataIntegrityViolationException e) {
        log.info("Нарушение целостности данных");
        return new ApiError(e.getMessage(), "Нарушение целостности данных",
                HttpStatus.CONFLICT.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Exception e) {
        log.error("Произошла непредвиденная ошибка", e);
        return new ApiError(e.getMessage(), "Произошла непредвиденная ошибка",
                ExceptionUtils.getStackFrames(e), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), LocalDateTime.now());
    }
}
