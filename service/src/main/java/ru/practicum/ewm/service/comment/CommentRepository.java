package ru.practicum.ewm.service.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventId(long eventId, Pageable pageable);

    @Query(value = "SELECT c FROM Comment c " +
            "WHERE (:categories is null or c.event.category.id in :categories)")
    List<Comment> findAllByEventCategoryId(@Param("categories") List<Long> categories);
}
