package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.Client;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.service.comment.mapper.CommentMapper;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.EventRepository;
import ru.practicum.ewm.service.event.dto.EventState;
import ru.practicum.ewm.service.exception.EventNotPublishedException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.exception.ValidationException;
import ru.practicum.ewm.service.request.Request;
import ru.practicum.ewm.service.request.RequestRepository;
import ru.practicum.ewm.service.request.dto.RequestStatus;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final Client statsClient;
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    private static final LocalDateTime START_DATE = LocalDateTime.of(2000, 1, 1, 1, 1);

    @Override
    @Transactional
    public CommentDto create(NewCommentDto commentDto, long userId, long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найдено", eventId)));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Событие должно быть 'PUBLISHED' для комментирования");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));

        int views = getViews(List.of(String.format("/events/%d", eventId)), eventId);

        Comment savedComment = commentRepository.save(CommentMapper.mapToComment(commentDto, user, event));

        log.info("Комментарий успешно создан {}", savedComment);
        return CommentMapper.mapToCommentDto(savedComment, views, getConfirmedRequestsCount(eventId));
    }

    @Override
    @Transactional
    public CommentDto update(UpdateCommentDto commentDto, long userId, long eventId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Комментарий с идентификатором %s не найден", commentId)));

        long commentUser = comment.getUser().getId();
        long commentEvent = comment.getEvent().getId();

        Event event = comment.getEvent();

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Событие должно быть 'PUBLISHED' для комментирования");
        }

        if (commentUser != userId && commentEvent != eventId) {
            throw new ValidationException("Некорректные userId и eventId, они должны совпадать с комментарием");
        }

        int views = getViews(List.of(String.format("/events/%d", eventId)), eventId);

        Comment updatedComment = commentRepository.save(CommentMapper.mapToComment(commentDto, comment.getUser(), event));

        log.info("Комментарий успешно обновлен {}", updatedComment);
        return CommentMapper.mapToCommentDto(updatedComment, views, getConfirmedRequestsCount(eventId));
    }

    @Override
    @Transactional
    public void remove(long userId, long eventId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Комментарий с идентификатором %s не найден", commentId)));

        long commentUser = comment.getUser().getId();
        long commentEvent = comment.getEvent().getId();

        Event event = comment.getEvent();

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Событие должно быть 'PUBLISHED' для комментирования");
        }

        if (commentUser != userId && commentEvent != eventId) {
            throw new ValidationException("Некорректные userId и eventId, они должны совпадать с комментарием");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий с идентификатором {} успешно удален", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByEventId(long eventId, Pageable pageable) {

        int confirmedRequests = getConfirmedRequestsCount(eventId);
        int views = getViews(List.of(String.format("/events/%d", eventId)), eventId);

        log.info("Комментарий под событием {} успешно получены", eventId);
        return commentRepository.findAllByEventId(eventId, pageable)
                .stream()
                .map(comment -> CommentMapper.mapToCommentDto(comment, views, confirmedRequests))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getById(long userId, long eventId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Комментарий с идентификатором %s не найден", commentId)));

        long commentUser = comment.getUser().getId();
        long commentEvent = comment.getEvent().getId();

        Event event = comment.getEvent();

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Событие должно быть 'PUBLISHED' для комментирования");
        }

        if (commentUser != userId && commentEvent != eventId) {
            throw new ValidationException("Некорректные userId и eventId, они должны совпадать с комментарием");
        }

        int confirmedRequests = getConfirmedRequestsCount(eventId);
        int views = getViews(List.of(String.format("/events/%d", eventId)), eventId);

        log.info("Комментарий с идентификатором {} успешно получен", commentId);
        return CommentMapper.mapToCommentDto(comment, views, confirmedRequests);
    }

    private int getViews(List<String> eventIds, long eventId) {
        Map<Long, ViewStats> viewStats = statsClient.sendStats(START_DATE, LocalDateTime.now().plusDays(1),
                        eventIds, true)
                .stream()
                .collect(Collectors.toMap(stats -> getEventIdFromUri(stats.getUri()), Function.identity()));

        return viewStats.getOrDefault(eventId, new ViewStats()).getHits().intValue();
    }

    private Long getEventIdFromUri(String uri) {
        if (uri.contains("?")) {
            return null;
        }
        return Long.parseLong(uri.split("/events/")[1]);
    }

    private int getConfirmedRequestsCount(long eventId) {
        List<Request> confirmedRequests = requestRepository.findAllByEventIdAndStatusEquals(eventId,
                RequestStatus.CONFIRMED);

        Map<Long, Long> eventRequests = confirmedRequests
                .stream()
                .collect(Collectors.groupingBy(request -> request.getEvent().getId(), Collectors.counting()));

        return eventRequests.getOrDefault(eventId, 0L).intValue();
    }
}
