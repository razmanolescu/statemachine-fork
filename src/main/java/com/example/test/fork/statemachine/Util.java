package com.example.test.fork.statemachine;

import lombok.extern.slf4j.Slf4j;

import org.springframework.statemachine.StateContext;

import java.util.concurrent.TimeUnit;

@Slf4j
public class Util {

    public static final long ACTION_SLEEP_MS = TimeUnit.SECONDS.toMillis(3);

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void logTransitionAndWait(StateContext<States, Events> context) {

        log.error("Transitioning from {} -> {}, thread {} ", context.getSource().getId().name(),
                  context.getTarget().getId().name(), Thread.currentThread().getName());
        sleep(ACTION_SLEEP_MS);
    }
}
