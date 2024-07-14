package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.event.EventState;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.RequestStatus;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.EventRepository;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.request.mapper.RequestMapper;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.UserRepository;

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
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        Request savedRequest = requestRepository.save(RequestMapper.mapToRequest(user, event, status));
        return RequestMapper.mapToParticipationRequestDto(savedRequest);
    }

    @Override
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
    public ParticipationRequestDto cancel(long userId, long requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Запрос на участие с идентификатором %s не найден", requestId)));

        if (!request.getRequester().getId().equals(userId)) {
            throw new DataIntegrityViolationException(String.format("Пользователь с идентификатором %s не является создателем заявки", userId));
        }
        Event event = request.getEvent();
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.mapToParticipationRequestDto(requestRepository.save(request));
    }
}
