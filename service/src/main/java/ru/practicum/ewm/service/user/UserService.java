package ru.practicum.ewm.service.user;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> getAll(List<Long> ids, Pageable pageable);

    void remove(long userId);
}
