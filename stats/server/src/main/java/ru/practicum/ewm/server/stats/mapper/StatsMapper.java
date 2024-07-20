package ru.practicum.ewm.server.stats.mapper;

import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.server.stats.Hit;

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
