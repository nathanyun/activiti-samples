package com.example;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ActivitiApplicationTests {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    @Test
    public void testStartInstance(){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("sqlApplyCandidateUsers");
        System.out.println("DefinitionId = " + processInstance.getProcessDefinitionId());
        System.out.println("InstanceId = " + processInstance.getId());
    }
}
