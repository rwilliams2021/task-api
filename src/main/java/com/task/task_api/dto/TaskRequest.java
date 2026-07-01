package com.task.task_api.dto;

import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

    @NotBlank
    @Size(min = 3, message = "title must be at least 3 characters long")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;
}
