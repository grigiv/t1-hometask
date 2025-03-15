package ru.t1.taskmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.errorhandling.TaskNotFoundException;
import ru.t1.taskmanager.event.TaskProducer;
import ru.t1.taskmanager.model.Task;
import ru.t1.taskmanager.model.TaskStatus;
import ru.t1.taskmanager.repository.TaskRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskProducer taskProducer;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    void setUp() {
        task = new Task(1L, "Test Task", "Description", 101L, TaskStatus.NEW);
        taskDTO = new TaskDTO(1L, "Test Task", "Description", 101L, TaskStatus.NEW);
    }

    @Test
    void TaskService_getAllTasks_ShouldReturnTaskList() {
        when(taskRepository.findAll()).thenReturn(List.of(task));

        List<TaskDTO> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void TaskService_getTaskById_ShouldReturnTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskDTO foundTask = taskService.getTaskById(1L);

        assertNotNull(foundTask);
        assertEquals(1L, foundTask.getTaskId());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void TaskService_getTaskById_ShouldThrowException_WhenTaskNotFound() {
        when(taskRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(2L));
        verify(taskRepository, times(1)).findById(2L);
    }

    @Test
    void TaskService_createTask_ShouldSaveAndReturnTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDTO createdTask = taskService.createTask(taskDTO);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void TaskService_updateTask_ShouldUpdateTaskAndSendEvent_WhenStatusChanges() {
        Task updatedTask = new Task(1L, "Updated Task", "New Description", 101L, TaskStatus.IN_PROGRESS);
        TaskDTO updatedTaskDTO = new TaskDTO(1L, "Updated Task", "New Description", 101L, TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskDTO result = taskService.updateTask(1L, updatedTaskDTO);

        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskProducer, times(1)).send(updatedTaskDTO); // Убеждаемся, что событие отправлено
    }

    @Test
    void TaskService_updateTask_ShouldNotSendEvent_WhenStatusUnchanged() {
        Task updatedTask = new Task(1L, "Updated Task", "New Description", 101L, TaskStatus.NEW);
        TaskDTO unchangedStatusTaskDTO = new TaskDTO(1L, "Updated Task", "New Description", 101L, TaskStatus.NEW);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        taskService.updateTask(1L, unchangedStatusTaskDTO);

        verify(taskProducer, never()).send(any(TaskDTO.class)); // Убеждаемся, что событие НЕ отправлено
    }

    @Test
    void TaskService_deleteTask_ShouldCallRepository() {
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }
}
