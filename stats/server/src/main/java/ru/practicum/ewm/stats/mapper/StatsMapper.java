package ru.practicum.explorewithme.stats.mapper;

import ru.practicum.ewm.dto.EndpointHit;

public class StatsMapper {
    public static EndpointHit mapToEndpointHit(EndpointHit createEndpointHit) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setIp(createEndpointHit.getIp());
        endpointHit.setUri(createEndpointHit.getUri());
        endpointHit.setTimestamp(createEndpointHit.getTimestamp());
        endpointHit.setApp(createEndpointHit.getApp());

        return endpointHit;
    }

//    public static ViewStats mapToViewStats() {
//        return new ViewStats();
//    }
}
