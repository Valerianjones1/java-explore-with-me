package ru.practicum.ewm.service.compilation.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    private List<Long> events;

    private Boolean pinned;

    @Length(min = 1, max = 50)
    private String title;

    private Long compId;
}
