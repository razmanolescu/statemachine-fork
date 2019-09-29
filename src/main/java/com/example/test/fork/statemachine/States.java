package com.example.test.fork.statemachine;

public enum States {

    NOT_STARTED,
    TOOLS_FORK, TOOLS_JOIN,
    TOOLS_TASKS,
    TASK_1, TASK_2,
    TASK_1_WORK_MORE, TASK_2_WORK_MORE,
    TASK_1_WORK, TASK_2_WORK,
    TASK_1_DONE, TASK_2_DONE,
    ERROR,
    END

}
