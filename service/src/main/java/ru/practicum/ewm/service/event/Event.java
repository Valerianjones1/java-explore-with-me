package ru.practicum.ewm.service.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.ReadOnlyProperty;
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
public class Eventtest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ReadOnlyProperty
    private Long id;

    @Column
    private String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column
    private String description;

    @Column
    private LocalDateTime eventDate;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Column(name = "is_paid")
    private Boolean paid = false;

    @Column
    @Enumerated(EnumType.STRING)
    private EventState state;

    @Column
    private Integer participantLimit;

    @Column
    private Boolean requestModeration = true;

    @Column
    private String title;

}
