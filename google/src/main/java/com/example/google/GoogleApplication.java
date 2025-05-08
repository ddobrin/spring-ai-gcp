package com.example.google;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class GoogleApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleApplication.class, args);
    }

}

record Appointment(String date) {
}

// TBD MCP?
@Component
class DogAdoptionsScheduler {

    @Tool(description = "schedule an appointment to pickup or adopt a dog at a Pooch Palace location")
    Appointment scheduleAppointment(@ToolParam(description = "the id of the dog") String dogId,
                                    @ToolParam(description = "the name of the dog") String dogName) {
        var i = Instant
                .now()
                .plus(3, ChronoUnit.DAYS)
                .toString();
        var a = new Appointment(i);
        System.out.println("scheduled appointment for " + a.date() + " for dog " + dogName + " with id " + dogId + ".");
        return a;
    }
}


interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}

@Controller
@ResponseBody
class AssistantController {

    private final ChatClient ai;

    private final Map<String, PromptChatMemoryAdvisor> advisors = new ConcurrentHashMap<>();

    AssistantController(JdbcClient db, DogRepository repository, DogAdoptionsScheduler scheduler, ChatClient.Builder ai, VectorStore vectorStore) {

        if (db.sql("select count(*) from vector_store").query(Integer.class).single().equals(0)) {
            repository.findAll().forEach(d -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        d.id(), d.name(), d.description()
                ));
                vectorStore.add(List.of(dogument));
            });
        }

        // http :8080/jlong/inquire question=="do you have any neurotic dogs?"
        // http :8080/jlong/inquire question=="fantastic. when can i schedule an appointmnet to pick up Prancer from the Mountain View location?"

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Mountain View, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        this.ai = ai
                .defaultSystem(system)
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                .defaultTools(scheduler)
                .build();
    }

    @GetMapping("/{user}/inquire")
    String inquire(@PathVariable String user, @RequestParam String question) {
        var advisor = this.advisors
                .computeIfAbsent(user, _ -> PromptChatMemoryAdvisor.builder(new InMemoryChatMemory()).build());
        // tbd changes in > m8
        return this.ai
                .prompt()
                .advisors(advisor)
                .user(question)
                .call()
                .content();
    }
}