package ru.practicum.ewm.service.compilation.dto;

import lombok.Data;
import ru.practicum.ewm.service.event.dto.EventShortDto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Data
public class CompilationDto {
    @NotNull(message = "Поле 'id' не может быть null")
    @PositiveOrZero
    private Long id;

    private List<EventShortDto> events;

    @NotNull(message = "Поле 'pinned' не может быть null")
    private Boolean pinned;

    private String title;

}
