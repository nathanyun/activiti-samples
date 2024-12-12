import org.activiti.engine.*;
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
 * 流程变量测试, 启动流程实例时指定变量
 */
public class VariablesStartInstanceTest {

    /**
     * 部署流程
     */
    @Test
    public void testDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leaveVariables.bpmn")// 指定流程配置文件
                .addClasspathResource("bpmn/leaveVariables.png")// 指定流程图文件
                .name("请假申请流程leaveVariables")// 命名
                .deploy();// 部署
        System.out.println("deptId=" + deployment.getId());
        System.out.println("deptName=" + deployment.getName());
    }

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
        Map<String, Object> variables1 = buildVariables();
        Map<String, Object> variables2 = buildVariables2();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("leaveVariables", variables2);
        System.out.println("流程ID=" + processInstance.getId());
        System.out.println("流程定义ID=" + processInstance.getProcessDefinitionId());
        System.out.println("=======================================");
    }



    private Map<String, Object> buildVariables(){
        Map<String, Object> variables = new HashMap<>();
        variables.put("num", 2);// 请假2天
        variables.put("worker", "zhangsan");
        variables.put("leader", "lisi");
        variables.put("manager", "wangwu");
        variables.put("hr", "zhaoliu");
        return variables;
    }

    private Map<String, Object> buildVariables2(){
        Map<String, Object> variables = new HashMap<>();
        variables.put("num", 5);// 请假5天
        variables.put("worker", "Jack");
        variables.put("leader", "Tom Director Manager");
        variables.put("manager", "Bob General Manager");
        variables.put("hr", "Bonny HR");
        return variables;
    }


    /**
     * 查询任务列表
     */
    @Test
    public void testFindTasks(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();

        // 查询任务列表
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("leaveVariables")
                //.taskAssignee("manager")// worker --> manager --> hr
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
        // 未处理任务前，两个实例的流程如下：
        /*
        流程实例ID: 22501
        任务ID: 22510
        任务名称: 提交申请
        任务负责人: zhangsan
        ==================================
        流程实例ID: 25001
        任务ID: 25010
        任务名称: 提交申请
        任务负责人: Jack
        ==================================
        */

        // 将两个流程实例任务执行完直属领导审批后, 两个实例的流程分别如下：
        /*
        流程实例ID: 22501
        任务ID: 32502
        任务名称: HR审批
        任务负责人: zhaoliu
        ==================================
        流程实例ID: 25001
        任务ID: 35002
        任务名称: 部门领导审批
        任务负责人: Bob General Manager
        ==================================
        */

        // 查询任务
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("leaveVariables")// 流程key
                //.processInstanceId("22501") // 流程实例ID
                .taskAssignee("Tom Director Manager")// 负责人
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)

        // 完成任务
        if (task != null){
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
