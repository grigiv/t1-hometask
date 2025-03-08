package ru.t1.taskmanager.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class TaskProducer {
    private final KafkaTemplate kafkaTemplate;
    @Value("t1_task_status_updated")
    private String topicName;

    public void send(Object o) {
        try {
            kafkaTemplate.send(topicName, o);
            kafkaTemplate.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
