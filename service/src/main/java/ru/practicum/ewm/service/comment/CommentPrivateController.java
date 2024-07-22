package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.comment.dto.CommentDto;
import ru.practicum.ewm.service.comment.dto.NewCommentDto;
import ru.practicum.ewm.service.comment.dto.UpdateCommentDto;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateController {
    private final CommentService service;

    @PostMapping("/{userId}/events/{eventId}/comments")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody NewCommentDto commentDto,
                                    @PathVariable long userId,
                                    @PathVariable long eventId) {
        log.info("Создаем комментарии {} под событием {}", commentDto, eventId);
        commentDto.setUserId(userId);
        commentDto.setEventId(eventId);
        return service.create(commentDto);
    }

    @GetMapping("/{userId}/events/{eventId}/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CommentDto getCommentById(@PathVariable long userId,
                                     @PathVariable long eventId,
                                     @PathVariable long commentId) {
        log.info("Получаем комментарий с идентификатором {} под событием {}", commentId, eventId);
        return service.getById(userId, eventId, commentId);
    }

    @PatchMapping("/{userId}/events/{eventId}/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody UpdateCommentDto commentDto,
                                    @PathVariable long userId,
                                    @PathVariable long eventId,
                                    @PathVariable long commentId) {
        log.info("Обновляем комментарии {} под событием {}", commentDto, eventId);
        commentDto.setUserId(userId);
        commentDto.setEventId(eventId);
        commentDto.setCommentId(commentId);
        return service.update(commentDto);
    }

    @DeleteMapping("/{userId}/events/{eventId}/comments/{commentId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long userId,
                              @PathVariable long eventId,
                              @PathVariable long commentId) {
        log.info("Удаляем комментарий с идентификатором{}", commentId);
        service.remove(userId, eventId, commentId);
    }
}
