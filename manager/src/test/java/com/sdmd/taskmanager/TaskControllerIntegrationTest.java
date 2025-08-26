package com.sdmd.taskmanager.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdmd.taskmanager.dto.TaskCreateRequest;
import com.sdmd.taskmanager.exception.GlobalExceptionHandler;
import com.sdmd.taskmanager.exception.TaskNotFoundException;
import com.sdmd.taskmanager.model.Task;
import com.sdmd.taskmanager.service.TaskService;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = true)
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)

public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    private Task task;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TaskService taskService() {
            return Mockito.mock(TaskService.class);
        }
    }

    @BeforeEach
    void setup() {
        reset(taskService);
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(Task.StatusEnum.PENDING);
        task.setDeadline(OffsetDateTime.now().plusDays(1));
    }

    @Test
    void createTask_Success() throws Exception {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setDescription("New Desc");
        request.setDeadline(OffsetDateTime.now().plusDays(2));

        Task savedTask = new Task();
        savedTask.setId(10L);
        savedTask.setTitle(request.getTitle());
        savedTask.setDescription(request.getDescription());
        savedTask.setDeadline(request.getDeadline());
        savedTask.setStatus(Task.StatusEnum.PENDING);

        Mockito.when(taskService.createTask(any(TaskCreateRequest.class))).thenReturn(savedTask);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.url").value("http://localhost/tasks/10"));
    }

    @Test
    void createTask_ValidationFailure() throws Exception {
        TaskCreateRequest invalidRequest = new TaskCreateRequest(); // missing title and description

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.title").value("must not be null"))
                .andExpect(jsonPath("$.errors.description").value("must not be null"));
    }


    @Test
    void getTask_Success() throws Exception {
        Mockito.when(taskService.getTaskById(1L)).thenReturn(task);

        mockMvc.perform(get("/tasks/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.title").value(task.getTitle()))
                .andExpect(jsonPath("$.description").value(task.getDescription()));
    }

    @Test
    void getTask_NotFound() throws Exception {
        long invalidId = 99L;

        Mockito.when(taskService.getTaskById(invalidId))
                .thenThrow(new TaskNotFoundException(invalidId));

        mockMvc.perform(get("/tasks/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + invalidId + " not found."));
    }

    @Test
    void getAllTasks_Success() throws Exception {
        List<Task> taskList = new ArrayList<>();
        taskList.add(task);
        Mockito.when(taskService.getAllTasks()).thenReturn(taskList);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(task.getId()))
                .andExpect(jsonPath("$[0].title").value(task.getTitle()));
    }

    @Test
    void updateTask_Success() throws Exception {
        Task updated = new Task();
        updated.setId(1L);
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Desc");
        updated.setStatus(Task.StatusEnum.IN_PROGRESS);
        updated.setDeadline(OffsetDateTime.now().plusDays(5));

        Mockito.when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(updated);

        mockMvc.perform(put("/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value(Task.StatusEnum.IN_PROGRESS.getValue()));
    }

    @Test
    void updateTask_NotFound() throws Exception {
        long invalidId = 99L;
        Task updateRequest = new Task();
        updateRequest.setTitle("Title");
        updateRequest.setDescription("Desc");

        Mockito.when(taskService.updateTask(eq(invalidId), any(Task.class)))
                .thenThrow(new TaskNotFoundException(invalidId));

        mockMvc.perform(put("/tasks/{id}", invalidId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id " + invalidId + " not found."));
    }

    @Test
    void deleteTask_Success() throws Exception {
        Mockito.doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_NotFound() throws Exception {
        long invalidId = 99L;
        Mockito.doThrow(new TaskNotFoundException(invalidId)).when(taskService).deleteTask(invalidId);

        mockMvc.perform(delete("/tasks/{id}", invalidId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id " + invalidId + " not found."));
    }
}