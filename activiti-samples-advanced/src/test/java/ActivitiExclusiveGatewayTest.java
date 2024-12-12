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
 * 排他网关测试
 */
public class ActivitiExclusiveGatewayTest {

    /**
     * 部署流程
     */
    @Test
    public void testDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leaveExclusiveGateway.bpmn")// 指定流程配置文件
                .addClasspathResource("bpmn/leaveExclusiveGateway.png")// 指定流程图文件
                .name("请假申请流程leaveExclusiveGateway")// 命名
                .deploy();// 部署
        System.out.println("deptId=" + deployment.getId());
        System.out.println("deptName=" + deployment.getName());
    }

    /**
     * 启动流程实例
     */
    @Test
    public void testStartProcess(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<>();
        variables.put("num", 5);// 请假5天
        variables.put("worker", "Tom");
        variables.put("leader", "Jerry");
        variables.put("manager", "Bob");
        variables.put("hr", "Bonny");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveExclusiveGateway", variables);
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
                .processDefinitionKey("leaveExclusiveGateway")
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
        // 查询任务
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("leaveExclusiveGateway")// 流程key
                .taskAssignee("Jerry")// 查询Tom的任务
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)
        if (task != null){
            // 执行任务
            taskService.complete(task.getId());
            System.err.println(task.getAssignee() +"的任务："+task.getName()+" 完成了!");
        }else{
            System.err.println("暂无待办事项");
        }
    }

    /**
     * 查询流程定义列表
     */
    @Test
    public void testQueryProcessDefinition(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitions = processDefinitionQuery.processDefinitionKey("leaveExclusiveGateway")
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
        List<ProcessInstance> instances = processInstanceQuery.processDefinitionKey("leaveExclusiveGateway").list();
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
