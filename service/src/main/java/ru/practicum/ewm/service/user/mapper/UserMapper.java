package ru.practicum.ewm.service.user.mapper;

import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.dto.NewUserRequest;
import ru.practicum.ewm.service.user.dto.UserDto;

public class UserMapper {

    public static User mapToUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());

        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId().intValue());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());

        return userDto;
    }
}
