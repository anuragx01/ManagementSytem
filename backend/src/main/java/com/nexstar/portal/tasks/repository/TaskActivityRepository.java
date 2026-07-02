package com.nexstar.portal.tasks.repository;

import com.nexstar.portal.tasks.entity.TaskActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, UUID> {

    Page<TaskActivity> findByTaskIdOrderByCreatedAtDesc(UUID taskId, Pageable pageable);
}
