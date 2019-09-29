# Setup

To setup the example, you must have:
 * docker and docker-compose / local Postgres DB
 * Java 8 or higher JDK
 * IDE with Lombok support
 * Maven 3

# State machine diagram
```
                    +--------------+
                    |              |
                    |  NOT_STARTED |
                    |              |
                    +------+-------+
                           |  RUN
                           v
                    +------+-------+
                    |              |
                    |  TASKS_FORK  |
                    |              |
                    +------+-------+
                           |
+--------------------------v----------------------------------+
|                                                  TOOL_TASKS |
| +----------------+                 +-------------------+    |
| |                |                 |                   |    |
| |                |                 |                   |    |
| |    TASK_1      |                 |      TASK_2       |    |
| |                |                 |                   |    |
| +-------+--------+                 +----------+--------+    |
|         |                                     |             |
| +-------v--------+                 +----------v--------+    |
| |                |                 |                   |    |
| |  TASK_1_WORK   |                 |    TASK_2_WORK    |    |
| |                |                 |                   |    |
| |                |                 |                   |    |
| +-------+--------+                 +----------+--------+    |
|         |                                     |             |
| +-------v--------+                 +----------v--------+    |
| |                |                 |                   |    |
| |TASK_1_WORK_MORE|                 | TASK_2_WORK_MORE  |    |
| |                |                 |                   |    |
| |                |                 |                   |    |
| +-------+--------+                 +----------+--------+    |
|         |                                     |             |
| +-------v--------+                 +----------v--------+    |
| |                |                 |                   |    |
| |   TASK_1_END   |                 |    TASK_2_DONE    |    |
| |                |                 |                   |    |
| +----------------+                 +-------------------+    |
+---------------------------+---------------------------------+
                            |                                                                                       <---------+
                            |                                                                                                 |
                    +-------v-------+                                                                                         |
                    |               |                                                                                         +
                    |  TASKS_JOIN   |
                    +-------+-------+
                            |
                    +-------v-------+
                    |               |
                    |      END      |
                    +---------------+
``` 

# Problem statement
## Overview

It appears that the current state of each sub-region is persisted using the parent context (except in the case of initial state). This in turn overwrites the last sub-statemachine state, instead of being additive.
This works on explicit persistence of entire SM, so it may be that there might be an issue with context building.
 
## Issues

 * Using default configuration (with persistence provided by `JpaRepositoryStateMachinePersist`), all sub-regions seem to be reset upon restarting the SM. It continues from the fork parent task and re-runs all children.
 * It seems that if one of the sub-regions is in end-state and one is not (and persisted as such), the finalization (reaching end-state) of the latter will not trigger transition to join state and SM is stuck.

## Expectations
    
 * The expectation is that all sub-regions current states are persisted by default. Once a SM is acquired from persistence source, it should initialize all sub-state current states correctly and begin transitioning
 * If one or more forked regions are saved with end-state, restarting parent SM should not inhibit transitioning to join-state, if remaining regions reach end-state.
 
# Steps to reproduce

## Running with default configuration
 * Run application once with clean DB (set `spring.jpa.hibernate.ddl-auto=create`). This will kill the SM before transitioning to each sub-region end-state. The current state of both sub-regions should be `TASK_<N>_WORK` or `TASK_<N>_WORK_MORE`
 * Run the application again, with the modified DB (set `spring.jpa.hibernate.ddl-auto=none`).
 * The sub-regions will rest and log will display ```Transitioning from TASK_<N> -> TASK_<N>_WORK```
 
## Running with explicit persist
  * Change `statemachine.persist.force` flag to `true`
  * Run application once with clean DB (set `spring.jpa.hibernate.ddl-auto=create`). This will kill the SM before transitioning to each sub-region end-state. The current state of both sub-regions should be `TASK_<N>_WORK` or `TASK_<N>_WORK_MORE`
  * Run the application again, with the modified DB (set `spring.jpa.hibernate.ddl-auto=none`).
  * The sub-regions will have correct state but do no start ``` [getIds()=[TOOLS_TASKS, TASK_1_WORK, TASK_2_WORK], getClass()=class org.springframework.statemachine.state.RegionState, hashCode()=1171097095, toString()=AbstractState [id=TOOLS_TASKS, pseudoState=org.springframework.statemachine.state.DefaultPseudoState@9e33a6a, deferred=[], entryActions=[], exitActions=[], stateActions=[], regions=[TASK_1_WORK TASK_1_DONE TASK_1_WORK_MORE TASK_1  / TASK_1_WORK / uuid=d5a3d9bf-1a00-4c4a-bd25-06ffb2d85634 / id=test, TASK_2_DONE TASK_2 TASK_2_WORK TASK_2_WORK_MORE  / TASK_2_WORK / uuid=c4aea4c1-0571-4cc6-ad64-c990e31ca06e / id=test], submachine=null]], ```
  

  

