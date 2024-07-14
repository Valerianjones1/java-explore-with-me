package ru.practicum.ewm.dto.category;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NewCategoryDto {
    @NotNull(message = "Поле 'name' не может быть null")
    @NotBlank(message = "Поле 'name' не может быть пустым")
    @Length(min = 1, max = 50)
    private String name;
}
