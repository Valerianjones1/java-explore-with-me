package ru.practicum.ewm.server.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.server.exception.DateValidationException;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {
    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void createHit(@Valid @RequestBody EndpointHit endpointHit) {
        log.info("Создается информация, что к эндпоинту {} был запрос", endpointHit);
        service.createHit(endpointHit);
    }

    @GetMapping("/stats")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ViewStats> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                    @RequestParam(required = false, defaultValue = "") List<String> uris,
                                    @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("Получаем статистику по посещениям с {} по {} по uris {}", start, end, uris);
        validateDate(start, end);
        return service.getAllStats(start, end, uris, unique);
    }

    private void validateDate(LocalDateTime start, LocalDateTime end) {
        if ((start != null && end != null) &&
                (start.isAfter(end) || end.isBefore(start))) {
            throw new DateValidationException("Ошибка с валидацией данных даты и времени");
        }
    }
}
