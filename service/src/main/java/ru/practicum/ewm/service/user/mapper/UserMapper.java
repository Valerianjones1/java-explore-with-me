package ru.practicum.ewm.service.user.mapper;

import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.dto.user.UserShortDto;
import ru.practicum.ewm.service.user.User;

public class UserMapper {

    public static User mapToUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());

        return user;
    }

    public static UserShortDto mapToUserShortDto(User user) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(user.getId().intValue());
        userShortDto.setName(user.getName());

        return userShortDto;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId().intValue());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());

        return userDto;
    }
}
