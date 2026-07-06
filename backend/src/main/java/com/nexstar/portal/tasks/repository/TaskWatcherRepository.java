package com.nexstar.portal.tasks.repository;

import com.nexstar.portal.tasks.entity.TaskWatcher;
import com.nexstar.portal.tasks.entity.TaskWatcherId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskWatcherRepository extends JpaRepository<TaskWatcher, TaskWatcherId> {

    @Query("SELECT tw FROM TaskWatcher tw WHERE tw.task.id = :taskId")
    List<TaskWatcher> findByTaskId(UUID taskId);

    @Query("SELECT COUNT(tw) > 0 FROM TaskWatcher tw WHERE tw.task.id = :taskId AND tw.user.id = :userId")
    boolean existsByTaskIdAndUserId(UUID taskId, UUID userId);

    @Modifying
    @Query("DELETE FROM TaskWatcher tw WHERE tw.task.id = :taskId AND tw.user.id = :userId")
    void deleteByTaskIdAndUserId(UUID taskId, UUID userId);

    @Query("SELECT COUNT(tw) FROM TaskWatcher tw WHERE tw.task.id = :taskId")
    long countByTaskId(UUID taskId);
}
