package ru.practicum.ewm.service.user.admin.mapper;

import ru.practicum.ewm.service.user.admin.User;
import ru.practicum.ewm.service.user.admin.dto.NewUserRequest;
import ru.practicum.ewm.service.user.admin.dto.UserDto;

public class UserMapper {

    public static User mapToUser(NewUserRequest newUserRequest) {
        User user = new User();
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());

        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();

        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());

        return userDto;
    }
}
