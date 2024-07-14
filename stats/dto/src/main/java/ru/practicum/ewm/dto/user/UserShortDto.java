package ru.practicum.ewm.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserShortDto {
    private Integer id;

    @NotNull(message = "Поле 'name' не может быть null")
    @NotBlank(message = "Поле 'name' не может быть пустым")
    private String name;
}
