package ru.practicum.ewm.service.category.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.ReadOnlyProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CategoryDto {
    @ReadOnlyProperty
    private Integer id;

    @NotNull(message = "Поле 'name' не может быть null")
    @NotBlank(message = "Поле 'name' не может быть пустым")
    @Length(min = 1, max = 50)
    private String name;
}