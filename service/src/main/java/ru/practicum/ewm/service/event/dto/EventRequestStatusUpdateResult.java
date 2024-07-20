package ru.practicum.ewm.service.event.dto;

import lombok.Data;
import ru.practicum.ewm.service.request.dto.ParticipationRequestDto;

import java.util.List;

@Data
public class EventRequestStatusUpdateResult {
    private List<ParticipationRequestDto> confirmedRequests;

    private List<ParticipationRequestDto> rejectedRequests;
}
