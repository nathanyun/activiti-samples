# Activiti 的高级用法

## 1. 启动流程实例添加自定义key
```java
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
```

## 2. 挂起,激活流程
挂起后的流程不可以再去执行.

**挂起/激活流程定义**
```java
// 所有的流程都会被挂起
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
```

**挂起/激活流程实例**
```java
// 只有一个流程实例会被挂起, 其他的流程不会被挂起
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
```