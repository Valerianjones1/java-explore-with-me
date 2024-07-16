package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.event.dto.EventFullDto;
import ru.practicum.ewm.service.event.dto.UpdateEventAdminRequest;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {
    private final EventService service;

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<String> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(required = false, defaultValue = "10") @Positive int size) {
        log.info("Получаем события с users={}, states={}, categories={}, с {} по {}", users, states, categories, rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return service.getEventsByParams(users, states, categories, rangeStart, rangeEnd, pageable);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto updateEvent(@RequestBody @Valid UpdateEventAdminRequest updateEventAdminRequest,
                                    @PathVariable long eventId) {
        log.info("Обновление событие {} с идентификатором {} ", eventId, updateEventAdminRequest);
        return service.updateEventById(updateEventAdminRequest, eventId);
    }
}
