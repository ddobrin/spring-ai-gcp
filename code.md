# Google Cloud Code Walkthrough 

Lets build a simple Google Cloud-optimized Spring Ai applicarion. go tto th espring intiiaalizr and lets buld a new project called `google` (for the artificat id). choose the following dependencies: `web`, `iactuator`, `graalvm`, `data jdbc`, `gemini chat`, `gemini embeddinmgs`, `pgvector`, `mcp client`, and `docker compose`. 

### the build
were going to use graalvm , which is a distribution of openjdk with some extra utilities that aloow us to compile our code into operating-system specific and archicture specific native images. these images operate in a fractin of the ram and startup in a fractio of the time compared to regualr JRE based applcation. theyre *much* more optimized. if u r using `sdkman.io` u can install it with just: `sdk install java 24-graalce`. weve also chosen apache maven as the buld tool, but you do you. 

the next thonig we need to worry about is which version of java we're runing. im using java 24. spring booto 3.x, on which spring ai is based, only requires java 17. but, really, the mimium wed recommend is java 21. jacva 21 gives you virtual threads which can be invaluable n the world of AI, as it makes the cost of these long netweokr equests basically nil. each time you call an LLM over the netweok via some REST endpinit? thats a network call. So, we're using Java 24 and you proabbyl should too. or whatever the latest version is. 

finally, we chose apache maven as the build tool.  you can do whatever yo like, but to keep things simple, the instructions in this blog will be in terms of apache maven. hit `Generate` and you'll get a zip file ou can open in your IDE. let's modify the `pom.xml` to have some extra dependencies. 

add the following to th ebottom of your `<dependencyManagement>` block in your `pom.xml`:

```xml

            <dependency>
                <groupId>com.google.cloud</groupId>
                <artifactId>spring-cloud-gcp-dependencies</artifactId>
                <version>${spring-cloud-gcp.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            
            <dependency>
                <groupId>com.google.cloud</groupId>
                <artifactId>libraries-bom</artifactId>
                <version>26.59.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

```


### the configuration values 

let's look at some confiugration values that we'll need to do oyr work:

```
spring.application.name=google

# actuator
management.endpoints.web.exposure.include=*

# docker
spring.docker.compose.lifecycle-management=start_only

# vertex embedding
spring.ai.vertex.ai.embedding.project-id=joshlong
spring.ai.vertex.ai.embedding.location=us-central1

# vertex chat
spring.ai.vertex.ai.gemini.project-id=joshlong
spring.ai.vertex.ai.gemini.location=us-central1
spring.ai.vertex.ai.gemini.chat.options.model=gemini-2.5-pro-preview-05-06

# vector store
spring.ai.vectorstore.pgvector.initialize-schema=true

# database
spring.datasource.password=secret
spring.datasource.username=myuser
spring.datasource.url=jdbc:postgresql://localhost/mydatabase
```

let's go through each secrtion, one by one. 

in the first section we define the name of the spring. this isnt strctly speaking all that importatn. moving on...

in the next section well leverage the inbuilt dockr compose support. this awill automatically detect the `compose.yml` file and run the docker image for us before we get started. temember, the spring initalize will have generated this docker compose.yml file ffor us giving an instance of PostgreSQL.  we don't want sprin gboot to restart the container each time, though; postgresql isn't serverless! so well tell spring boot ro staert it only, and to leave it running once its up or detected running. 

in the next section we configure which gemini chat and embedding model we're to use from google cloud's gemeini. chat models are, well, theyre useful for free from human language text. embedding models, as we explained above, are what's used to convert arbitrary data into _vectors_. The nice thing is that google provides _tons_ of choices here. we've picked two suitable ones for our usecase. you do you.

scrolling down to the `vector store` section, we r using postgresql which in turn has a `vector` type. Weve got an implementation of SPring AI's `VectorStore` implementation that will use this vectgor store as thoughj it wete an actual vector store. We need to tell spring ai to initialzie a table (often called `vector_store`) for us for this to work. 

in the final section, we connect to the sql datrabase. do you need this, structly speaking? No. the docker compose support in spring boot will automatically connect for us. but if youre using intellij u can click on tese properties int he roperty file in the gutter of the code page na ditll automatically let you connect using the ides' databasee editor. 

