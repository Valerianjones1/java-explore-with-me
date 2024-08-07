package ru.practicum.ewm.service.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.service.event.dto.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(long userId, Pageable pageable);

    List<Event> findAllByIdIn(Set<Long> eventIds);

    Optional<Event> findByIdAndInitiatorId(long eventId, long userId);

    Optional<Event> findByIdAndStateEquals(long eventId, EventState state);

    @Query(value = "SELECT e FROM Event e " +
            "WHERE (:categoryIds is null or e.category.id in :categoryIds) " +
            "and (cast(:startDate as date) is null or e.eventDate >= :startDate) " +
            "and (cast(:endDate as date) is null or e.eventDate <= :endDate) " +
            "and (:userIds is null or e.initiator.id in :userIds) " +
            "and (:eventStates is null or e.state in :eventStates)")
    List<Event> findAllByCategoryIdsAndUserIds(@Param("categoryIds") List<Long> categoryIds,
                                               @Param("userIds") List<Long> userIds,
                                               @Param("eventStates") List<EventState> eventStates,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate,
                                               Pageable pageable);

    @Query(value = "SELECT * FROM events e " +
            "WHERE (:categories is null or e.category_id in :categories) " +
            "and (cast(:rangeStart as date) is null or e.event_date >= :rangeStart) " +
            "and (cast(:rangeEnd as date) is null or e.event_date <= :rangeEnd) " +
            "and (:paid is null or e.is_paid = :paid) " +
            "and (:text is null or lower(e.annotation) LIKE %:text% " +
            "or lower(e.description) LIKE %:text%)", nativeQuery = true)
    List<Event> findAllBySearchParams(@Param("text") String text,
                                      @Param("paid") Boolean paid,
                                      @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEnd") LocalDateTime rangeEnd,
                                      @Param("categories") List<Long> categories,
                                      Pageable pageable);
}
