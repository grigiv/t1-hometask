package ru.t1.taskmanager.service;

import ru.t1.grigiv.starter.aspect.annotation.LogAfterReturning;
import ru.t1.grigiv.starter.aspect.annotation.LogAfterThrowing;
import ru.t1.grigiv.starter.aspect.annotation.LogBefore;
import ru.t1.grigiv.starter.aspect.annotation.LogExecutionTime;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.errorhandling.TaskNotFoundException;
import ru.t1.taskmanager.event.TaskProducer;
import ru.t1.taskmanager.model.Task;
import ru.t1.taskmanager.model.TaskStatus;
import ru.t1.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskProducer taskProducer;

    public TaskService(TaskRepository taskRepository, TaskProducer taskProducer) {
        this.taskRepository = taskRepository;
        this.taskProducer = taskProducer;
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
        Task task = new Task(null, taskDTO.getTitle(), taskDTO.getDescription(), taskDTO.getUserId(), TaskStatus.NEW);
        return toDTO(taskRepository.save(task));
    }

    @LogExecutionTime
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
        taskDTO.setTaskId(id);
        TaskStatus oldStatus = task.getStatus();
        Task updatedTask = toEntity(taskDTO);
        TaskStatus newStatus = updatedTask.getStatus();
        taskRepository.save(updatedTask);
        if (!oldStatus.equals(newStatus)) {
            taskProducer.send(taskDTO);
        }
        return toDTO(updatedTask);
    }


    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    private TaskDTO toDTO(Task task) {
        return new TaskDTO(task.getTaskId(), task.getTitle(), task.getDescription(), task.getUserId(), task.getStatus());
    }

    private Task toEntity(TaskDTO taskDTO) {
        return new Task(taskDTO.getTaskId(), taskDTO.getTitle(), taskDTO.getDescription(), taskDTO.getUserId(), taskDTO.getStatus());
    }
}
