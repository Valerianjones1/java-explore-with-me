package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.comment.dto.CommentDto;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentPublicController {
    private final CommentService service;

    @GetMapping("/{eventId}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<CommentDto> getCommentsOfEvent(@PathVariable long eventId,
                                               @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                                               @Positive @RequestParam(required = false, defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());

        return service.getAllByEventId(eventId, pageable);
    }
}
