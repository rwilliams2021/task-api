package com.task.task_api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.task.task_api.dto.TaskRequest;
import com.task.task_api.dto.TaskResponse;
import com.task.task_api.entity.Task;
import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;
import com.task.task_api.exception.TaskNotFoundException;
import com.task.task_api.repository.TaskRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void shouldDelegateToCombinedQueryWhenStatusAndPriorityGiven() {
        when(taskRepository.findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH))
                .thenReturn(List.of(sampleTask()));

        List<TaskResponse> result = taskService.findAll(TaskStatus.OPEN, TaskPriority.HIGH);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void shouldReturnEverythingWhenNoFiltersGiven() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask()));

        List<TaskResponse> result = taskService.findAll(null, null);

        assertThat(result).hasSize(1);
        verify(taskRepository).findAll();
    }

    @Test
    void shouldDelegateToStatusQueryWhenOnlyStatusGiven() {
        when(taskRepository.findByStatus(TaskStatus.OPEN)).thenReturn(List.of(sampleTask()));

        List<TaskResponse> result = taskService.findAll(TaskStatus.OPEN, null);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByStatus(TaskStatus.OPEN);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void shouldDelegateToPriorityQueryWhenOnlyPriorityGiven() {
        when(taskRepository.findByPriority(TaskPriority.HIGH)).thenReturn(List.of(sampleTask()));

        List<TaskResponse> result = taskService.findAll(null, TaskPriority.HIGH);

        assertThat(result).hasSize(1);
        verify(taskRepository).findByPriority(TaskPriority.HIGH);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void shouldReturnTaskWhenFindByIdMatches() {
        Task task = sampleTask();
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskResponse response = taskService.findById(task.getId());

        assertThat(response.getId()).isEqualTo(task.getId());
        assertThat(response.getTitle()).isEqualTo(task.getTitle());
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenFindByIdMissing() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(id))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void shouldSaveTaskBuiltFromRequestOnCreate() {
        TaskRequest request = new TaskRequest();
        request.setTitle("Write tests");
        request.setStatus(TaskStatus.OPEN);
        request.setPriority(TaskPriority.MEDIUM);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(request);

        assertThat(response.getTitle()).isEqualTo("Write tests");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.OPEN);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }

    @Test
    void shouldUpdateAndFlushTaskWhenUpdatingExistingTask() {
        Task existing = sampleTask();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated title");
        request.setDescription("Updated description");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);
        when(taskRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(taskRepository.saveAndFlush(existing)).thenReturn(existing);

        TaskResponse response = taskService.update(existing.getId(), request);

        assertThat(response.getTitle()).isEqualTo("Updated title");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.LOW);
        verify(taskRepository).saveAndFlush(existing);
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenUpdatingMissingTask() {
        UUID id = UUID.randomUUID();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated title");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(id, request))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).saveAndFlush(any(Task.class));
    }

    @Test
    void shouldThrowAndNeverDeleteWhenDeletingMissingTask() {
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(id))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, times(0)).deleteById(id);
    }

    @Test
    void shouldDeleteWhenTaskExists() {
        UUID id = UUID.randomUUID();
        when(taskRepository.existsById(id)).thenReturn(true);

        taskService.delete(id);

        verify(taskRepository).deleteById(id);
    }

    private Task sampleTask() {
        return Task.builder()
                .id(UUID.randomUUID())
                .title("Sample")
                .status(TaskStatus.OPEN)
                .priority(TaskPriority.HIGH)
                .build();
    }
}
