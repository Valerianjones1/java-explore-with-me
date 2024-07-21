package ru.practicum.ewm.service.event.mapper;

import ru.practicum.ewm.service.category.Category;
import ru.practicum.ewm.service.category.mapper.CategoryMapper;
import ru.practicum.ewm.service.event.Event;
import ru.practicum.ewm.service.event.dto.*;
import ru.practicum.ewm.service.location.Location;
import ru.practicum.ewm.service.location.mapper.LocationMapper;
import ru.practicum.ewm.service.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.user.User;
import ru.practicum.ewm.service.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

public class EventMapper {
    public static EventFullDto mapToEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        eventFullDto.setLocation(LocationMapper.mapToLocationDto(event.getLocation()));
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setInitiator(UserMapper.mapToUserShortDto(event.getInitiator()));
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setState(event.getState());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setViews(event.getViews());
        eventFullDto.setId(event.getId().intValue());
        eventFullDto.setState(event.getState());

        if (event.getState().equals(EventState.PUBLISHED)) {
            eventFullDto.setPublishedOn(LocalDateTime.now());
        }
        return eventFullDto;
    }


    public static EventShortDto mapToEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        eventShortDto.setInitiator(UserMapper.mapToUserShortDto(event.getInitiator()));
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setId(event.getId().intValue());
        eventShortDto.setViews(event.getViews());
        return eventShortDto;
    }

    public static EventShortDto mapToEventShortDto(Event event, int confirmedRequests, int views) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        eventShortDto.setInitiator(UserMapper.mapToUserShortDto(event.getInitiator()));
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setId(event.getId().intValue());
        eventShortDto.setViews(views);
        eventShortDto.setConfirmedRequests(confirmedRequests);
        return eventShortDto;
    }

    public static Event mapToEvent(NewEventDto newEventDto, User initiator, Location location, Category category) {
        Event event = new Event();
        event.setEventDate(newEventDto.getEventDate());
        event.setCategory(category);
        event.setAnnotation(newEventDto.getAnnotation());
        event.setPaid(newEventDto.getPaid());
        event.setDescription(newEventDto.getDescription());
        event.setInitiator(initiator);
        event.setLocation(location);
        event.setTitle(newEventDto.getTitle());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setParticipantLimit(newEventDto.getParticipantLimit());

        return event;
    }

    public static EventRequestStatusUpdateResult mapToEventStatusResult(List<ParticipationRequestDto> confirmedRequests,
                                                                        List<ParticipationRequestDto> rejectedRequests) {
        EventRequestStatusUpdateResult eventResult = new EventRequestStatusUpdateResult();

        eventResult.setRejectedRequests(rejectedRequests);
        eventResult.setConfirmedRequests(confirmedRequests);

        return eventResult;
    }
}
