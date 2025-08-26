package com.sdmd.taskmanager.service.impl;

import com.sdmd.taskmanager.dto.TaskCreateRequest;
import com.sdmd.taskmanager.entity.TaskEntity;
import com.sdmd.taskmanager.mapper.TaskMapper;
import com.sdmd.taskmanager.model.Task;
import com.sdmd.taskmanager.repository.TaskRepository;
import com.sdmd.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public Task createTask(TaskCreateRequest request) {
        // Manually map TaskCreateRequest → Task
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setStatus(Task.StatusEnum.PENDING); // default status

        TaskEntity entity = taskMapper.toEntity(task);

        TaskEntity savedEntity = taskRepository.save(entity);

        return taskMapper.toDto(savedEntity);
    }

    @Override
    public Task getTaskById(Long id) {
        TaskEntity entity = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return taskMapper.toDto(entity);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Task updateTask(Long id, Task updatedTask) {
        TaskEntity existing = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setStatus(updatedTask.getStatus().toString());
        existing.setDeadline(updatedTask.getDeadline());

        return taskMapper.toDto(taskRepository.save(existing));
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
