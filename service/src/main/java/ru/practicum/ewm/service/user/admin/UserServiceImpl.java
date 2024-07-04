package ru.practicum.ewm.service.user.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.user.admin.dto.NewUserRequest;
import ru.practicum.ewm.service.user.admin.dto.UserDto;
import ru.practicum.ewm.service.user.admin.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto create(NewUserRequest newUserRequest) {
        User user = UserMapper.mapToUser(newUserRequest);

        User savedUser = repository.save(user);

        return UserMapper.mapToUserDto(savedUser);
    }

    @Override
    public List<UserDto> getAll(List<Integer> ids, Pageable pageable) {
        if (ids.isEmpty()) {
            return repository.findAll(pageable)
                    .stream()
                    .map(UserMapper::mapToUserDto)
                    .collect(Collectors.toList());
        }

        return repository.findAllByIdIn(ids, pageable)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void remove(int userId) {
        repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));

        repository.deleteById(userId);
    }
}
