package ru.practicum.ewm.dto.user;

import lombok.Data;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserDto {
    @ReadOnlyProperty
    private Integer id;

    @NotNull(message = "Поле 'email' не может быть null")
    @NotBlank(message = "Поле 'email' не может быть пустым")
    private String email;

    @NotNull(message = "Поле 'name' не может быть null")
    @NotBlank(message = "Поле 'name' не может быть пустым")
    private String name;
}
