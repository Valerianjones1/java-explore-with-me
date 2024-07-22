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
import ru.practicum.ewm.service.comment.CommentRepository;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.mapper.CommentMapper;
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
import java.util.Objects;
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
    private final CommentRepository commentRepository;

    private final Client statsClient;

    private static final LocalDateTime START_DATE = LocalDateTime.of(2000, 1, 1, 1, 1);
    private static final LocalDateTime END_DATE = LocalDateTime.now().plusDays(1);

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

        Event savedEvent = eventRepository.save(EventMapper.mapToEvent(newEventDto, user, location, category));

        log.info("Событие успешно сохранено {}", savedEvent);
        return EventMapper.mapToEventFullDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getByUserId(long userId, Pageable pageable) {
        Map<Long, Long> eventRequests = getConfirmedRequests();

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        List<String> eventUris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        Map<Long, ViewStats> viewStats = getStats(eventUris);

        log.info("События, где инициатором является пользователь {}, успешно получены", userId);
        return events.stream()
                .map(event -> {
                    int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
                    int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

                    EventShortDto eventShortDto = EventMapper.mapToEventShortDto(event);
                    eventShortDto.setConfirmedRequests(confirmedRequestsCount);
                    eventShortDto.setViews(views);

                    return eventShortDto;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public EventFullDto getByUserIdAndEventId(long userId, long eventId) {
        List<CommentDto> comments = commentRepository.findAllByEventId(eventId, Pageable.ofSize(10))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventRequests = getEventConfirmedRequests(eventId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        Map<Long, ViewStats> viewStats = getStats(List.of(String.format("/events/%d", eventId)));

        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(event);

        int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
        int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

        eventFullDto.setConfirmedRequests(confirmedRequestsCount);
        eventFullDto.setViews(views);
        eventFullDto.setComments(comments);

        log.info("Событие {} успешно получено", event.getId());
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateByUserIdAndEventId(UpdateEventUserRequest eventRequestDto, long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найдено", eventId)));

        List<CommentDto> comments = commentRepository.findAllByEventId(eventId, Pageable.ofSize(10))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventRequests = getEventConfirmedRequests(eventId);

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new DataIntegrityViolationException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }

        Event updatedEvent = eventRepository.save(fillEvent(event, eventRequestDto));

        Map<Long, ViewStats> viewStats = getStats(List.of(String.format("/events/%d", eventId)));

        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(updatedEvent);

        int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
        int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

        eventFullDto.setConfirmedRequests(confirmedRequestsCount);
        eventFullDto.setViews(views);
        eventFullDto.setComments(comments);

        log.info("Событие успешно обновлено {}", updatedEvent);
        return eventFullDto;
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

        Map<Long, Long> eventRequests = getConfirmedRequests();

        List<Event> events = eventRepository.findAllBySearchParams(text != null ? text.toLowerCase() : null,
                paid, rangeStart, rangeEnd, categoryIds, pageable);

        Map<Long, ViewStats> viewStats = getStats(events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList()));

        boolean isAvailable = onlyAvailable != null && onlyAvailable;

        log.info("События найдены");
        return !isAvailable ? getEventsByParams(events, viewStats, eventRequests)
                : getAvailableEventsByParams(events, viewStats, eventRequests);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(long eventId) {
        Event foundEvent = eventRepository.findByIdAndStateEquals(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найден", eventId)));

        List<CommentDto> comments = commentRepository.findAllByEventId(eventId, Pageable.ofSize(10))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventRequests = getEventConfirmedRequests(eventId);

        Map<Long, ViewStats> viewStats = getStats(List.of(String.format("/events/%d", eventId)));

        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(foundEvent);

        int confirmedRequestsCount = eventRequests.getOrDefault(eventId, 0L).intValue();
        int views = viewStats.getOrDefault(eventId, new ViewStats()).getHits().intValue();

        eventFullDto.setConfirmedRequests(confirmedRequestsCount);
        eventFullDto.setViews(views);
        eventFullDto.setComments(comments);

        log.info("Событие с идентификатором {} успешно получено", eventId);
        return eventFullDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEventsByParams(List<Long> users,
                                                List<String> states,
                                                List<Long> categoryIds,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                Pageable pageable) {
        List<EventState> eventStates = states != null ? states
                .stream()
                .map(EventState::valueOf)
                .collect(Collectors.toList()) : null;

        List<Event> events = eventRepository.findAllByCategoryIdsAndUserIds(categoryIds, users, eventStates, rangeStart, rangeEnd, pageable);

        Map<Long, Long> eventRequests = getConfirmedRequests();

        Map<Long, ViewStats> viewStats = getStats(events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList()));

        List<CommentDto> comments = commentRepository.findAllByEventCategoryId(categoryIds)
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());

        Map<Long, List<CommentDto>> eventComments = comments
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getEvent().getId().longValue(), Collectors.toList()));

        log.info("События успешно получены");

        return events
                .stream()
                .map(event -> {
                    int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
                    int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

                    EventFullDto eventFullDto = EventMapper.mapToEventFullDto(event);

                    eventFullDto.setConfirmedRequests(confirmedRequestsCount);
                    eventFullDto.setViews(views);
                    eventFullDto.setComments(eventComments.getOrDefault(event.getId(), Collections.emptyList()));

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

        List<CommentDto> comments = commentRepository.findAllByEventId(eventId, Pageable.ofSize(10))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());

        Map<Long, Long> eventRequests = getEventConfirmedRequests(eventId);

        Event updatedEvent = eventRepository.save(fillEvent(event, updateEventAdminRequest));

        Map<Long, ViewStats> viewStats = getStats(List.of(String.format("/events/%d", eventId)));

        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(updatedEvent);

        int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
        int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

        eventFullDto.setConfirmedRequests(confirmedRequestsCount);
        eventFullDto.setViews(views);
        eventFullDto.setComments(comments);

        log.info("Событие успешно обновлено {}", updatedEvent);
        return eventFullDto;
    }

    private Location getLocation(LocationDto locationDto) {
        return locationRepository.findByLatAndLon(locationDto.getLat(), locationDto.getLon())
                .orElse(locationRepository.save(LocationMapper.mapToLocation(locationDto)));
    }

    private Map<Long, ViewStats> getStats(List<String> eventIds) {
        return statsClient.sendStats(START_DATE, END_DATE, eventIds, true)
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

    private Map<Long, Long> getEventConfirmedRequests(long eventId) {
        List<Request> confirmedRequests = requestRepository.findAllByEventIdAndStatusEquals(eventId,
                RequestStatus.CONFIRMED);

        return confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));
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
                    int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
                    int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

                    if (event.getParticipantLimit() != 0 && confirmedRequestsCount <= event.getParticipantLimit()) {
                        eventShortDto.setConfirmedRequests(confirmedRequestsCount);
                        eventShortDto.setViews(views);

                        return eventShortDto;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<EventShortDto> getEventsByParams(List<Event> events, Map<Long, ViewStats> viewStats,
                                                  Map<Long, Long> eventRequests) {
        return events
                .stream()
                .map(event -> {
                    int confirmedRequestsCount = eventRequests.getOrDefault(event.getId(), 0L).intValue();
                    int views = viewStats.getOrDefault(event.getId(), new ViewStats()).getHits().intValue();

                    EventShortDto eventShortDto = EventMapper.mapToEventShortDto(event);
                    eventShortDto.setConfirmedRequests(confirmedRequestsCount);
                    eventShortDto.setViews(views);

                    return eventShortDto;
                })
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getConfirmedRequests() {
        List<Request> confirmedRequests = requestRepository.findAllByStatusEquals(RequestStatus.CONFIRMED);

        return confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));
    }
}
