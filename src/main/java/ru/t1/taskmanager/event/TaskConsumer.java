package ru.t1.taskmanager.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.service.NotificationService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class TaskConsumer {

    private final NotificationService notificationService;

    @KafkaListener(id = "t1-task-updated",
                    topics = "t1_task_status_updated",
                    containerFactory = "kafkaListenerContainerFactory")
    public void listener(@Payload List<TaskDTO> messageList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.debug("Task consumer: обработка новых сообщений");
        try {
            messageList.forEach(taskDTO -> {
            Context context = new Context();
            context.setVariable("newStatus", taskDTO.getStatus());
            notificationService.send(
                    "grigorieviv87@yandex.ru",
                    "Статус задачи изменился",
                    "EmailConfirmation.html", context);
            });
        }
        finally {
            ack.acknowledge();
        }
        log.debug("Task consumer: записи обработаны");
    }
}
