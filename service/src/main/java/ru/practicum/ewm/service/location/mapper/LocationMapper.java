package ru.practicum.ewm.service.location.mapper;

import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.service.location.Location;

public class LocationMapper {

    public static Location mapToLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());

        return location;
    }

    public static LocationDto mapToLocationDto(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());

        return locationDto;
    }
}
