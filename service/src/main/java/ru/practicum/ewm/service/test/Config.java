package ru.practicum.ewm.service.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.client.StatsClient;

@Configuration
public class Config {

    @Bean
    public StatsClient statsClient(@Value("${stats-service.url}") String statsUrl) {
        return new StatsClient(statsUrl);
    }
}
