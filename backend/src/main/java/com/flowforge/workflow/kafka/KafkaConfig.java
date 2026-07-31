package com.flowforge.workflow.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_TASKS_EXECUTE = "flowforge.tasks.execute";

    @Bean
    public NewTopic tasksExecuteTopic() {
        return TopicBuilder.name(TOPIC_TASKS_EXECUTE)
                .partitions(3)
                .replicas(1) // Adjust for production
                .build();
    }
}
