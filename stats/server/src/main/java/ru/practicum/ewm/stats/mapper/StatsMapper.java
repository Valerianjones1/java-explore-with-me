package ru.practicum.ewm.stats.mapper;

import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.stats.Hit;

public class StatsMapper {
    public static Hit mapToEndpointHit(EndpointHit createEndpointHit) {
        Hit endpointHit = new Hit();
        endpointHit.setIp(createEndpointHit.getIp());
        endpointHit.setUri(createEndpointHit.getUri());
        endpointHit.setTimestamp(createEndpointHit.getTimestamp());
        endpointHit.setApp(createEndpointHit.getApp());

        return endpointHit;
    }

}