### the database 
you'll have a sql database with no data. that wont do! if a database has no data, is it really a database? or just a _base_? anyway, well fix that by having spring boot initialize the database by runnning some sql commands and inserting some data. we'll defined two files, `src/main/resurces/schema.sql` and `/src/main/resources/data.sql`. first, `schema.sql`:


```sql
drop table if exists dog ;

create table if not exists dog
(
    id serial primary key,
    name text not null,
    description text not null,
    owner   text
);
```

simple enough. itll define a `dog` rable. now lets get some dogs in there, in `src/main/resources/data.sql`: 

```sql
INSERT INTO dog(id, name, description) values (97, 'Rocky', 'A brown Chihuahua known for being protective.');
INSERT INTO dog(id, name, description) values (87, 'Bailey', 'A tan Dachshund known for being playful.');
INSERT INTO dog(id, name, description) values (89, 'Charlie', 'A black Bulldog known for being curious.');
INSERT INTO dog(id, name, description) values (67, 'Cooper', 'A tan Boxer known for being affectionate.');
INSERT INTO dog(id, name, description) values (73, 'Max', 'A brindle Dachshund known for being energetic.');
INSERT INTO dog(id, name, description) values (3, 'Buddy', 'A Poodle known for being calm.');
INSERT INTO dog(id, name, description) values (93, 'Duke', 'A white German Shepherd known for being friendly.');
INSERT INTO dog(id, name, description) values (63, 'Jasper', 'A grey Shih Tzu known for being protective.');
INSERT INTO dog(id, name, description) values (69, 'Toby', 'A grey Doberman known for being playful.');
INSERT INTO dog(id, name, description) values (101, 'Nala', 'A spotted German Shepherd known for being loyal.');
INSERT INTO dog(id, name, description) values (61, 'Penny', 'A white Great Dane known for being protective.');
INSERT INTO dog(id, name, description) values (1, 'Bella', 'A golden Poodle known for being calm.');
INSERT INTO dog(id, name, description) values (91, 'Willow', 'A brindle Great Dane known for being calm.');
INSERT INTO dog(id, name, description) values (5, 'Daisy', 'A spotted Poodle known for being affectionate.');
INSERT INTO dog(id, name, description) values (95, 'Mia', 'A grey Great Dane known for being loyal.');
INSERT INTO dog(id, name, description) values (71, 'Molly', 'A golden Chihuahua known for being curious.');
INSERT INTO dog(id, name, description) values (65, 'Ruby', 'A white Great Dane known for being protective.');
INSERT INTO dog(id, name, description) values (45, 'Prancer', 'A silly, goofy dog who slobbers all over everyone');
```

Nice! tgis will get run on every retart. were taking care to avoid duplicate data bu just dropping the table on every restart and re-inserting the same rows. if this were a real databsae, we might use an _upsert_, which PostgresSQL supports with its `insert on conflict... do` syntax. 

well use Spring Data JDBC to make shrot work of building a daga access repository and an entity, called `Dog`, to model the data in the repository.

add the following to the bottom of `GoogleApplication.java`, underneath `GoogleApplication`.

```java
interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

record Dog(@Id int id, String name, String owner, String description) {
}

```



Now yu've got a working data access repository that will query, update, etc., data in our `dog` table. 

### the assistant

now for the meat.. or the _bone_ of the matter, as I imagine our pal Prancer might say! 


let's build an `AssistantController` class, again at the  ottom of the `GoogleApplication.java` class:


```java



@Controller
@ResponseBody
class AssistantController {

    private final ChatClient ai;

    private final Map<String, PromptChatMemoryAdvisor> advisors = new ConcurrentHashMap<>();

    AssistantController(ChatClient.Builder ai) {

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Mountain View, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        this.ai = ai
                .defaultSystem(system)
                .build();
    }
    
    // ...

}
```

let's walk through what's going on in the constructor first. We need a Sprign AI `ChatClient`. The `ChatClient` is your one-stop-shop for all your chat model interactions. It in turn depends on the (autoconfigured) `ChatModel` that talks to, in this case, Google's legednary Gemini LLM. You typically only have one or a very small number of `ChatModel`s configured in the application, but you'll have many `ChatClients`, with different defaults and scenarios configured appropriately. You crate  a new `ChatClient` by using the the `ChatClient.Builder`, which were injecting into the constructor.

were going to setup an http endpoint, `/dogs/{dogId}/inquire`, to which we can address requets about dogs in the shelter. 

