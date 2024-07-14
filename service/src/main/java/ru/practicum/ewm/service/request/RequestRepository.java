package ru.practicum.ewm.service.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.dto.request.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventIdAndStatusEquals(long eventId, RequestStatus status);

    List<Request> findAllByRequesterId(long requesterId);

    List<Request> findAllByEventInitiatorIdAndEventId(long userId, long eventId);

    List<Request> findAllByEventInitiatorIdAndEventIdAndIdInAndStatusEquals(long userId, long eventId, List<Long> requestIds, RequestStatus status);
}
