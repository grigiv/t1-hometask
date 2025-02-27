package ru.t1.taskmanager.errorhandling;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ApiError(
        String error,
        String message,
        LocalDateTime timestamp) {
}
