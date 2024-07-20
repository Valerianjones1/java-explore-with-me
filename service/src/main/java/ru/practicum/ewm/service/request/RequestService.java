package ru.practicum.ewm.service.request;

import ru.practicum.ewm.service.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.service.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.service.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(long userId, long eventId);

    List<ParticipationRequestDto> getAll(long userId);

    ParticipationRequestDto cancel(long userId, long requestId);

    List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest requestDto, long userId, long eventId);

}
