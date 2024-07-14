package ru.practicum.ewm.server.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.dto.stats.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats(eh.app,eh.uri,count(eh.ip)) " +
            "FROM Hit AS eh " +
            "WHERE eh.timestamp > :start AND eh.timestamp < :end AND eh.uri IN (:uris) " +
            "GROUP BY eh.app, eh.ip, eh.uri " +
            "ORDER BY count(eh.ip) DESC")
    List<ViewStats> findAllViewStatsWithUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                     @Param("uris") List<String> uris);

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats(eh.app,eh.uri,count(distinct(eh.ip))) " +
            "FROM Hit AS eh " +
            "WHERE eh.timestamp > :start AND eh.timestamp < :end AND eh.uri IN (:uris)" +
            "GROUP BY eh.app, eh.ip, eh.uri " +
            "ORDER BY count(distinct(eh.ip)) DESC")
    List<ViewStats> findAllViewDistinctStatsWithUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                             @Param("uris") List<String> uris);

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats(eh.app,eh.uri,count(eh.ip)) " +
            "FROM Hit AS eh " +
            "WHERE eh.timestamp > :start AND eh.timestamp < :end " +
            "GROUP BY eh.app, eh.ip, eh.uri " +
            "ORDER BY count(eh.ip) DESC")
    List<ViewStats> findAllViewStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT new ru.practicum.ewm.dto.stats.ViewStats(eh.app,eh.uri,count(distinct(eh.ip))) " +
            "FROM Hit AS eh " +
            "WHERE eh.timestamp > :start AND eh.timestamp < :end " +
            "GROUP BY eh.app, eh.ip, eh.uri " +
            "ORDER BY count(distinct(eh.ip)) DESC")
    List<ViewStats> findAllViewDistinctStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


}
