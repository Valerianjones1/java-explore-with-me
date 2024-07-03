package ru.practicum.ewm.service.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.client.Client;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final Client statsClient;
    private static final String APP = "ewm-main-service";

    @GetMapping("/events")
    public void testCreateAndGetHit(HttpServletRequest request) {
        statsClient.sendHit(getEndpointHit(request));
        statsClient.sendHit(getEndpointHit(request));
        LocalDateTime start = LocalDateTime.of(2020, 5, 5, 0, 0, 0);
        List<ViewStats> viewStatsUnique = statsClient.sendStats(start, LocalDateTime.now().plusDays(2), List.of("/events"), true);
        List<ViewStats> viewStats = statsClient.sendStats(start, LocalDateTime.now().plusDays(2), List.of("/events"), false);
        log.info("viewStats {}", viewStats.get(0).getHits());
        log.info("viewStatsUnique {}", viewStatsUnique.get(0).getHits());
        if (viewStats.get(0).getHits() == 2L && viewStatsUnique.get(0).getHits() == 1L) {
            log.info("Все работает корректно");
        } else {
            log.info("Ошибка");
        }
    }


    private EndpointHit getEndpointHit(HttpServletRequest request) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setIp(request.getRemoteAddr());
        endpointHit.setTimestamp(LocalDateTime.now());
        endpointHit.setApp(APP);
        endpointHit.setUri(request.getRequestURI());

        return endpointHit;
    }

}
