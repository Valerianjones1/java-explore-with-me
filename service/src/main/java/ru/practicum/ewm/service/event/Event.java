package ru.practicum.ewm.service.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.service.category.Category;
import ru.practicum.ewm.service.event.dto.EventState;
import ru.practicum.ewm.service.location.Location;
import ru.practicum.ewm.service.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    @Column(name = "is_paid")
    private Boolean paid = false;

    @Column
    @Enumerated(EnumType.STRING)
    private EventState state = EventState.PENDING;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column
    private Boolean requestModeration = true;

    @Column
    private String title;

    @Transient
    private Integer views = 0;

    @Column(name = "date_create")
    private LocalDateTime createdOn = LocalDateTime.now();
}
