package com.task.task_api.controller;

import com.task.task_api.dto.TaskRequest;
import com.task.task_api.dto.TaskResponse;
import com.task.task_api.entity.TaskPriority;
import com.task.task_api.entity.TaskStatus;
import com.task.task_api.exception.TaskNotFoundException;
import com.task.task_api.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void shouldReturn201WithLocationWhenCreatingValidTask() throws Exception {
        UUID id = UUID.randomUUID();
        TaskRequest request = new TaskRequest();
        request.setTitle("Write tests");
        request.setStatus(TaskStatus.OPEN);
        request.setPriority(TaskPriority.MEDIUM);

        given(taskService.create(any(TaskRequest.class))).willReturn(sampleResponse(id));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(id.toString())))
                .andExpect(jsonPath("$.title").value("Write tests"));
    }

    @Test
    void shouldReturn400WhenCreatingTaskWithTooShortTitle() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("ab");
        request.setStatus(TaskStatus.OPEN);
        request.setPriority(TaskPriority.MEDIUM);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCreatingTaskWithMissingStatus() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Valid title");
        request.setPriority(TaskPriority.MEDIUM);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenTaskExists() throws Exception {
        UUID id = UUID.randomUUID();
        given(taskService.findById(id)).willReturn(sampleResponse(id));

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void shouldReturn404WhenTaskMissing() throws Exception {
        UUID id = UUID.randomUUID();
        given(taskService.findById(id)).willThrow(new TaskNotFoundException(id));

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPassStatusFilterThroughToService() throws Exception {
        given(taskService.findAll(eq(TaskStatus.OPEN), eq(null))).willReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/tasks").param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldPassPriorityFilterThroughToService() throws Exception {
        given(taskService.findAll(eq(null), eq(TaskPriority.HIGH))).willReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/tasks").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldPassCombinedFiltersThroughToService() throws Exception {
        given(taskService.findAll(eq(TaskStatus.OPEN), eq(TaskPriority.HIGH)))
                .willReturn(List.of(sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/tasks").param("status", "OPEN").param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnAllTasksWhenNoFiltersGiven() throws Exception {
        given(taskService.findAll(eq(null), eq(null)))
                .willReturn(List.of(sampleResponse(UUID.randomUUID()), sampleResponse(UUID.randomUUID())));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn204WhenDeletingExistingTask() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingMissingTask() throws Exception {
        UUID id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new TaskNotFoundException(id)).when(taskService).delete(id);

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn200WhenUpdatingTaskWithValidRequest() throws Exception {
        UUID id = UUID.randomUUID();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated title");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);

        given(taskService.update(eq(id), any(TaskRequest.class))).willReturn(sampleResponse(id));

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenUpdatingMissingTask() throws Exception {
        UUID id = UUID.randomUUID();
        TaskRequest request = new TaskRequest();
        request.setTitle("Updated title");
        request.setStatus(TaskStatus.DONE);
        request.setPriority(TaskPriority.LOW);

        given(taskService.update(eq(id), any(TaskRequest.class))).willThrow(new TaskNotFoundException(id));

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    private TaskResponse sampleResponse(UUID id) {
        return TaskResponse.builder()
                .id(id)
                .title("Write tests")
                .status(TaskStatus.OPEN)
                .priority(TaskPriority.MEDIUM)
                .createdAt(FIXED_INSTANT)
                .updatedAt(FIXED_INSTANT)
                .build();
    }
}
