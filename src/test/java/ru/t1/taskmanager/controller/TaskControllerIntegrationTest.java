package ru.t1.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.model.Task;
import ru.t1.taskmanager.model.TaskStatus;
import ru.t1.taskmanager.repository.TaskRepository;
import ru.t1.taskmanager.service.TaskService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaTemplate kafkaTemplate;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    public void TaskController_getAllTasks_ShouldReturnTaskList() throws Exception {
        // Arrange
        Task task1 = new Task(null, "Test Task1", "Description1", 101L, TaskStatus.NEW);
        Task task2 = new Task(null, "Test Task2", "Description2", 102L, TaskStatus.NEW);
        taskRepository.saveAll(List.of(task1, task2));

        // Act & Assert
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Test Task1"));
    }

    @Test
    void TaskController_getTaskById_ShouldReturnTask() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO(null, "Test Task1", "Description", 101L, TaskStatus.NEW);
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskDTO createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDTO.class
        );

        // Act & Assert
        mockMvc.perform(get("/tasks/" + createdTask.getTaskId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task1"));
    }

    @Test
    void TaskController_getTaskById_ShouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/tasks/111"))
                .andExpect(status().isNotFound());
    }


    @Test
    void TaskController_createTask_ShouldCreateTask() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO(null, "Test Task1", "Description", 101L, TaskStatus.NEW);

        // Act
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskDTO createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDTO.class
        );

        // Assert
        mockMvc.perform(get("/tasks/" + createdTask.getTaskId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task1"));
    }

    @Test
    void TaskController_updateTask_ShouldUpdateTaskAndSendEvent_WhenStatusChanges() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO(null, "Test Task1", "Description", 101L, TaskStatus.NEW);
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskDTO createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDTO.class
        );

        // Act & Assert
        TaskDTO updatedTaskDTO = new TaskDTO(null, "Updated Task1", "Updated Description1", 101L, TaskStatus.IN_PROGRESS);
        mockMvc.perform(put("/tasks/" + createdTask.getTaskId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task1"));
        verify(kafkaTemplate, times(1)).send(any(), any());
    }

    @Test
    void TaskController_updateTask_ShouldNotSendEvent_WhenStatusUnchanged() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO(null, "Test Task1", "Description", 101L, TaskStatus.NEW);
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskDTO createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDTO.class
        );

        // Act & Assert
        TaskDTO updatedTaskDTO = new TaskDTO(null, "Updated Task1", "Updated Description1", 101L, TaskStatus.NEW);
        mockMvc.perform(put("/tasks/" + createdTask.getTaskId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedTaskDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task1"));
        verify(kafkaTemplate, times(0)).send(any(), any());
    }

    @Test
    void TaskController_deleteTask_ShouldDeleteTask() throws Exception {
        // Arrange
        TaskDTO taskDTO = new TaskDTO(null, "Test Task1", "Description", 101L, TaskStatus.NEW);
        MvcResult createResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        TaskDTO createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskDTO.class
        );

        // Act
        mockMvc.perform(delete("/tasks/" + createdTask.getTaskId()))
                .andExpect(status().isNoContent());

        // Assert
        assertEquals(0, taskRepository.count());
        assertTrue(taskRepository.findById(createdTask.getTaskId()).isEmpty());
    }

}

