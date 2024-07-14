package ru.practicum.ewm.dto.event;

import lombok.Data;
import ru.practicum.ewm.dto.request.EventRequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private EventRequestStatus status;
}
