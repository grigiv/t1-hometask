package ru.t1.taskmanager.service;

import ru.t1.taskmanager.aspect.annotation.LogAfterReturning;
import ru.t1.taskmanager.aspect.annotation.LogAfterThrowing;
import ru.t1.taskmanager.aspect.annotation.LogBefore;
import ru.t1.taskmanager.aspect.annotation.LogExecutionTime;
import ru.t1.taskmanager.model.Task;
import ru.t1.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @LogBefore
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @LogAfterReturning
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @LogAfterThrowing
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    @LogExecutionTime
    public Optional<Task> updateTask(Long id, Task updatedTask) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setUserId(updatedTask.getUserId());
            return taskRepository.save(task);
        });
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