we want all requsets to be to be handled by the ai model, and we want to give it direction about what the goal is, what it's meant to do. this overriding dirrective is called a _system prompt_. it changes how the AI model responds to inquries, it frames the question and governs the response. in this constructor, we have a system prompt that telsl the model that they are meant to act like an employee at a ficticious dog adoption agency called _Pooch Palace_. We tell the model that if for whatever reason there's no dogs available, to respond politely that there are no dogs.

let's look at our first http endpoint, `/dogs/{dogId}/inquire`. add the following method to the `AssistantController` controller:

```java
    @GetMapping("/{user}/inquire")
    String inquire(@PathVariable String user,
                   @RequestParam String question) {
        var c = MessageWindowChatMemory.builder().chatMemoryRepository(new InMemoryChatMemoryRepository()).build();
        var advisor = this.advisors
                .computeIfAbsent(user, _ -> PromptChatMemoryAdvisor.builder(c).build());
        return this.ai
                .prompt()
                .advisors(advisor)
                .user(question)
                .call()
                .content();
    }

```

Remember, LLMs dont have memory. you send a request, and it may not remember. you need to _remind_ it of what's been said on every prior request.
in this method, you can see w'ere defining a `PromptChatMemoryAdvisor` which is an _advisor_. In Spring AI, an advisor pre- and post-processes requests destined to a model. this advisor keeps track of meessages destined to a user and then re-submit that transcript accordingly. the requests to the endpoint are expected to transmit a `question` parameter. we use the `user` path variable to distinguish who is making the requ4est. we could just as easily use a Spring Security `Principal#getName()` to do the work. we store the adivsor in a concurrent map under a key tied to each user.

with these changes this endpoint will work. try the following requests out: 

```
http :8080/jlong/inquire question=="my name is Bob."
```

and 

```
http :8080/dogs/inquire question=="what's my name?"
```

it should confirm that your name is Bob. and it might even try to keep yo on track in adopting a dog. 

fine. let's see what else it can do. ask it:

```shell
http :8080/dogs/inquire question=="do you have any silly dogs?" 
```

it will respond that it doesn't have any information about dogs in any Pooch Palace location, and maybe encourage you to check the local listings. The problem is that it doesn't have access to the data. So let's give it access! but we shouldn't give it _all_ of the data. i mean.. we _could_, perhaps, give it all the data. there's only records in this prticular database, after all! did you know that google gemini 2.5 pro has a 1 _million_ token size? all LLMs have this concept of _tokens_ - an approximatino for the amount of data consumed and produced by an LLM. If you're using a local model like  the open Gemma model, which you cna run locally, then the only cost to runnning a model is the complexity and CPU cost. If you run a hosted, and vastly superior model like Gemini, then there's also an a dollars-and-cents cost. so, even thgough you _could_ send all the data along your request to one of these models, you should try to limit the data you send. It's the principle of the thing! so we'll store everything in a vector store and the find things that might be potentially germaine to the content we're looking for, and finally we'll transmit only that data to the model for consideration. this process, of sourcing data from a database and then using that date to inform the response produced by a model is called _retreival augmented generation_ (or R.A.G.). Let's modify teh code to supprt it. 

In the constructor the controller, add a parameter of type `VectorStore vectorStore`. in the constructor, we'll read all the data from the `dog` table and write it all out to the `VectorStore` implementatino backed by PostgreSQL`. add the following to the constructor, at the very top: 

```java

        if (db.sql("select count(*) from vector_store").query(Integer.class).single().equals(0)) {
            repository.findAll().forEach(d -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        d.id(), d.name(), d.description()
                ));
                vectorStore.add(List.of(dogument));
            });
        }

```

all we're doing is checking that there are no existing records written to the `vector\_store` table and if there are, writing those records as Spring AI `Document` records. Were encoding the data as a string (with no particular schema). we're halfway there! the only thing to do now is to tell the `ChatClient` to first consult the `VectorStore` for relevant data that it include in the body of the request to the model. We'll do this, again, with an _advisors_. Add the following to the definition of the `Chatclient`:

```java
        this.ai = ai
                ...
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
                ...
```

Run the program and ask again: 

```shell
http :8080/jlong/inquire question=="do you have any silly dogs?" 
```

this time, it should respond that indeed there's a silly dog named _Prancer_ (hi, buddy!) in the shlter who might be just the dog for us! Huzzah! 

