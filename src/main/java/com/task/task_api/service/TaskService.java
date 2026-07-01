package com.task.task_api.service;

import java.util.List;
import java.util.UUID;

import com.task.task_api.dto.TaskRequest;
import com.task.task_api.dto.TaskResponse;
import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;

public interface TaskService {

    List<TaskResponse> findAll(TaskStatus status, TaskPriority priority);

    TaskResponse findById(UUID id);

    TaskResponse create(TaskRequest request);

    TaskResponse update(UUID id, TaskRequest request);

    void delete(UUID id);
}
