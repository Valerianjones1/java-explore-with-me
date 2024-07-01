package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Client {
    private final WebClient webClient;

    public Client(@Value("${stats-service.url}") String statsUrl) {
        this.webClient = WebClient.create(statsUrl);
    }


    public EndpointHit sendHit(EndpointHit endpointHit) {
        return webClient
                .post()
                .uri("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .body(endpointHit, EndpointHit.class)
                .retrieve()
                .bodyToMono(EndpointHit.class)
                .doOnError(error -> log.error("Ошибка с клиентом /hit {}", error.getMessage()))
                .block();
    }

    public List<ViewStats> sendStats(LocalDateTime start,
                          LocalDateTime end,
                          List<String> uris,
                          Boolean unique) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats/")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique)
                        .build())
                .retrieve()
                .bodyToMono(List.class)
                .doOnError(error -> log.error("Ошибка с клиентом /stats/ {}", error.getMessage()))
                .block();

    }
}

