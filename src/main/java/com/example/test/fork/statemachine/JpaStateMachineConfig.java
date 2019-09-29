package com.example.test.fork.statemachine;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.data.jpa.JpaPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.jpa.JpaStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

@Slf4j
@Configuration
public class JpaStateMachineConfig {

    @Bean
    public StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister(
        JpaStateMachineRepository jpaStateMachineRepository) {

        return new JpaPersistingStateMachineInterceptor<States, Events, String>(jpaStateMachineRepository) {

            @Override
            public void write(StateMachineContext<States, Events> context, String contextObj) throws Exception {
                log.debug("Saving state '{}' on sm '{}' - thread {}", context.getState().name(), contextObj,  Thread.currentThread().getName());
                super.write(context, contextObj);
            }

        };
    }

}
