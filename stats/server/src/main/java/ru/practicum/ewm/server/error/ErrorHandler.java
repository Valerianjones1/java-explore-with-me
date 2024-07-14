package ru.practicum.ewm.server.error;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.server.exception.DateValidationException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValid(final MethodArgumentNotValidException e) {
        log.error("Ошибка с валидацией", e);
        return new ApiError(e.getMessage(), "Ошибка с валидацией",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleRequestParam(final MissingServletRequestParameterException e) {
        log.error("Ошибка с валидацией параметров запроса", e);
        return new ApiError(e.getMessage(), "Ошибка с валидацией параметров запроса",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateValidation(final DateValidationException e) {
        log.error("Ошибка с валидацией даты и времени", e);
        return new ApiError(e.getMessage(), "Ошибка с валидацией даты и времени",
                HttpStatus.BAD_REQUEST.getReasonPhrase(), LocalDateTime.now());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Exception e) {
        log.error("Произошла непредвиденная ошибка", e);
        return new ApiError(e.getMessage(), "Произошла непредвиденная ошибка",
                ExceptionUtils.getStackFrames(e), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), LocalDateTime.now());
    }
}
