package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class RequestPrivateController {
    private final RequestService service;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable long userId,
                                                 @RequestParam long eventId) {
        log.info("Добавление запроса от текущего пользователя {} на участие в событии {}", userId, eventId);
        return service.create(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    @ResponseStatus(code = HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@PathVariable long userId) {
        log.info("Получение информации о заявках текущего пользователя {} на участие в чужих событиях", userId);
        return service.getAll(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(code = HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable long userId,
                                                 @PathVariable long requestId) {
        log.info("Отмена своего запроса {} на участие в событии", userId);
        return service.cancel(userId, requestId);
    }

}
