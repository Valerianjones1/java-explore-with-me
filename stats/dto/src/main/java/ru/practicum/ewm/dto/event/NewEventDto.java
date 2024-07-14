package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.ewm.dto.location.LocationDto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
public class NewEventDto {
    @NotNull(message = "Поле 'annotation' не может быть null")
    @NotBlank(message = "Поле 'annotation' не может быть пустым")
    @Length(min = 20, max = 2000)
    private String annotation;

    @Positive
    private Integer category;

    @NotNull(message = "Поле 'description' не может быть null")
    @NotBlank(message = "Поле 'description' не может быть пустым")
    @Length(min = 20, max = 7000)
    private String description;

    @NotNull(message = "Поле 'eventDate' не может быть равно null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @FutureOrPresent
    private LocalDateTime eventDate;

    @NotNull(message = "Поле 'location' не может быть равно null")
    private LocationDto location;

    private Boolean paid = false;

    @PositiveOrZero
    private Integer participantLimit = 0;

    private Boolean requestModeration = true;

    @NotNull(message = "Поле 'title' не может быть null")
    @NotBlank(message = "Поле 'title' не может быть пустым")
    @Length(min = 3, max = 120)
    private String title;
}
