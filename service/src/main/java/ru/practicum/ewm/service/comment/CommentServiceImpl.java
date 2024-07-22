package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.service.comment.mapper.CommentMapper;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.EventRepository;
import ru.practicum.ewm.service.event.dto.EventState;
import ru.practicum.ewm.service.exception.EventNotPublishedException;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.exception.NotRightUserOrEventException;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentDto create(NewCommentDto commentDto) {
        long eventId = commentDto.getEventId();
        long userId = commentDto.getUserId();

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с идентификатором %s не найдено", eventId)));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Событие должно быть 'PUBLISHED' для комментирования");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));

        Comment savedComment = commentRepository.save(CommentMapper.mapToComment(commentDto, user, event));

        log.info("Комментарий успешно создан {}", savedComment);
        return CommentMapper.mapToCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto update(UpdateCommentDto commentDto) {
        long eventId = commentDto.getEventId();
        long userId = commentDto.getUserId();
        long commentId = commentDto.getCommentId();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Комментарий с идентификатором %s не найден", commentId)));

        long commentUser = comment.getUser().getId();
        long commentEvent = comment.getEvent().getId();

        Event event = comment.getEvent();

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new EventNotPublishedException("Событие должно быть 'PUBLISHED' для комментирования");
        }

        if (commentUser != userId || commentEvent != eventId) {
            throw new NotRightUserOrEventException("Некорректные userId и eventId, они должны совпадать с комментарием");
        }

        Comment updatedComment = commentRepository.save(CommentMapper.mapToComment(commentDto, comment.getUser(), event));

        log.info("Комментарий успешно обновлен {}", updatedComment);
        return CommentMapper.mapToCommentDto(updatedComment);
    }

    @Override
    @Transactional
    public void remove(long userId, long eventId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Комментарий с идентификатором %s не найден", commentId)));

        long commentUser = comment.getUser().getId();

        if (commentUser != userId) {
            throw new NotRightUserOrEventException("Некорректный userId, он должен совпадать с комментарием");
        }

        commentRepository.deleteById(commentId);
        log.info("Комментарий с идентификатором {} успешно удален", commentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByEventId(long eventId, Pageable pageable) {
        log.info("Комментарий под событием {} успешно получены", eventId);
        return commentRepository.findAllByEventId(eventId, pageable)
                .stream()
                .map(CommentMapper::mapToCommentDto)
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
            throw new NotRightUserOrEventException("Некорректные userId и eventId, они должны совпадать с комментарием");
        }

        log.info("Комментарий с идентификатором {} успешно получен", commentId);
        return CommentMapper.mapToCommentDto(comment);
    }
}
