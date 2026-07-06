package com.wooseok.notifyflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("notification-events")
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationEventsDlqTopic() {
        return TopicBuilder.name("notification-events-dlq")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
