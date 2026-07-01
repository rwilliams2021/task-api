package com.task.task_api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.task.task_api.dto.TaskRequest;
import com.task.task_api.dto.TaskResponse;
import com.task.task_api.entity.Task;
import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;
import com.task.task_api.exception.TaskNotFoundException;
import com.task.task_api.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    @Override
    public List<TaskResponse> findAll(TaskStatus status, TaskPriority priority) {
        return taskRepository.findByFilters(status, priority).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Override
    public TaskResponse findById(UUID id) {
        return TaskResponse.from(getTaskOrThrow(id));
    }

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.OPEN)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = getTaskOrThrow(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setPriority(request.getPriority() != null ? request.getPriority() : task.getPriority());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    private Task getTaskOrThrow(UUID id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
