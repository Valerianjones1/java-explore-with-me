package ru.practicum.ewm.service.comment;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto create(NewCommentDto commentDto, long userId, long eventId);

    CommentDto update(UpdateCommentDto commentDto, long userId, long eventId, long commentId);

    void remove(long userId, long eventId, long commentId);

    List<CommentDto> getAllByEventId(long eventId, Pageable pageable);

    CommentDto getById(long userId, long eventId, long commentId);
}
