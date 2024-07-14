package ru.practicum.ewm.service.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.stats.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto create(long userId, NewEventDto newEventDto);

    List<EventShortDto> getByUserId(long userId, Pageable pageable);

    EventFullDto getByUserIdAndEventId(long userId, long eventId);

    EventFullDto updateByUserIdAndEventId(UpdateEventUserRequest requestDto, long userId, long eventId);

    List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest requestDto, long userId, long eventId);

    List<EventShortDto> getEventsBySearch(String text, List<Long> categoryIds, Boolean paid,
                                          EndpointHit endpointHit,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                          Pageable pageable);

    EventFullDto getEventById(long eventId, EndpointHit endpointHit);

    List<EventFullDto> getEventsByParams(List<Long> users, List<String> states, List<Long> categories,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    EventFullDto updateEventById(UpdateEventAdminRequest updateEventAdminRequest, long eventId);
}
