package com.sdmd.taskmanager.service;

import com.sdmd.taskmanager.dto.TaskCreateRequest;
import com.sdmd.taskmanager.model.Task;

import java.util.List;

public interface TaskService {
    Task createTask(TaskCreateRequest request);
    Task getTaskById(Long id);
    List<Task> getAllTasks();
    Task updateTask(Long id, Task task);
    void deleteTask(Long id);
}
