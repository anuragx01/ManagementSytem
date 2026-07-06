package com.nexstar.portal.tasks.repository;

import com.nexstar.portal.tasks.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedFalse(UUID id);

    Page<Task> findByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    List<Task> findByProjectIdAndSprintIdAndDeletedFalse(UUID projectId, UUID sprintId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.sprint IS NULL AND t.deleted = false")
    List<Task> findByProjectIdAndSprintIdIsNullAndDeletedFalse(UUID projectId);

    Page<Task> findByAssigneeIdAndDeletedFalse(UUID assigneeId, Pageable pageable);

    long countByProjectIdAndStatusAndDeletedFalse(UUID projectId, Task.TaskStatus status);

    List<Task> findByParentIdAndDeletedFalse(UUID parentId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.deleted = false")
    long countByProjectId(UUID projectId);
}
