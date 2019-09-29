package com.example.test.fork.statemachine;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig
        extends EnumStateMachineConfigurerAdapter<States, Events> {


    @Autowired
    private StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister;


    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {

        states.withStates()
                .initial(States.NOT_STARTED)
                .fork(States.TOOLS_FORK)
                .state(States.TOOLS_TASKS)
                .join(States.TOOLS_JOIN)
                .and().withStates()
                    .parent(States.TOOLS_TASKS)
                    .initial(States.TASK_1)
                    .state(States.TASK_1_WORK)
                    .state(States.TASK_1_WORK_MORE)
                    .end(States.TASK_1_DONE)
                .and().withStates()
                    .parent(States.TOOLS_TASKS)
                    .initial(States.TASK_2)
                    .state(States.TASK_2_WORK)
                    .state(States.TASK_2_WORK_MORE)
                    .end(States.TASK_2_DONE)
                .and().withStates()
                .end(States.ERROR)
                .end(States.END);

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal()
                .source(States.NOT_STARTED).target(States.TOOLS_FORK)
                .event(Events.RUN)
                .and()
                .withFork().source(States.TOOLS_FORK).target(States.TOOLS_TASKS)

                    .and().withExternal().source(States.TASK_1).target(States.TASK_1_WORK).action(Util::logTransitionAndWait)
                    .and().withExternal().source(States.TASK_1_WORK).target(States.TASK_1_WORK_MORE).action(Util::logTransitionAndWait)
                    .and().withExternal().source(States.TASK_1_WORK_MORE).target(States.TASK_1_DONE).action(Util::logTransitionAndWait)

                    .and().withExternal().source(States.TASK_2).target(States.TASK_2_WORK).action(Util::logTransitionAndWait)
                    .and().withExternal().source(States.TASK_2_WORK).target(States.TASK_2_WORK_MORE).action(Util::logTransitionAndWait)
                    .and().withExternal().source(States.TASK_2_WORK_MORE).target(States.TASK_2_DONE).action(Util::logTransitionAndWait)
                .and()
                .withJoin().source(States.TOOLS_TASKS).target(States.TOOLS_JOIN)
                .and()
                .withExternal().source(States.TOOLS_JOIN).target(States.END);
    }

    @Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(3);
        return taskExecutor;
    }

    @Bean
    public StateMachineService<States, Events> stateMachineService(
            StateMachineFactory<States, Events> stateMachineFactory,
            StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister) {
        return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config)
            throws Exception {
        config
                .withConfiguration()
                .and()
                .withPersistence()
                .runtimePersister(stateMachineRuntimePersister)
        ;
    }

}

