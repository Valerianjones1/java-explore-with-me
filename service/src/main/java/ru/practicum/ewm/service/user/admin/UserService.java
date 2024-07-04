package ru.practicum.ewm.service.user.admin;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.service.user.admin.dto.NewUserRequest;
import ru.practicum.ewm.service.user.admin.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest newUserRequest);

    List<UserDto> getAll(List<Integer> ids, Pageable pageable);

    void remove(int userId);
}
