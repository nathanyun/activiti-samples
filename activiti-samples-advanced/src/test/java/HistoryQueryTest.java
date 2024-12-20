import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryQueryTest {


    /**
     * 部署流程
     */
    @Test
    public void testDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/acuCreateMerchant.bpmn")// 指定流程配置文件
                .addClasspathResource("bpmn/acuCreateMerchant.png")// 指定流程图文件
                .name("创建商户")// 命名
                .deploy();// 部署
        System.out.println("deptId=" + deployment.getId());
        System.out.println("deptName=" + deployment.getName());
    }

    @Test
    public void testDelete(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
         /*若希望强制删除流程, 请使用下面的方式, 指定为true 不管流程是否完成, 都会删除整个流程实例.
         并且这个功能还会把这个流程的相关历史表的记录都会删掉, 一般此功能仅开放给管理员使用.*/
        repositoryService.deleteDeployment("162501", true);
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
        Map<String, Object> variables = new HashMap<>();
        variables.put("nameAbc", 2);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("acuCreateMerchant", variables);
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
        // 查询任务列表, processInstId=130001
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId("182501")
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
     * 认领任务
     */
    @Test
    public void testClaimTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        String candidateUser = "Elon Mask";
//        String candidateUser = "Trump";
        // 查询任务
        Task task = taskService.createTaskQuery()
                .processInstanceId("182501")
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
     * 执行任务
     */
    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        // 查询任务
        Task task = taskService.createTaskQuery()
                //.processDefinitionKey("acuCreateMerchant")// 流程key
                .processInstanceId("182501") // 流程实例ID
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)
        // 完成任务
        if (task != null){
            Map<String, Object> variables = new HashMap<>();
            variables.put("reviewStatus", "SUCCESS");
//            variables.put("reviewStatus", "RETURN");
//            variables.put("reviewStatus", "REFUSE");
            taskService.complete(task.getId(), variables);
            System.err.println(task.getAssignee() +"的任务："+task.getName()+" 完成了!");
        }else{
            System.err.println("暂无待办事项");
        }
    }

    /**
     * 查询历史
     */
    @Test
    public void testQuery(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        //String processDefinitionId = "acuCreateMerchant:1:180004";
        String processInstanceId = "182501";
        String gatewayId = "sid-c6998081-62ca-4c5d-98aa-578aa684cb6f";
        // 查询历史活动实例
//        HistoricActivityInstance gatewayActivity = historyService.createHistoricActivityInstanceQuery()
//                .processInstanceId(processInstanceId) // 使用流程实例 ID
//                //.activityId(gatewayId)               // 指定 ExclusiveGateway 的 ID
//                .singleResult();

        // 查询网关的后续流转
        List<HistoricActivityInstance> activityInstances = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceEndTime().asc() // 按结束时间排序，确保顺序
                .list();

        for (HistoricActivityInstance activity : activityInstances) {
            if (activity.getActivityType().equalsIgnoreCase("manualTask")) {
                System.out.println("流转线 ID：" + activity.getActivityName());
            }
        }

        // 结合网关的出线条件
//        RepositoryService repositoryService = processEngine.getRepositoryService();
//        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
//        FlowElement flowElement = bpmnModel.getFlowElement(gatewayId);
//
//        if (flowElement instanceof ExclusiveGateway) {
//            ExclusiveGateway exclusiveGateway = (ExclusiveGateway) flowElement;
//            List<SequenceFlow> outgoingFlows = exclusiveGateway.getOutgoingFlows();
//
//            for (SequenceFlow flow : outgoingFlows) {
//                // 查看历史记录是否包含此出线 ID
//                boolean executed = activityInstances.stream()
//                        .anyMatch(activity -> activity.getActivityId().equals(flow.getId()));
//                if (executed) {
//                    System.out.println("当时执行的分支是：" + flow.getId());
//                }
//            }
//        }




    }
}
