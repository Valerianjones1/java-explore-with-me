package ru.practicum.ewm.service.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.service.event.dto.EventShortDto;
import ru.practicum.ewm.service.user.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    @ReadOnlyProperty
    private Long id;

    @NotNull(message = "Поле 'content' не может быть null")
    @NotBlank(message = "Поле 'content' не может быть пустым")
    @Length(min = 1, max = 500)
    private String content;

    @NotNull(message = "Поле 'user' не может быть null")
    private UserShortDto user;

    @NotNull(message = "Поле 'event' не может быть null")
    private EventShortDto event;

    @NotNull(message = "Поле 'publishedOn' не может быть null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;
}
