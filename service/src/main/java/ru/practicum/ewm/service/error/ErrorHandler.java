package ru.practicum.ewm.service.error;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.service.exception.EventNotPublishedException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.exception.ValidationException;

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

    public ApiError handleMissingPathVar(final MethodArgumentTypeMismatchException e) {
        log.error("Ошибка с параметрами", e);
        return new ApiError(e.getMessage(), "Ошибка с параметрами",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestBody(final HttpMessageNotReadableException e) {
        log.error("Ошибка с телом запроса", e);
        return new ApiError(e.getMessage(), "Ошибка с телом запроса",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidParams(final ValidationException e) {
        log.error("Ошибка с параметрами", e);
        return new ApiError(e.getMessage(), "Ошибка с параметрами",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEventNotPublished(final EventNotPublishedException e) {
        log.error("Ошибка с событием, оно должно быть опубликовано", e);
        return new ApiError(e.getMessage(), "Ошибка с событием, оно должно быть опубликовано",
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
