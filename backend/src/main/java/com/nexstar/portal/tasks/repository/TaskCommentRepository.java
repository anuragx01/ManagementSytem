package com.nexstar.portal.tasks.repository;

import com.nexstar.portal.tasks.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, UUID> {

    List<TaskComment> findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(UUID taskId);

    Optional<TaskComment> findByIdAndDeletedFalse(UUID id);
}
