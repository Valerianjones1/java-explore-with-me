package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.EventRepository;
import ru.practicum.ewm.service.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.service.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.service.event.dto.EventState;
import ru.practicum.ewm.service.event.mapper.EventMapper;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.request.dto.EventRequestStatus;
import ru.practicum.ewm.service.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.request.dto.RequestStatus;
import ru.practicum.ewm.service.request.mapper.RequestMapper;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto create(long userId, long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Ивент с идентификатором %s не найден", eventId)));

        if (event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataIntegrityViolationException("Нельзя участвовать в неопубликованном событии");
        }

        int participantsCount = requestRepository.findAllByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED).size();

        int participantLimit = event.getParticipantLimit();
        if (participantLimit == participantsCount && participantLimit != 0) {
            throw new DataIntegrityViolationException("Достигнут лимит запросов на участие");
        }

        RequestStatus status = RequestStatus.PENDING;

        if (!event.getRequestModeration() || participantLimit == 0) {
            status = RequestStatus.CONFIRMED;
        }

        Request savedRequest = requestRepository.save(RequestMapper.mapToRequest(user, event, status));
        return RequestMapper.mapToParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getAll(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));

        return requestRepository.findAllByRequesterId(user.getId())
                .stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancel(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Запрос на участие с идентификатором %s не найден", requestId)));

        if (!request.getRequester().getId().equals(userId)) {
            throw new DataIntegrityViolationException(String.format("Пользователь с идентификатором %s не является создателем заявки", userId));
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest requestDto,
                                                               long userId,
                                                               long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        if (event.getInitiator().getId() != userId) {
            throw new DataIntegrityViolationException("Подтверждать или отменять заявки может только инициатор события");
        }

        int participantsCount = requestRepository.findAllByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED).size();
        int participantLimit = event.getParticipantLimit();

        if (participantLimit == participantsCount) {
            throw new DataIntegrityViolationException("Достигнут лимит запросов на участие");
        }

        List<Request> requests = requestRepository.findAllByEventInitiatorIdAndEventIdAndIdInAndStatusEquals(userId,
                eventId, requestDto.getRequestIds(), RequestStatus.PENDING);


        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (Request request : requests) {
            if (participantsCount == participantLimit ||
                    requestDto.getStatus().equals(EventRequestStatus.REJECTED)) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(RequestMapper.mapToParticipationRequestDto(request));
            } else if (requestDto.getStatus().equals(EventRequestStatus.CONFIRMED)) {
                request.setStatus(RequestStatus.CONFIRMED);
                participantsCount += 1;
                confirmedRequests.add(RequestMapper.mapToParticipationRequestDto(request));
            }
        }


        requestRepository.saveAll(requests);

        return EventMapper.mapToEventStatusResult(confirmedRequests, rejectedRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId) {
        return requestRepository.findAllByEventInitiatorIdAndEventId(userId, eventId)
                .stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
