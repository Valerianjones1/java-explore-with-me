package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.Client;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.dto.request.EventRequestStatus;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.RequestStatus;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.service.category.Category;
import ru.practicum.ewm.service.category.CategoryRepository;
import ru.practicum.ewm.service.event.mapper.EventMapper;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.location.Location;
import ru.practicum.ewm.service.location.LocationRepository;
import ru.practicum.ewm.service.location.mapper.LocationMapper;
import ru.practicum.ewm.service.request.Request;
import ru.practicum.ewm.service.request.RequestRepository;
import ru.practicum.ewm.service.request.mapper.RequestMapper;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final LocationRepository locationRepository;

    private final Client statsClient;

    @Override
    public EventFullDto create(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataIntegrityViolationException("Дата и время на которые намечено событие не может быть раньше" +
                    ", чем через два часа от текущего момента");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));


        Integer categoryId = newEventDto.getCategory();

        Location location = getLocation(newEventDto.getLocation());

        Category category = categoryId != null ? categoryRepository.findById((long) categoryId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", categoryId)))
                : null;
        Event event = EventMapper.mapToEvent(newEventDto, user, location, category);

        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getByUserId(long userId, Pageable pageable) {
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getByUserIdAndEventId(long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        return EventMapper.mapToEventFullDto(event);
    }

    @Override
    public EventFullDto updateByUserIdAndEventId(UpdateEventUserRequest eventRequestDto, long userId, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new DataIntegrityViolationException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }

        Event updatedEvent = eventRepository.save(fillEvent(event, eventRequestDto));
        return EventMapper.mapToEventFullDto(updatedEvent);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId) {
        return requestRepository.findAllByEventInitiatorIdAndEventId(userId, eventId)
                .stream()
                .map(RequestMapper::mapToParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
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
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                participantsCount += 1;
                confirmedRequests.add(RequestMapper.mapToParticipationRequestDto(request));
            }
        }


        requestRepository.saveAll(requests);

        return EventMapper.mapToEventStatusResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public List<EventShortDto> getEventsBySearch(String text, List<Long> categoryIds,
                                                 Boolean paid, EndpointHit endpointHit,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Boolean onlyAvailable, Pageable pageable) {

        if (text != null && text.isBlank()) {
            return Collections.emptyList();
        }

        statsClient.sendHit(endpointHit);

        if (onlyAvailable != null && onlyAvailable) {
            return eventRepository.findAllBySearchParamsAvailable(text != null ? text.toLowerCase() : null,
                            categoryIds, paid, rangeStart, rangeEnd, pageable)
                    .stream()
                    .map(EventMapper::mapToEventShortDto)
                    .collect(Collectors.toList());
        }


        return eventRepository.findAllBySearchParams(text != null ? text.toLowerCase() : null, categoryIds, paid, rangeStart, rangeEnd, pageable)
                .stream()
                .map(EventMapper::mapToEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventById(long eventId, EndpointHit endpointHit) {
        Event event = eventRepository.findByIdAndStateEquals(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        statsClient.sendHit(endpointHit);
        List<ViewStats> viewStats = statsClient.sendStats(LocalDateTime.of(2000, 1, 1, 1, 1),
                LocalDateTime.now(),
                List.of(endpointHit.getUri()), true);

        event.setViews(!viewStats.isEmpty() ? viewStats.get(0).getHits().intValue() : 0);

        eventRepository.save(event);
        return EventMapper.mapToEventFullDto(event);
    }


    @Override
    public List<EventFullDto> getEventsByParams(List<Long> users,
                                                List<String> states,
                                                List<Long> categories,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                Pageable pageable) {
        List<EventState> eventStates = states != null ? states
                .stream()
                .map(EventState::valueOf)
                .collect(Collectors.toList()) : null;

        return eventRepository.findAllByCategoryIdsAndUserIds(categories, users, eventStates, rangeStart, rangeEnd, pageable)
                .stream()
                .map(EventMapper::mapToEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto updateEventById(UpdateEventAdminRequest updateEventAdminRequest, long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        if ((!event.getState().equals(EventState.PENDING) && updateEventAdminRequest.getStateAction().equals("PUBLISH_EVENT"))
                || ((!event.getState().equals(EventState.PENDING) && updateEventAdminRequest.getStateAction().equals("REJECT_EVENT")))) {
            throw new DataIntegrityViolationException("Только события со статусом PENDING можно опубликовать или отклонить");
        }

        Event updatedEvent = eventRepository.save(fillEvent(event, updateEventAdminRequest));
        return EventMapper.mapToEventFullDto(updatedEvent);
    }

    private Location getLocation(LocationDto locationDto) {
        return locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon())
                .orElse(locationRepository.save(LocationMapper.mapToLocation(locationDto)));
    }

    private Event fillEvent(Event event, UpdateEventUserRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DataIntegrityViolationException("Дата и время на которые намечено событие не может быть раньше" +
                        ", чем через два часа от текущего момента");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory().longValue())
                    .orElse(event.getCategory()));
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }

        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(LocationMapper.mapToLocation(updateEventUserRequest.getLocation()));
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(EventStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            } else {
                event.setState(EventState.PENDING);
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        return event;
    }

    private Event fillEvent(Event event, UpdateEventAdminRequest updateEventUserRequest) {
        if (updateEventUserRequest.getEventDate() != null) {
            event.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory().longValue())
                    .orElse(event.getCategory()));
        }

        if (updateEventUserRequest.getPaid() != null) {
            event.setPaid(updateEventUserRequest.getPaid());
        }

        if (updateEventUserRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }


        if (updateEventUserRequest.getDescription() != null) {
            event.setDescription(updateEventUserRequest.getDescription());
        }

        if (updateEventUserRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventUserRequest.getAnnotation());
        }

        if (updateEventUserRequest.getLocation() != null) {
            event.setLocation(getLocation(updateEventUserRequest.getLocation()));
        }

        if (updateEventUserRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals("PUBLISH_EVENT")) {
                event.setState(EventState.PUBLISHED);
            } else if (updateEventUserRequest.getStateAction().equals("REJECT_EVENT")) {
                event.setState(EventState.CANCELED);
            } else {
                event.setState(EventState.PENDING);
            }
        }

        if (updateEventUserRequest.getTitle() != null) {
            event.setTitle(updateEventUserRequest.getTitle());
        }

        return event;
    }
}
