package com.example.cornel.demo.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.util.ObjectUtils;

import java.util.EnumSet;
import java.util.Map;

@Configuration
@EnableStateMachine
public class StateMachineConfig
        extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states
                .withStates()
                .initial(States.READY)
                .fork(States.FORK)
                .state(States.TASKS)
                .join(States.JOIN)
                .choice(States.CHOICE)
                .state(States.ERROR)
                .and()
                .withStates()
                .parent(States.TASKS)
                .initial(States.T1)
                .end(States.T1E)
                .and()
                .withStates()
                .parent(States.TASKS)
                .initial(States.T2)
                .end(States.T2E)
                .and()
                .withStates()
                .parent(States.TASKS)
                .initial(States.T3)
                .end(States.T3E)
                .and()
                .withStates()
                .parent(States.ERROR)
                .initial(States.AUTOMATIC)
                .state(States.AUTOMATIC, automaticAction(), null)
                .state(States.MANUAL)
                .and()
                .withStates()
                .state(States.NEXT1)
            .and()
            .withStates()
            .state(States.NEXT2)
                .end(States.END);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(States.READY).target(States.FORK)
                .event(Events.RUN)
                .and()
                .withFork()
                .source(States.FORK).target(States.TASKS)
                .and()
                .withExternal()
                .source(States.T1).target(States.T1E)
                .and()
                .withExternal()
                .source(States.T2).target(States.T2E)
                .and()
                .withExternal()
                .source(States.T3).target(States.T3E)
                .and()
                .withJoin()
                .source(States.TASKS).target(States.JOIN)
                .and()
                .withExternal()
                .source(States.JOIN).target(States.CHOICE)
                .and()
                .withChoice()
                .source(States.CHOICE)
                .first(States.ERROR, tasksChoiceGuard())
                .last(States.NEXT1)
            .and()
            .withExternal()
            .source(States.NEXT1)
            .target(States.NEXT2)
            .event(Events.NEXT)
                .and()
                .withExternal()
                .source(States.ERROR).target(States.READY)
                .event(Events.CONTINUE)
                .and()
                .withExternal()
                .source(States.AUTOMATIC).target(States.MANUAL)
                .event(Events.FALLBACK)
                .and()
                .withInternal()
                .source(States.MANUAL)
                .action(fixAction())
                .event(Events.FIX)
;
    }

    @Bean
    public Guard<States, Events> tasksChoiceGuard() {
        return context -> {
            Map<Object, Object> variables = context.getExtendedState().getVariables();
            return !(ObjectUtils.nullSafeEquals(variables.get("T1"), true)
                    && ObjectUtils.nullSafeEquals(variables.get("T2"), true)
                    && ObjectUtils.nullSafeEquals(variables.get("T3"), true));
        };
    }

    @Bean
    public Action<States, Events> automaticAction() {
        return context -> {
            Map<Object, Object> variables = context.getExtendedState().getVariables();
            if (ObjectUtils.nullSafeEquals(variables.get("T1"), true)
                    && ObjectUtils.nullSafeEquals(variables.get("T2"), true)
                    && ObjectUtils.nullSafeEquals(variables.get("T3"), true)) {
                context.getStateMachine().sendEvent(Events.CONTINUE);
            } else {
                context.getStateMachine().sendEvent(Events.FALLBACK);
            }
        };
    }

    @Bean
    public Action<States, Events> fixAction() {
        return context -> {
            Map<Object, Object> variables = context.getExtendedState().getVariables();
            variables.put("T1", true);
            variables.put("T2", true);
            variables.put("T3", true);
            context.getStateMachine().sendEvent(Events.CONTINUE);
        };
    }

    @Bean
    public Tasks tasks() {
        return new Tasks();
    }

    @Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        return taskExecutor;
    }

//    @Bean
//    public StateMachineService<States, Events> stateMachineService(
//            StateMachineFactory<States, Events> stateMachineFactory,
//            StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister) {
//        return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
//    }

//end::snippetAE[]

}

