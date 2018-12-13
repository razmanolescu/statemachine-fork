package com.example.cornel.demo.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.example")
public class Application implements CommandLineRunner {


    @Autowired
    Workflow workflow;

    @Override
    public void run(String... args) throws Exception {
        workflow.init();



    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}