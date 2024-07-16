package ru.practicum.ewm.service.compilation.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class NewCompilationDto {
    private List<Long> events;

    private Boolean pinned = false;

    @NotNull(message = "Поле 'title' не может быть null")
    @NotBlank(message = "Поле 'title' не может быть пустым")
    @Length(min = 1, max = 50)
    private String title;
}
