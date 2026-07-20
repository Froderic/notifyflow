package com.wooseok.notifyflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notifyFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NotifyFlow API")
                        .description("Event-driven notification platform — " +
                                "Kafka-based async pipeline with rate limiting, " +
                                "deduplication, retry logic, and dead letter queue")
                        .version("1.0.0"));
    }
}