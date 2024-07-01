package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class EndpointHit {
    @NotNull(message = "Приложение не может быть равно null")
    @NotBlank(message = "Приложение не может быть пустым")
    private String app;

    @NotNull(message = "URI не может быть равно null")
    @NotBlank(message = "URI не может быть пустым")
    private String uri;

    @NotNull(message = "IP не может быть равно null")
    @NotBlank(message = "IP не может быть равно пустым")
    private String ip;

    @NotNull(message = "Timestamp не может быть равно null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}
