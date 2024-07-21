package ru.practicum.ewm.service.comment.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class NewCommentDto {
    @NotNull(message = "Поле 'content' не может быть null")
    @NotBlank(message = "Поле 'content' не может быть пустым")
    @Length(min = 1, max = 500)
    private String content;
}
