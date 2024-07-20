package ru.practicum.ewm.service.event.dto;

import lombok.Data;
import ru.practicum.ewm.service.request.dto.EventRequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;

    private EventRequestStatus status;
}
