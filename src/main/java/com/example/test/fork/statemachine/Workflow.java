package com.example.test.fork.statemachine;

import static com.example.test.fork.statemachine.Util.ACTION_SLEEP_MS;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

@Slf4j
@Controller
@WithStateMachine
public class Workflow {

  private static final long MAIN_THREAD_WAIT_MS = ACTION_SLEEP_MS * 3;
  private static final String SM_DEFAULT_NAME = "test";
  private StateMachine<States, Events> stateMachine;

  @Value("${statemachine.persist.force}")
  private boolean forcePersist;

  @Autowired
  private StateMachineService<States, Events> sms;

  @Autowired
  private StateMachinePersist<States, Events, String> stateMachinePersist;

  @Autowired
  EntityManager entityManager;

  public void start() throws Exception {

    printDbState("BEFORE");
    StateMachinePersister<States, Events, String> persister = new DefaultStateMachinePersister<>(stateMachinePersist);
    stateMachine = sms.acquireStateMachine(SM_DEFAULT_NAME, false);

    if (stateMachine.isComplete() || States.NOT_STARTED.equals(stateMachine.getState().getId())) {
      log.debug("(Re)starting SM");
      if (stateMachine.getState() != null && States.END.equals(stateMachine.getState().getId())) {
        stateMachine.getStateMachineAccessor()
            .doWithRegion(access ->
                              access.resetStateMachine(new DefaultStateMachineContext<>(stateMachine.getInitialState().getId(), null, null, stateMachine.getExtendedState(), null, SM_DEFAULT_NAME))
            );
      }
      stateMachine.start();
      Thread.sleep(100);
      stateMachine.sendEvent(Events.RUN);

    } else {
      if (forcePersist) {
        persister.restore(stateMachine, SM_DEFAULT_NAME);
      }
      stateMachine.start();
    }

    log.warn("Completed  {}, with state {}, error {}", stateMachine.isComplete(), stateMachine.getState(), stateMachine.hasStateMachineError());

    Thread.sleep(MAIN_THREAD_WAIT_MS);
    if (forcePersist) {
      persister.persist(stateMachine, SM_DEFAULT_NAME);
    }

    printDbState("AFTER");
    System.exit(1);

  }

  private void printDbState(String loggingPrefix) {
    Map<String, String> stateMap = new HashMap<>();
    List<Object[]> sms = entityManager.createNativeQuery(
        "SELECT * FROM state_machine")
        .getResultList();

    sms.forEach(e -> stateMap.put(e[0].toString(), e[1].toString()));

    log.error("[{}] Databases SMs states : {}", loggingPrefix, stateMap);
  }

}

