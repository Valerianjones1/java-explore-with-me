package ru.practicum.ewm.service.user;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.service.user.dto.NewUserRequest;
import ru.practicum.ewm.service.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> getAll(List<Long> ids, Pageable pageable);

    void remove(long userId);
}
