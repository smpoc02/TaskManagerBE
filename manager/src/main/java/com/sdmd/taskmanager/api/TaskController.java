package com.sdmd.taskmanager.api;

import com.sdmd.taskmanager.dto.TaskCreateRequest;
import com.sdmd.taskmanager.model.Task;
import com.sdmd.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "API for managing tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Create a new task", description = "Creates a new task and returns the location of the created resource.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            UriComponentsBuilder uriBuilder) {

        Task savedTask = taskService.createTask(request);
        URI location = uriBuilder.path("/tasks/{id}").buildAndExpand(savedTask.getId()).toUri();

        Map<String, Object> response = Map.of(
                "id", savedTask.getId(),
                "url", location.toString()
        );

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get a task by ID", description = "Retrieves the task with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Get all tasks", description = "Returns a list of all tasks.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @Operation(summary = "Update an existing task", description = "Updates the task with the given ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @Operation(summary = "Delete a task", description = "Deletes the task with the specified ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
