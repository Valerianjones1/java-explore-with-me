package ru.practicum.ewm.service.comment.mapper;

import ru.practicum.ewm.service.comment.Comment;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.mapper.EventMapper;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.mapper.UserMapper;

import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment mapToComment(NewCommentDto newCommentDto,
                                       User user,
                                       Event event) {
        Comment comment = new Comment();

        comment.setContent(newCommentDto.getContent());
        comment.setUser(user);
        comment.setEvent(event);
        comment.setPublishedOn(LocalDateTime.now());

        return comment;
    }

    public static Comment mapToComment(UpdateCommentDto newCommentDto,
                                       User user,
                                       Event event) {
        Comment comment = new Comment();

        comment.setContent(newCommentDto.getContent());
        comment.setUser(user);
        comment.setEvent(event);
        comment.setPublishedOn(LocalDateTime.now());

        return comment;
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();

        commentDto.setId(comment.getId());
        commentDto.setContent(comment.getContent());
        commentDto.setUser(UserMapper.mapToUserShortDto(comment.getUser()));
        commentDto.setPublishedOn(comment.getPublishedOn());
        commentDto.setEvent(EventMapper.mapToEventShortDto(comment.getEvent()));

        return commentDto;
    }
}
