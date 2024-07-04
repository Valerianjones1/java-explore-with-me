package ru.practicum.ewm.service.user;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByIdIn(List<Long> ids, Pageable pageable);
}
