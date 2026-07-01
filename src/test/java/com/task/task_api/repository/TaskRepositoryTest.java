package com.task.task_api.repository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.task.task_api.entity.Task;
import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.saveAll(List.of(
                Task.builder().title("Write report").status(TaskStatus.OPEN).priority(TaskPriority.HIGH).build(),
                Task.builder().title("Fix bug").status(TaskStatus.IN_PROGRESS).priority(TaskPriority.HIGH).build(),
                Task.builder().title("Clean desk").status(TaskStatus.OPEN).priority(TaskPriority.LOW).build()));
    }

    @Test
    void shouldReturnOnlyMatchingTasksWhenFilteringByStatus() {
        List<Task> tasks = taskRepository.findByStatus(TaskStatus.OPEN);

        assertThat(tasks)
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Write report", "Clean desk");
    }

    @Test
    void shouldReturnOnlyMatchingTasksWhenFilteringByPriority() {
        List<Task> tasks = taskRepository.findByPriority(TaskPriority.HIGH);

        assertThat(tasks)
                .hasSize(2)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Write report", "Fix bug");
    }

    @Test
    void shouldReturnOnlyExactMatchWhenFilteringByStatusAndPriority() {
        List<Task> tasks = taskRepository.findByStatusAndPriority(TaskStatus.OPEN, TaskPriority.HIGH);

        assertThat(tasks)
                .singleElement()
                .extracting(Task::getTitle)
                .isEqualTo("Write report");
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtOnPersist() {
        Task saved = taskRepository.save(
                Task.builder().title("New task").status(TaskStatus.OPEN).priority(TaskPriority.MEDIUM).build());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
