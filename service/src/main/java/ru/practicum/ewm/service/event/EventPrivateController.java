package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {
    private final EventService service;

    @PostMapping("/{userId}/events")
    @ResponseStatus(code = HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Создаем событие {}", newEventDto);
        return service.create(userId, newEventDto);
    }

    @GetMapping("/{userId}/events")
    @ResponseStatus(code = HttpStatus.OK)
    public List<EventShortDto> getEvents(@PathVariable long userId,
                                         @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                                         @Positive @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        log.info("Получаем события пользователя {} from={}, size={}", userId, from, size);
        return service.getByUserId(userId, pageable);
    }

    @GetMapping("/{userId}/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto getEvent(@PathVariable long userId,
                                 @PathVariable long eventId) {
        log.info("Получаем событие {} пользователя {}", eventId, userId);
        return service.getByUserIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public EventFullDto updateEvent(@Valid @RequestBody UpdateEventUserRequest updateEventDto,
                                    @PathVariable long userId,
                                    @PathVariable long eventId) {
        log.info("Обновляем событие {} пользователя {} {}", eventId, userId, updateEventDto);
        return service.updateByUserIdAndEventId(updateEventDto, userId, eventId);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsByEvent(@PathVariable long userId,
                                                            @PathVariable long eventId) {
        log.info("Получаем информацию о запросах на участие в событии {} текущего пользователя {} ", eventId, userId);
        return service.getRequestsByUserIdAndEventId(userId, eventId);
    }


    @PatchMapping("/{userId}/events/{eventId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(@RequestBody @Valid EventRequestStatusUpdateRequest updateRequestDto,
                                                              @PathVariable long userId,
                                                              @PathVariable long eventId) {
        log.info("Измененяем статусы (подтверждена, отменена) заявок на участие в событии {} текущего пользователя {}", eventId, userId);
        return service.updateRequestsStatus(updateRequestDto, userId, eventId);
    }
}
