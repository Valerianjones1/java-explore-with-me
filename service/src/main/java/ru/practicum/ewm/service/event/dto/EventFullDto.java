package ru.practicum.ewm.service.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.service.category.dto.CategoryDto;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.location.dto.LocationDto;
import ru.practicum.ewm.service.user.dto.UserShortDto;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
public class EventFullDto {
    @Positive
    private Integer id;

    @NotNull(message = "Поле 'annotation' не может быть null")
    @NotBlank(message = "Поле 'annotation' не может быть пустым")
    @Length(min = 20, max = 2000)
    private String annotation;

    @NotNull(message = "Поле 'category' не может быть null")
    private CategoryDto category;

    @PositiveOrZero
    private Integer confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @NotNull(message = "Поле 'description' не может быть null")
    @NotBlank(message = "Поле 'description' не может быть пустым")
    @Length(min = 20, max = 7000)
    private String description;

    @NotNull(message = "Поле 'eventDate' не может быть равно null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Поле 'initiator' не может быть равно null")
    private UserShortDto initiator;

    @NotNull(message = "Поле 'location' не может быть равно null")
    private LocationDto location;

    @NotNull(message = "Поле 'paid' не может быть равно null")
    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit = 0;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private Boolean requestModeration = true;

    @Enumerated(EnumType.STRING)
    private EventState state;


    @NotNull(message = "Поле 'title' не может быть null")
    @NotBlank(message = "Поле 'title' не может быть пустым")
    @Length(min = 3, max = 120)
    private String title;

    @PositiveOrZero
    private Integer views;

    private List<CommentDto> comments = Collections.emptyList();
}
