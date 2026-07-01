package com.task.task_api.dto;

import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

    @NotBlank
    @Size(min = 3, max = 255, message = "title must be between 3 and 255 characters long")
    private String title;

    @Size(max = 255, message = "description must be at most 255 characters long")
    private String description;

    @NotNull
    private TaskStatus status;

    @NotNull
    private TaskPriority priority;
}
