package ru.practicum.ewm.service.user.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NewUserRequest {

    @NotNull(message = "Поле 'email' не может быть null")
    @NotBlank(message = "Поле 'email' не может быть пустым")
    @Email(message = "Неверный формат поля 'email'")
    @Length(min = 6, max = 254)
    private String email;

    @NotNull(message = "Поле 'name' не может быть null")
    @NotBlank(message = "Поле 'name' не может быть пустым")
    @Length(min = 2, max = 250)
    private String name;
}
