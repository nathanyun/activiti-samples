import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程变量测试,  在完成任务时设置流程变量
 * <pre>前提条件: 流程已经部署并启动了实例</pre>
 */
public class ActivitiVariablesTaskTest {


    /**
     * 启动流程实例
     */
    @Test
    public void testStartProcess(){
        // 1. 获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2. 得到 RuntimeService 实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 3. 启动实例
        Map<String, Object> variables = new HashMap<>();
        variables.put("num", 5);// 请假5天
        variables.put("worker", "Jack");
        variables.put("leader", "Tom");
        variables.put("manager", "Bob");
        variables.put("hr", "Bonny");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveVariables", variables);
        System.out.println("流程ID=" + processInstance.getId());
        System.out.println("流程定义ID=" + processInstance.getProcessDefinitionId());
        System.out.println("=======================================");
    }


    /**
     * 查询任务列表
     */
    @Test
    public void testFindTasks(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("leaveVariables")
                .list();
        for (Task task : tasks) {
            System.out.println("流程实例ID: " + task.getProcessInstanceId());
            System.out.println("任务ID: " + task.getId());
            System.out.println("任务名称: " + task.getName());
            System.out.println("任务负责人: " + task.getAssignee());
            System.out.println("==================================");
        }
    }

    /**
     * 执行任务
     */
    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        // 未处理任务前，实例的流程如下：, 我们在执行完任务后, 修改下一个审批人为 Nathan
        /*
        流程实例ID: 25001
        任务ID: 35002
        任务名称: 部门领导审批
        任务负责人: Bob
        ==================================
        */
        // 查询任务
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("leaveVariables")// 流程key
                .processInstanceId("25001") // 流程实例ID
                .taskAssignee("Bob General Manager")// 负责人
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)
        // 完成任务
        if (task != null){

            // 执行任务时,修改流程变量!!!!
            Map<String, Object> variables = new HashMap<>();
            variables.put("hr", "Nathan");// 完成任务时, 修改hr 的值为 Nathan
            taskService.complete(task.getId(), variables);

            System.err.println(task.getAssignee() +"的任务："+task.getName()+" 完成了!");
        }else{
            System.err.println("暂无待办事项");
        }

        /* 再次查询任务, 会发现已经变成我们要修改的内容了.
        流程实例ID: 25001
        任务ID: 37502
        任务名称: HR审批
        任务负责人: Nathan
        ==================================
         */
    }

    /**
     * 查询流程定义列表
     */
    @Test
    public void testQueryProcessDefinition(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitions = processDefinitionQuery.processDefinitionKey("leaveVariables")
                .orderByProcessDefinitionVersion()// 按照版本排序
                .desc() // 倒序
                .list();
        for (ProcessDefinition definition : definitions) {
            System.out.println("定义ID："+definition.getId());
            System.out.println("定义名称："+definition.getName());
            System.out.println("定义版本："+definition.getVersion());
            System.out.println("定义key："+definition.getKey());
            System.out.println("定义所属部署ID："+definition.getDeploymentId());
            System.out.println(" =====================================>>> ");
        }
    }

    /**
     * 查询流程实例
     */
    @Test
    public void testQueryInstance(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
        processInstanceQuery.orderByProcessInstanceId().asc();
        List<ProcessInstance> instances = processInstanceQuery.processDefinitionKey("leaveVariables").list();
        for (ProcessInstance instance : instances) {
            System.out.println("实例ID:"+instance.getId());
            System.out.println("实例所属流程ID:"+instance.getProcessDefinitionId());
            System.out.println("实例名称:"+instance.getName());
            System.out.println("实例开始时间:"+instance.getStartTime());
            System.out.println("getProcessVariables:"+instance.getProcessVariables());
            System.out.println("getBusinessKey:"+instance.getBusinessKey());
            System.out.println(" =====================================>>> ");
        }
    }

}
