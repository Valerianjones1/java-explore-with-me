package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {
    private final UserService service;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Создается пользователь {}", newUserRequest);
        return service.create(newUserRequest);
    }

    @GetMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<UserDto> getUsers(@RequestParam(required = false, defaultValue = "") List<Long> ids,
                                  @PositiveOrZero @RequestParam(required = false, defaultValue = "0") int from,
                                  @Positive @RequestParam(required = false, defaultValue = "10") int size) {
        log.info("Получаем пользователей с ids {}, from={}, size={}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return service.getAll(ids, pageable);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable long userId) {
        log.info("Удаляем пользователя с идентификатором {}", userId);
        service.remove(userId);
    }
}
