package ru.practicum.ewm.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewStats {
    private String app;

    private String uri;

    private Long hits = 0L;
}
