package com.task.task_api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.task.task_api.entity.Task;
import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            """)
    List<Task> findByFilters(@Param("status") TaskStatus status, @Param("priority") TaskPriority priority);
}
