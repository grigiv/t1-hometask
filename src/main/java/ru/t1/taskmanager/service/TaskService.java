package ru.t1.taskmanager.service;

import ru.t1.taskmanager.aspect.annotation.LogAfterReturning;
import ru.t1.taskmanager.aspect.annotation.LogAfterThrowing;
import ru.t1.taskmanager.aspect.annotation.LogBefore;
import ru.t1.taskmanager.aspect.annotation.LogExecutionTime;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.errorhandling.TaskNotFoundException;
import ru.t1.taskmanager.model.Task;
import ru.t1.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @LogBefore
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @LogAfterReturning
    public TaskDTO getTaskById(Long id) {
        return taskRepository.findById(id).map(this::toDTO).orElseThrow(() -> new TaskNotFoundException(id));
    }

    @LogAfterThrowing
    public TaskDTO createTask(TaskDTO taskDTO) {
        Task task = new Task(null, taskDTO.title(), taskDTO.description(), taskDTO.userId());
        return toDTO(taskRepository.save(task));
    }

    @LogExecutionTime
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        return taskRepository.findById(id).map(task -> {
            task.setTitle(taskDTO.title());
            task.setDescription(taskDTO.description());
            task.setUserId(taskDTO.userId());
            return toDTO(taskRepository.save(task));
        }).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    private TaskDTO toDTO(Task task) {
        return new TaskDTO(task.getId(), task.getTitle(), task.getDescription(), task.getUserId());
    }
}
