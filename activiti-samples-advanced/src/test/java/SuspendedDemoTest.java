import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

public class SuspendedDemoTest {

    /**
     * 挂起流程定义, 所有的流程都会被挂起
     */
    @Test
    public void testSuspendDefinition(){
        // 获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();

        // 查询流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("myLeave")
                .latestVersion()
                .singleResult();

        String definitionId = processDefinition.getId();

        if (processDefinition.isSuspended()){
            // 激活流程定义, 支持传入一个时间, 等时间到了再激活
            repositoryService.activateProcessDefinitionById(definitionId, true, null);
            System.out.println("流程定义ID: " + definitionId + " 已激活!" );
        }else{
            // 挂起流程定义
            repositoryService.suspendProcessDefinitionById(definitionId);
            System.out.println("流程定义ID: " + definitionId + " 已挂起!" );
        }
    }


    /**
     * 查询流程实例列表
     */
    @Test
    public void testQueryInstance(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("myLeave")
                .list();
        for (ProcessInstance instance : instances) {
            System.out.println("实例ID:"+instance.getId());
            System.out.println("实例所属流程定义ID:"+instance.getProcessDefinitionId());
            System.out.println("实例名称:"+instance.getName());
            System.out.println("实例开始时间:"+instance.getStartTime());
            System.out.println("BusinessKey:"+instance.getBusinessKey());
            System.out.println(" =====================================>>> ");
        }
    }

    /**
     * 挂起流程实例, 只有一个流程实例会被挂起, 其他的流程不会被挂起
     */
    @Test
    public void testSuspendInstance(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        RuntimeService runtimeService = processEngine.getRuntimeService();

        // 获取实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId("12501").singleResult();

        String instanceId = processInstance.getId();

        if (processInstance.isSuspended()){
            // 激活流程实例
            runtimeService.activateProcessInstanceById(instanceId);
            System.out.println("流程实例ID: " + instanceId + " 已激活!" );

        }else{
            // 挂起流程实例
            runtimeService.suspendProcessInstanceById(instanceId);
            System.out.println("流程实例ID: " + instanceId + " 已挂起!" );
        }
    }


    /**
     * 当流程挂起后, 再执行任务, 会抛出异常: org.activiti.engine.ActivitiException: Cannot complete a suspended task
     */
    @Test
    public void testComplete(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceId("12501").singleResult();
        // 处理任务
        taskService.complete(task.getId());
    }

}
