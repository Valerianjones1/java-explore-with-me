package ru.practicum.ewm.service.request.mapper;

import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.request.Request;
import ru.practicum.ewm.service.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.request.dto.RequestStatus;
import ru.practicum.ewm.service.user.User;

import java.time.LocalDateTime;

public class RequestMapper {
    public static ParticipationRequestDto mapToParticipationRequestDto(Request request) {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(request.getId().intValue());
        participationRequestDto.setRequesterId(request.getRequester().getId().intValue());

        participationRequestDto.setStatus(request.getStatus().toString());

        participationRequestDto.setCreated(request.getCreated());
        participationRequestDto.setEventId(request.getEvent().getId().intValue());

        return participationRequestDto;
    }

    public static Request mapToRequest(User user, Event event, RequestStatus status) {
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);
        request.setStatus(status);

        return request;
    }
}
