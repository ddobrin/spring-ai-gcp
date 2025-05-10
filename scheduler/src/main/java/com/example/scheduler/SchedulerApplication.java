package com.example.scheduler;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
public class SchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

    @Bean
    MethodToolCallbackProvider dogMethodToolCallbackProvider(DogAdoptionsScheduler scheduler) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(scheduler)
                .build();
    }
}

record Appointment(String date) {
}


// TBD MCP?

@Component
class DogAdoptionsScheduler {

    @Tool(description = "schedule an appointment to pickup or adopt a dog at a Pooch Palace location")
    String scheduleAppointment(@ToolParam(description = "the id of the dog") String dogId,
                               @ToolParam(description = "the name of the dog") String dogName) throws Exception {
        var i = Instant
                .now()
                .plus(3, ChronoUnit.DAYS)
                .toString();
        System.out.println("scheduled appointment for " + i +
                " for dog " + dogName + " with id " + dogId + ".");
        return i;
    }
}
