package ru.t1.taskmanager.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import ru.t1.taskmanager.dto.TaskDTO;
import ru.t1.taskmanager.event.MessageDeserializer;


import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConfig {
    @Value("t1-task-updated")
    private String groupId;
    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;
    @Value("${spring.kafka.max.partition.fetch.bytes: 300000}")
    private String maxPartitionFetchBytes;
    @Value("${spring.kafka.max.poll.records:1}")
    private String maxPollRecords;
    @Value("t1_task_status_updated")
    private String taskStatusUpdatedTopic;

    // Настройка продюсера
    @Bean
    public ProducerFactory<String, TaskDTO> producerTaskFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("task")
    public KafkaTemplate<String, TaskDTO> kafkaTemplate(ProducerFactory<String, TaskDTO> producerTaskFactory) {
        KafkaTemplate<String, TaskDTO> kafkaTemplate = new KafkaTemplate<>(producerTaskFactory());
        kafkaTemplate.setDefaultTopic(taskStatusUpdatedTopic);
        return new KafkaTemplate<>(producerTaskFactory());
    }

    @Bean
    public ConsumerFactory<String, TaskDTO> consumerListenerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); //консюмер группа
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); //кто будет десериализовать ключ
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MessageDeserializer.class); //кто будет десериализовать value, чтобы отловить ошибки
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "ru.t1.taskmanager.dto.TaskDTO"); // во что мапим
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes); // максимальный размер сообшения
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords); // сколько сообщений прочитать за один раз и коммит одного оффсета
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE); // будет ли консюмер автоматически подтверждать смещение после обработки сообщения
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // начинать с раннего сообщения
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, MessageDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

        DefaultKafkaConsumerFactory factory = new DefaultKafkaConsumerFactory<String, TaskDTO>(props);
        factory.setKeyDeserializer(new StringDeserializer());
        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, TaskDTO> kafkaListenerContainerFactory(@Qualifier("consumerListenerFactory") ConsumerFactory<String, TaskDTO> consumerFactory)  {
        ConcurrentKafkaListenerContainerFactory<String, TaskDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factoryBuilder(consumerFactory, factory);
        return factory;
    }

    private <T> void factoryBuilder(ConsumerFactory<String, T> consumerFactory, ConcurrentKafkaListenerContainerFactory<String, T> factory) {
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setPollTimeout(5000);
        factory.getContainerProperties().setMicrometerEnabled(true);
        factory.setCommonErrorHandler(errorHandler());
    }

    private CommonErrorHandler errorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(1000, 3));
        errorHandler.addNotRetryableExceptions(IllegalStateException.class);
        errorHandler.setRetryListeners(((record, ex, deliveryAttempt) ->
                log.error("RetryListeners message = {}, offset = {}, deliveryAttempt = {}", record, ex, deliveryAttempt)));
        return errorHandler;
    }
}