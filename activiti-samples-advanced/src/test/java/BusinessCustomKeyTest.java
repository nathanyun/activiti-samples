import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.junit.Test;

import java.util.List;

public class BusinessCustomKeyTest {

    /**
     * 添加业务自定义key
     */
    @Test
    public void testAddBusinessKey(){
        // 获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 在启动实例前, 可以设置业务的自定义key
        // 第一个参数为流程key, 第二个参数为业务的自定义key
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myLeave", "businessCustomKey000");
        System.out.println("getBusinessKey = " + processInstance.getBusinessKey());
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
        List<ProcessInstance> instances = processInstanceQuery.processDefinitionKey("myLeave").list();
        for (ProcessInstance instance : instances) {
            System.out.println("实例ID:"+instance.getId());
            System.out.println("实例所属流程定义ID:"+instance.getProcessDefinitionId());
            System.out.println("实例名称:"+instance.getName());
            System.out.println("实例开始时间:"+instance.getStartTime());
            System.out.println("getProcessVariables:"+instance.getProcessVariables());
            System.out.println("getBusinessKey:"+instance.getBusinessKey());
            System.out.println("========================================");
        }
    }
}
