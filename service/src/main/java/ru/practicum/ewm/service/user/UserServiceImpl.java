package ru.practicum.ewm.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.user.dto.NewUserRequest;
import ru.practicum.ewm.service.user.dto.UserDto;
import ru.practicum.ewm.service.user.mapper.UserMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        User user = UserMapper.mapToUser(newUserRequest);

        User savedUser = repository.save(user);

        log.info("Пользователь успешно создан {}", savedUser);
        return UserMapper.mapToUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll(List<Long> ids, Pageable pageable) {
        if (ids.isEmpty()) {
            log.info("Пользователи успешно получены {}", ids);
            return repository.findAll(pageable)
                    .stream()
                    .map(UserMapper::mapToUserDto)
                    .collect(Collectors.toList());
        }

        log.info("Пользователи успешно получены {}", ids);
        return repository.findAllByIdIn(ids, pageable)
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void remove(long userId) {
        repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с идентификатором %s не найден", userId)));

        repository.deleteById(userId);
        log.info("Пользователи с идентификатором {} удален", userId);
    }
}
