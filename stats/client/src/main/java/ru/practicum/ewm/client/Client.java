package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.ewm.dto.EndpointHit;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class Client {
    private final WebClient webClient;
    private final String appName;

    public Client(@Value("${stats-service.url}") String statsUrl,
                  @Value("${spring.application.name}") String appName) {
        this.webClient = WebClient.create(statsUrl);
        this.appName = appName;
    }


//    public EndPointHit sendHit(HttpServletRequest request) {
//        try {
//            Mono<Void> monoResponse = webClient
//                    .post()
//                    .uri("/hit")
//                    .contentType(MediaType.APPLICATION_JSON)
//                    .body(endPointHit, EndpointHit.class)
//                    .retrieve()
//                    .bodyToMono(Void.class);
//
//            monoResponse.subscribe();
//            Thread.sleep(5000);
//        } catch (Exception ex) {
//            log.error("error", ex);
//        }
//    }

    public void sendStats(HttpServletRequest request,
                          LocalDateTime start,
                          LocalDateTime end,
                          List<String> uris,
                          Boolean unique) {
        try {
            Mono<String> monoResponse = webClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/stats/")
                            .queryParam())
                    .retrieve()
                    .bodyToMono(String.class);
            monoResponse.subscribe(s -> log.info("got response: " + s));
            log.info("sleeping...");
            Thread.sleep(5000);
            log.info("completed");
        } catch (Exception ex) {
            log.error("error", ex);
        }
    }
}
