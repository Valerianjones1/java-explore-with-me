package ru.practicum.explorewithme.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;
import ru.practicum.explorewithme.stats.mapper.StatsMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    public void createHit(EndpointHit endpointHitDto) {
        ru.practicum.ewm.dto.EndpointHit endpointHit = StatsMapper.mapToEndpointHit(endpointHitDto);
        repository.save(endpointHit);
    }

    @Override
    public List<ViewStats> getAllStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        if (unique) {
            return uris.isEmpty() ? repository.findAllViewDistinctStats(start, end)
                    : repository.findAllViewDistinctStatsWithUris(start, end, uris);
        }

        return uris.isEmpty() ? repository.findAllViewStats(start, end)
                : repository.findAllViewStatsWithUris(start, end, uris);
    }
}
