package ru.practicum.ewm.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.Client;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.service.category.Category;
import ru.practicum.ewm.service.category.CategoryRepository;
import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.event.mapper.EventMapper;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.location.Location;
import ru.practicum.ewm.service.location.LocationRepository;
import ru.practicum.ewm.service.location.dto.LocationDto;
import ru.practicum.ewm.service.location.mapper.LocationMapper;
import ru.practicum.ewm.service.request.Request;
import ru.practicum.ewm.service.request.RequestRepository;
import ru.practicum.ewm.service.request.dto.RequestStatus;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;

    private final Client statsClient;

    private static final LocalDateTime START_DATE = LocalDateTime.of(2000, 1, 1, 1, 1);

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<EventShortDto> getByUserId(long userId, Pageable pageable) {
        List<Request> confirmedRequests = requestRepository.findAllByRequesterIdAndStatusEquals(userId, RequestStatus.CONFIRMED);

        Map<Long, Long> eventRequests = confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        List<String> eventIds = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        Map<Long, ViewStats> viewStats = getStats(eventIds);

        return events.stream()
                .map(event -> {
                    EventShortDto eventShortDto = EventMapper.mapToEventShortDto(event);
                    eventShortDto.setConfirmedRequests(eventRequests.get(event.getId()) != null
                            ? eventRequests.get(event.getId()).intValue() : 0);
                    eventShortDto.setViews(!viewStats.isEmpty() ? viewStats.get(event.getId()).getHits().intValue() : 0);


                    return eventShortDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByUserIdAndEventId(long userId, long eventId) {
        List<Request> confirmedRequests = requestRepository.findAllByEventIdAndRequesterIdAndStatusEquals(eventId,
                userId, RequestStatus.CONFIRMED);

        Map<Long, Long> eventRequests = confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        Map<Long, ViewStats> viewStats = getStats(List.of(String.format("/events/%d", eventId)));

        int requestsCount = eventRequests.get(event.getId()) != null
                ? eventRequests.get(event.getId()).intValue() : 0;

        event.setViews(!viewStats.isEmpty() ? viewStats.get(eventId).getHits().intValue() : 0);
        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(event);
        eventFullDto.setConfirmedRequests(requestsCount);

        return eventFullDto;
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsBySearch(String text, List<Long> categoryIds,
                                                 Boolean paid,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Boolean onlyAvailable, Pageable pageable) {
        if (text != null && text.isBlank()) {
            return Collections.emptyList();
        }

        List<Request> confirmedRequests = requestRepository.findAllByStatusEquals(RequestStatus.CONFIRMED);

        Map<Long, Long> eventRequests = confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        List<Event> events = eventRepository.findAllBySearchParams(text != null ? text.toLowerCase() : null,
                paid, rangeStart, rangeEnd, categoryIds, pageable);

        Map<Long, ViewStats> viewStats = getStats(events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList()));

        boolean isAvailable = onlyAvailable != null && onlyAvailable;

        return !isAvailable ? getEventsByParams(events, viewStats, eventRequests)
                : getAvailableEventsByParams(events, viewStats, eventRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(long eventId) {
        Event foundEvent = eventRepository.findByIdAndStateEquals(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));


        List<Request> confirmedRequests = requestRepository.findAllByEventIdAndStatusEquals(eventId, RequestStatus.CONFIRMED);

        Map<Long, Long> eventRequests = confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        Map<Long, ViewStats> viewStats = getStats(List.of(String.format("/events/%d", eventId)));

        int requestsCount = eventRequests.get(eventId) != null
                ? eventRequests.get(eventId).intValue() : 0;


        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(foundEvent);
        eventFullDto.setConfirmedRequests(requestsCount);
        eventFullDto.setViews(viewStats.get(eventId) != null ? viewStats.get(eventId).getHits().intValue() : 0);

        return eventFullDto;
    }


    @Override
    @Transactional(readOnly = true)
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

        List<Event> events = eventRepository.findAllByCategoryIdsAndUserIds(categories, users, eventStates, rangeStart, rangeEnd, pageable);

        List<Request> confirmedRequests = requestRepository.findAllByStatusEquals(RequestStatus.CONFIRMED);

        Map<Long, Long> eventRequests = confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        Map<Long, ViewStats> viewStats = getStats(events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList()));

        return events
                .stream()
                .map(event -> {
                    int requestsCount = eventRequests.get(event.getId()) != null
                            ? eventRequests.get(event.getId()).intValue() : 0;

                    int views = viewStats.get(event.getId()) != null
                            ? viewStats.get(event.getId()).getHits().intValue() : 0;

                    EventFullDto eventFullDto = EventMapper.mapToEventFullDto(event);

                    eventFullDto.setConfirmedRequests(requestsCount);
                    eventFullDto.setViews(views);

                    return eventFullDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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

    private Map<Long, ViewStats> getStats(List<String> eventIds) {
        return statsClient.sendStats(START_DATE, LocalDateTime.now(), eventIds, true)
                .stream()
                .collect(Collectors.toMap(stats -> getEventIdFromUri(stats.getUri()), Function.identity()));
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

    private Long getEventIdFromUri(String uri) {
        if (uri.contains("?")) {
            return null;
        }
        return Long.parseLong(uri.split("/events/")[1]);
    }

    private List<EventShortDto> getAvailableEventsByParams(List<Event> eventsAvailable, Map<Long, ViewStats> viewStats,
                                                           Map<Long, Long> eventRequests) {
        return eventsAvailable
                .stream()
                .map(event -> {
                    EventShortDto eventShortDto = EventMapper.mapToEventShortDto(event);
                    int requestsCount = eventRequests.get(event.getId()) != null
                            ? eventRequests.get(event.getId()).intValue() : 0;

                    int views = viewStats.get(event.getId()) != null
                            ? viewStats.get(event.getId()).getHits().intValue() : 0;

                    if (event.getParticipantLimit() != 0
                            && requestsCount <= event.getParticipantLimit()) {
                        eventShortDto.setConfirmedRequests(requestsCount);
                        eventShortDto.setViews(views);

                        return eventShortDto;
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    private List<EventShortDto> getEventsByParams(List<Event> events, Map<Long, ViewStats> viewStats,
                                                  Map<Long, Long> eventRequests) {
        return events
                .stream()
                .map(event -> {
                    EventShortDto eventShortDto = EventMapper.mapToEventShortDto(event);
                    int requestsCount = eventRequests.get(event.getId()) != null
                            ? eventRequests.get(event.getId()).intValue() : 0;

                    int views = viewStats.get(event.getId()) != null
                            ? viewStats.get(event.getId()).getHits().intValue() : 0;

                    eventShortDto.setConfirmedRequests(requestsCount);
                    eventShortDto.setViews(views);

                    return eventShortDto;
                })
                .collect(Collectors.toList());
    }
}
