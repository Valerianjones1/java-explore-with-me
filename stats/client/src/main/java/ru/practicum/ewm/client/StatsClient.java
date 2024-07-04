package ru.practicum.ewm.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.ewm.dto.EndpointHit;
import ru.practicum.ewm.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatsClient implements Client {
    private final WebClient webClient;

    public StatsClient(@Value("${stats-service.url}") String statsUrl) {
        this.webClient = WebClient.create(statsUrl);
    }


    @Override
    public void sendHit(EndpointHit endpointHit) {
        try {
            String uri = "/hit";
            webClient
                    .post()
                    .uri(uri)
                    .bodyValue(endpointHit)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.debug("Запрос к сервису статистики был успешно отправлен на эндпоинт {}" +
                            ", с ответом {}", uri, response))
                    .doOnError(error -> log.error("Произошла ошибка при запросе на эндпоинт {}, с сообщением {}",
                            uri, error.getMessage()))
                    .block();
        } catch (Exception e) {
            log.error("Ошибка с клиентом /hit {}", e.getMessage());
        }
    }

    @Override
    public List<ViewStats> sendStats(LocalDateTime start,
                                     LocalDateTime end,
                                     List<String> uris,
                                     Boolean unique) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String uri = "/stats/";
            return webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path(uri)
                            .queryParam("start", start.format(formatter))
                            .queryParam("end", end.format(formatter))
                            .queryParam("uris", uris)
                            .queryParam("unique", unique)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<ViewStats>>() {
                    })
                    .doOnSuccess(response -> log.debug("Запрос к сервису статистики был успешно отправлен на эндпоинт {}" +
                            ", с ответом {}", uri, response))
                    .doOnError(error -> log.error("Произошла ошибка при запросе на эндпоинт {}, с сообщением {}",
                            uri, error.getMessage()))
                    .block();
        } catch (Exception e) {
            log.error("Ошибка с клиентом /stats {}", e.getMessage());
            return Collections.emptyList();
        }

    }
}

