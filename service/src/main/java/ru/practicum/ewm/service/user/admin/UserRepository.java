package ru.practicum.ewm.service.user.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findAllByIdIn(List<Integer> ids, Pageable pageable);
}
