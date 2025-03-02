package ru.t1.taskmanager.util;

import org.springframework.stereotype.Component;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.model.Task;

@Component
public class TaskMapper {
    public static TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO(
                task.getTaskId(),
                task.getTitle(),
                task.getDescription(),
                task.getUserId(),
                task.getStatus()
        );
        return dto;
    }

    public static Task toEntity(TaskDTO dto) {
        Task task = new Task();
        task.setTaskId(dto.getTaskId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setUserId(dto.getUserId());
        task.setStatus(dto.getStatus());
        return task;
    }
}
