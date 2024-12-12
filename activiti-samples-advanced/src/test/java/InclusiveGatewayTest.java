import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 包含网关测试
 */
public class InclusiveGatewayTest {

    /**
     * 部署流程
     */
    @Test
    public void testDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leaveInclusiveGateway.bpmn")// 指定流程配置文件
                .addClasspathResource("bpmn/leaveInclusiveGateway.png")// 指定流程图文件
                .name("出差申请流程inclusive")// 命名
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
        variables.put("num", 6);// 出差天数
        variables.put("worker", "Tony");
        variables.put("productManager", "Tom");
        variables.put("technicalManager", "Jerry");
        variables.put("hr", "Bonny");
        variables.put("manager", "Bob");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveInclusiveGateway", variables);
        System.out.println("流程实例ID=" + processInstance.getId());
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
                .processDefinitionKey("leaveInclusiveGateway")
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
                //.processDefinitionKey("leaveInclusiveGateway")// 流程key
                .processInstanceId("90001")
                .taskAssignee("Tom")// 查询Tom的任务
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
     * 查询历史
     */
    @Test
    public void testQueryHis(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        // 获取实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("leaveInclusiveGateway")
                .singleResult();

        // 查询这个实例的历史记录
        HistoryService historyService = processEngine.getHistoryService();
        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery();
        historicActivityInstanceQuery.processInstanceId(processInstance.getId());

        for (HistoricActivityInstance hi : historicActivityInstanceQuery.list()) {
            if ("userTask".equals(hi.getActivityType())){
                System.out.println("在" + hi.getTime() + " 时, " + hi.getAssignee() +"做了任务:" + hi.getActivityName());
            }
        }
    }
}
