# Google Cloud Code Walkthrough 

Lets build a simple Google Cloud-optimized Spring Ai applicarion. go tto th espring intiiaalizr and choose the following dependencies: `web`, `iactuator`, `graalvm`, `data jdbc`, `gemini chat`, `gemini embeddinmgs`, `pgvector`, and `docker compose`. We chose Java 24, but you should the latest an dgreatest version of java at the time you read this. in particular, were going to use graalvm , which is a distribution of openjdk with some extra utilities that aloow us to compile our code into operating-system specific and archicture specific native images. these images operate in a fractin of the ram and startup in a fractio of the time compared to regualr JRE based applcation. theyre *much* more optimized. if u r using `sdkman.io` u can install it with just: `sdk install java 24-graalce`. weve also chosen apache maven as the buld tool, but you do you. 

the next thonig we need to worry about is which version of java we're runing. im using java 24. spring booto 3.x, on which spring ai is based, only requires java 17. but, really, the mimium wed recommend is java 21. jacva 21 gives you virtual threads which can be invaluable n the world of AI, as it makes the cost of these long netweokr equests basically nil. each time you call an LLM over the netweok via some REST endpinit? thats a network call. So, we're using Java 24 and you proabbyl should too. or whatever the latest version is. 

finally, we chose apache maven as the build tool.  you can do whatever yo like, but to keep things simple, the instructions in this blog will be in terms of apache maven.

<!-- we need to sort out the mess that is the maven dependencies  -->

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
#
spring.ai.vectorstore.pgvector.initialize-schema=true
#
spring.datasource.password=secret
spring.datasource.username=myuser
spring.datasource.url=jdbc:postgresql://localhost/mydatabase
```

let's go through 
