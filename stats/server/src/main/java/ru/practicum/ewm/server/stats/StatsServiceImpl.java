package ru.practicum.ewm.server.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.server.stats.mapper.StatsMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    @Transactional
    public void createHit(EndpointHit endpointHitDto) {
        Hit endpointHit = StatsMapper.mapToEndpointHit(endpointHitDto);
        repository.save(endpointHit);
        log.info("Информация была успешно сохранена");
    }

    @Override
    @Transactional(readOnly = true)
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
