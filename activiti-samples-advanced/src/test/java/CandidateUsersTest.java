import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多候选人测试
 */
public class CandidateUsersTest {

    private static final String DEFINITION_KEY = "sqlApplyCandidateUsers";

    // 获取最新的定义ID
    public String findLastInstantId(ProcessEngine processEngine){
        // 获取最新的定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(DEFINITION_KEY)
                .latestVersion()
                .singleResult();

        assert processDefinition != null;

        // 获取最新的实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processDefinitionId(processDefinition.getId())
                .orderByProcessInstanceId()
                .desc()
                .singleResult();
        if (processInstance == null){
            return "";
        }
        return processInstance.getId();
    }

    /**
     * 部署流程
     */
    @Test
    public void testDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/sqlApplyCandidateUsers.bpmn")// 指定流程配置文件
                .addClasspathResource("bpmn/sqlApplyCandidateUsers.png")// 指定流程图文件
                .name("SQL申请sqlApplyCandidateUsers")// 命名
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
        variables.put("worker", "Tom");

        // 配置多个候选人
        List<String> candidateUserList = Arrays.asList("Jerry", "Bob", "Bonny");
        variables.put("candidateUserList", candidateUserList);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(DEFINITION_KEY, variables);
        System.out.println("流程定义ID=" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例ID=" + processInstance.getId());
    }


    /**
     * 查询任务列表
     */
    @Test
    public void testFindTasks(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 查找任务
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(findLastInstantId(processEngine))
                .list();
        for (Task task : tasks) {
            System.out.println("流程定义ID: " + task.getProcessDefinitionId());
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
                .processInstanceId(findLastInstantId(processEngine))
                .taskAssignee("Bonny")// 查询Tom的任务
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
     * 认领任务
     */
    @Test
    public void testClaimTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String candidateUser = "Bonny";
        // 查询任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(findLastInstantId(processEngine))
                .taskCandidateUser(candidateUser)// 查询 Jerry 的任务
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)
        if (task != null){
            // 执行任务
            taskService.claim(task.getId(), candidateUser);
            System.err.println("任务被："+candidateUser+" 认领了!");
        }else{
            System.err.println("查找任务失败");
        }
    }


    /**
     * 退回认领任务
     */
    @Test
    public void testUnClaimTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String candidateUser = "Bonny";
        // 查询任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(findLastInstantId(processEngine))
                .taskCandidateOrAssigned(candidateUser)// 查询 Jerry 的任务
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)
        if (task != null){
            // 退回
            taskService.unclaim(task.getId());
            // 也可以把任务设置给别人
            //taskService.setAssignee(task.getId(), "otherUser");
            System.err.println("任务被："+candidateUser+" 退回认领了!");
        }else{
            System.err.println("查找任务失败");
        }
    }
}
