package com.nexstar.portal.tasks.repository;

import com.nexstar.portal.tasks.entity.TaskChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskChecklistRepository extends JpaRepository<TaskChecklist, UUID> {

    List<TaskChecklist> findByTaskIdAndDeletedFalseOrderByPosition(UUID taskId);

    Optional<TaskChecklist> findByIdAndDeletedFalse(UUID id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE TaskChecklist cl SET cl.deleted = true WHERE cl.task.id = :taskId AND cl.deleted = false")
    void softDeleteByTaskId(UUID taskId);
}
