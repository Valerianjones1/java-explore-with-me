package ru.practicum.ewm.service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.service.category.dto.CategoryDto;
import ru.practicum.ewm.service.user.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Data
public class EventShortDto {
    @Positive
    private Integer id;

    @NotNull(message = "Поле 'annotation' не может быть null")
    @NotBlank(message = "Поле 'annotation' не может быть пустым")
    @Length(min = 20, max = 2000)
    private String annotation;

    @NotNull(message = "Поле 'category' не может быть null")
    private CategoryDto category;

    @PositiveOrZero
    private Integer confirmedRequests = 0;

    @NotNull(message = "Поле 'eventDate' не может быть равно null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Поле 'initiator' не может быть равно null")
    private UserShortDto initiator;

    @NotNull(message = "Поле 'paid' не может быть равно null")
    private Boolean paid;

    @NotNull(message = "Поле 'title' не может быть null")
    @NotBlank(message = "Поле 'title' не может быть пустым")
    @Length(min = 3, max = 120)
    private String title;

    @PositiveOrZero
    private Integer views = 0;
}
