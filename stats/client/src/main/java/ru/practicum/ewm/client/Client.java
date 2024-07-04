package ru.practicum.ewm.client;

import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface Client {
    void sendHit(EndpointHit endpointHit);

    List<ViewStats> sendStats(LocalDateTime start,
                              LocalDateTime end,
                              List<String> uris,
                              Boolean unique);
}