Well, anturallt the next thing we'll want to do is adopt this doggo of joy. but when might we stop by to adopt and pickup this dog? we'll need to connect our LLM to the patent-pending, world-leading schedulign algorithm. here's the implementation of the scheduling logic. 


```java

@Component
class DogAdoptionsScheduler {


    @Tool(description = "schedule an appointment to pickup or adopt a dog at a Pooch Palace location")
    String scheduleAppointment(@ToolParam(description = "the id of the dog") String dogId,
                               @ToolParam(description = "the name of the dog") String dogName) throws Exception {
        var i = Instant
                .now()
                .plus(3, ChronoUnit.DAYS)
                .toString();
        System.out.println("scheduled appointment for " +  i  +
                " for dog " + dogName + " with id " + dogId + ".");
        return i;
    }
}

```

ok, so maybe it's not _that_ involved! But noe that it'll respond with an invariant of three days. Anyway, thats the logic. Remember when your parents told you to _use your words_ when frustrated? that's true here, too. we've annotated the method with Spring AI's `@Tool` and `@ToolParam` annotations. these annotation furnish descriptions that the model will use, along with the shape of the methods, to intuit whether they might be of use. let's tell the model these tools are available, too. inject the newly defined `DogAdoptionScheduler scheduler` into the constructor of the controller and then add the following to the definition of the `ChatClient`: 


```java
  this.ai = 
     ...
     .defaultTools(scheduler)
     ...

```


restrt the program and let's try it out. ask about prancer, as above, and then ask:

```java
http :8080/dogs/jlong/inquire question=="fantastic. when can i schedule an appointment to pickup Prancer from the San Francisco location?"
```

remember, the algorithm is designed to return a date three days in the future. if it does, you can bet it invoked our custom tool. confirm as much by lookig at the logs on the console. 

nice! we've now successfully given ou model access to the data and business logic of our service.  

this service is all written in spring and using spring ai. but obviously, there are other languages, and indeed other services out there, and they may want to leverage this patent-pending indfustry leading schedulign algorithm. how could we extrat that functinoality and make it a tool available to all interactions with th eLLM? well use [Model Context Prootcol ](https://modelcontextprotocol.io). This protocol was first designed by Anthropic and provides an easy way for any LLM to assimialte tools into their toolbx, no matter what language in which they were written. 

Let's hit the [Spring Initializr](https://start.spring.io) again. Generate  a new project named `scheduler`, adding `Web`, and `MCP Server` as depdendencies and choosing Java 24 and Maven, as before. Unzip the resulting `.zip` file and open it in your iDE, as before. 

we'll transplant the scheduler component from the `google` project to this new project. cut and paste for the win! specicy that this new service start on a different port; add `server.port=8081` to `application.properties`. then, we'll need to tell Spring AI which tools we want to export as MCP endpoints. Define the following bean in `SchedulerApplication`:


```java

    @Bean
    MethodToolCallbackProvider dogMethodToolCallbackProvider(DogAdoptionsScheduler scheduler) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(scheduler)
                .build();
    }
```

start the resulting application up. 

return to the assistant and let's modify it to work with a remote service in lieu of the component that it used before. change the controller of `AssistantController` to inject a `McpSyncClient client` instead of the  `DogAdoptionsScheduler` that was there before. modify the `ai` definition accordingly:

```java
        ...
        this.ai = ai
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(client))

        ... 
```


the constructors expecting a reference to an `McpSyncClient`, which we've yet to define. let's remedy that. add the following bena definition to the `GoogleApplication.java` class:

```java

    @Bean
    McpSyncClient mcpSyncClient() {
        var mcp = McpClient
                .sync( HttpClientSseClientTransport.builder("http://localhost:8081").build())
                .build();
        mcp.initialize();
        return mcp;
    }
```

Restart the application and try the interaction, again. 

```shell

  http :8080/dogs/jlong/inquire question=="do you have any silly dogs?" 
```

and 

```shell
http :8080/dogs/jlong/inquire question=="do you have any silly dogs?"
```

and

```shell
http :8080/dogs/jlong/inquire question=="fantastic. when can i schedule an appointment to pickup Prancer from the San Francisco location?"
```

you should see this time that the model responds basically the same as last time, except that the request was handle in thew newly minted `scheduler` component, not in the `google` module. 

nice! 

and with that, we've built a fully functioning, scalable, and production worthy Spring AI application powered by Google Cloud. 
