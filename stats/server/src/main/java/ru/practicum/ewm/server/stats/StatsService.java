package ru.practicum.ewm.server.stats;

import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void createHit(EndpointHit endpointHit);

    List<ViewStats> getAllStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                Boolean unique);
}
