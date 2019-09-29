package com.example.test.fork.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.util.Collections;

@EntityScan(basePackages = {"com.example.test.fork"})
@SpringBootApplication
public class Application implements CommandLineRunner {


    @Autowired
    Workflow workflow;

    @Override
    public void run(String... args) throws Exception {
        workflow.start();
    }

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(Application.class);

        app.setDefaultProperties(Collections
                .singletonMap("server.port", "8083"));
        app.run(args);
    }

}