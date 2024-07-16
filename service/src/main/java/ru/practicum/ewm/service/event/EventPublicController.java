package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.Client;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.EventShortDto;
import ru.practicum.ewm.service.exception.DateValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
@Slf4j
public class EventPublicController {
    private final EventService service;

    private final Client statsClient;

    private static final String APP = "ewm-main-service";


    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventShortDto> searchEvents(@RequestParam(required = false) String text,
                                            @RequestParam(required = false, defaultValue = "") List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                            @RequestParam(required = false) String sort,
                                            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(required = false, defaultValue = "10") @Positive int size,
                                            HttpServletRequest httpServletRequest) {
        log.info("Ищем события text={}", text);
        if ((rangeStart != null && rangeEnd != null) &&
                (rangeStart.isAfter(rangeEnd) || rangeEnd.isBefore(rangeStart))) {
            throw new DateValidationException("Ошибка с валидацией данных даты и времени");
        }
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(getSort(sort)).ascending());

        List<EventShortDto> events = service.getEventsBySearch(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, pageable);

        statsClient.sendHit(getEndpointHit(httpServletRequest));

        return events;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable long eventId,
                                     HttpServletRequest httpServletRequest) {
        log.info("Получаем событие с идентификатором {}", eventId);

        EventFullDto eventFullDto = service.getEventById(eventId);

        statsClient.sendHit(getEndpointHit(httpServletRequest));

        eventFullDto.setViews(getViews(httpServletRequest.getRequestURI()));

        return eventFullDto;
    }

    private EndpointHit getEndpointHit(HttpServletRequest httpServletRequest) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setUri(httpServletRequest.getRequestURI());
        endpointHit.setIp(httpServletRequest.getRemoteAddr());
        endpointHit.setTimestamp(LocalDateTime.now());
        endpointHit.setApp(APP);
        return endpointHit;
    }

    private int getViews(String uri) {
        List<ViewStats> viewStats = statsClient.sendStats(LocalDateTime.of(2000, 1, 1, 1, 1),
                LocalDateTime.now().plusHours(1),
                List.of(uri), true);
        return !viewStats.isEmpty() ? viewStats.get(0).getHits().intValue() : 0;
    }

    private String getSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "id";
        }
        return sort;
    }
}
