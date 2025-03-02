package ru.t1.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.t1.taskmanager.model.TaskStatus;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class TaskDTO {
        private Long taskId;
        @NotBlank(message = "Заголовок (title) не может быть пустым")
        private String title;
        private String description;
        private Long userId;
        private TaskStatus status;
}
