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

## 3. 网关
### 3.1 排他网关 ExclusiveGateway
排他网关，用来在流程中实现决策。 当流程执行到这个网关，所有分支都会判断条 件是否为true，如果为true则执行该分支，

> 注意：排他网关只会选择一个为true的分支执行。如果有两个分支条件 都为true，排他网关会选择id值较小的一条分支去执行。

为什么要用排他网关？

不用排他网关也可以实现分支，如：在连线的condition条件上设置分支条件。 在连线设置condition条件的缺点：如果条件都不满足，流程就结束了(是异常结 束)。

**使用场景**

例如:Tom申请请假, 3天以下需要直属经理审批,再由HR审批; 3天以上需要直属经理审批后,再有部门经理审批,最后由HR审批.

示例代码:`ActivitiExclusiveGatewayTest`

### 3.2 并行网关 ParallelGateway
并行网关允许将流程分成多条分支，也可以把多条分支汇聚到一起，并行网关的功 能是基于进入和外出顺序流的：

fork分支：并行后的所有外出顺序流，为每个顺序流都创建一个并发分支。

join汇聚： 所有到达并行网关，在此等待的进入分支， 直到所有进入顺序流的分支 都到达以后， 流程就会通过汇聚网关。

> 注意，如果同一个并行网关有多个进入和多个外出顺序流， 它就同时具 有分支和汇聚功能。 这时，网关会先汇聚所有进入的顺序流，然后再切 分成多个并行分支。

与其他网关的主要区别是，并行网关不会解析条件。 即使顺序流中定义了条件，也 会被忽略。

**使用场景**
例如: Tom申请出差, 需要技术经理和项目经理同时审批后, 再由HR审批.