package com.wooseok.notifyflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotifyflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotifyflowApplication.class, args);
    }
}
