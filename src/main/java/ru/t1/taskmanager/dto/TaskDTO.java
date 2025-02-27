package ru.t1.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TaskDTO(
        Long id,
        @NotBlank(message = "Заголовок (title) не может быть пустым")
        String title,
        String description,
        Long userId) {
}
