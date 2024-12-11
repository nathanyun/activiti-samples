import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * 部署一个流程
 */
public class ActivitiDemoTest {

    /**
     * 方式1: 通过上传文件方式来部署一个流程
     */
    @Test
    public void testDeployment(){
        // 1. 获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2. 得到 processEngine 实例
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 3. 创建
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("bpmn/leave.bpmn")// 指定流程配置文件
                .addClasspathResource("bpmn/leave.png")// 指定流程图文件
                .name("请假申请流程")// 命名
                .deploy();// 部署
        System.out.println("deptId=" + deployment.getId());
        System.out.println("deptName=" + deployment.getName());
    }



    /**
     * 方式2: 通过上传zip文件方式来部署一个流程 (适合多个流程一起发布)
     */
    @Test
    public void testDeploymentByZip(){
        // 1. 获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2. 得到 processEngine 实例
        RepositoryService repositoryService = processEngine.getRepositoryService();

        // 获取zip输入流
        // 注意: leave.zip 中的 .bpmn 文件与 .png 文件关系是通过 <process id="myLeave"> 中的ID来绑定关系的
        // 需要将png文件名规范命名, <通过文件名.processId.后缀名来命名>
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("bpmn/leave.zip");
        ZipInputStream zipInputStream = null;
        if (inputStream != null) {
            zipInputStream = new ZipInputStream(inputStream);
        }

        // 3. 创建
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("请假申请流程")// 命名
                .deploy();// 部署
        System.out.println("deptId=" + deployment.getId());
        System.out.println("deptName=" + deployment.getName());
    }

    /**
     * 启动流程实例 (部署流程完成后, 需要启动流程)
     */
    @Test
    public void testStartProcess(){
        // 1. 获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2. 得到 RuntimeService 实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 3. 启动实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("myLeave");
        System.out.println("流程ID=" + processInstance.getId());
        System.out.println("流程定义ID=" + processInstance.getProcessDefinitionId());
        System.out.println("当前活动ID=" + processInstance.getActivityId());
    }

    /**
     * 查询任务列表(流程实例启动后, 需要执行任务, 执行前需要查询列表)
     */
    @Test
    public void testFindTasks(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();

        // 查询任务列表
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("myLeave")
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

        // 查询任务
        Task task1 = taskService.createTaskQuery()
                .processDefinitionKey("myLeave")// 流程key
                .taskAssignee("worker")// 负责人
                .singleResult();// 只返回一个任务(注意: 如果这个负责人有多个任务,这里查一个的话会抛异常)

        // 完成任务
        taskService.complete(task1.getId());
    }

    /**
     * 删除流程
     */
    @Test
    public void testDeleteDeployment(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 删除, 参数为ID, 若流程还未执行完, 调用删除会报错
        repositoryService.deleteDeployment("2501");
        /*
         若希望强制删除流程, 请使用下面的方式, 指定为true 不管流程是否完成, 都会删除整个流程实例.
         并且这个功能还会把这个流程的相关历史表的记录都会删掉, 一般此功能仅开放给管理员使用.*/
        //repositoryService.deleteDeployment("1", true);

    }

    /**
     * 查询流程定义列表
     */
    @Test
    public void testQueryProcessDefinition(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> definitions = processDefinitionQuery.processDefinitionKey("myLeave")
                .orderByProcessDefinitionVersion()// 按照版本排序
                .desc() // 倒序
                .list();
        for (ProcessDefinition definition : definitions) {
            System.out.println(definition.getId());
            System.out.println(definition.getName());
            System.out.println(definition.getVersion());
            System.out.println(definition.getKey());
            System.out.println(definition.getDeploymentId());
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
        List<ProcessInstance> instances = processInstanceQuery.processDefinitionKey("myLeave").list();
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

    /**
     * 查询历史
     */
    @Test
    public void testQueryHis(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();

        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery();
        // 查询 ACT_HI_ACTINST 表，条件：根据 InstanceId 查询
        //historicActivityInstanceQuery.processInstanceId("5001");
        // 查询 ACT_HI_ACTINST 表，条件：根据 DefinitionId 查询
        //historicActivityInstanceQuery.processDefinitionId("myLeave:2:2504");
        historicActivityInstanceQuery.orderByHistoricActivityInstanceStartTime().asc();
        List<HistoricActivityInstance> list = historicActivityInstanceQuery.list();
        for (HistoricActivityInstance hi : list) {
            System.out.println(hi.getActivityId());
            System.out.println(hi.getActivityName());
            System.out.println(hi.getProcessDefinitionId());
            System.out.println(hi.getProcessInstanceId());
            System.out.println("<==========================>");
        }
    }

    /**
     * 从数据库下载流程图和配置文件
     * @throws IOException
     */
    @Test
    public void  queryBpmnFile() throws IOException {
        // 1、得到引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2、获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 3、得到查询器：ProcessDefinitionQuery，设置查询条件,得到想要的流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("myLeave")
                .singleResult();
        // 4、通过流程定义信息，得到部署ID
        String deploymentId = processDefinition.getDeploymentId();
        // 5、通过repositoryService的方法，实现读取图片信息和bpmn信息
        // png图片的流
        InputStream pngInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getDiagramResourceName());
        //        bpmn文件的流
        InputStream bpmnInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
        // 6、构造OutputStream流
        File file_png = new File("/Users/yunnasheng/Downloads/myLeave.png");
        File file_bpmn = new File("/Users/yunnasheng/Downloads/myLeave.bpmn");
        FileOutputStream bpmnOut = new FileOutputStream(file_bpmn);
        FileOutputStream pngOut = new FileOutputStream(file_png);
        // 7、输入流，输出流的转换
        IOUtils.copy(pngInput,pngOut);
        IOUtils.copy(bpmnInput,bpmnOut);
        // 8、关闭流
        pngOut.close();
        bpmnOut.close();
        pngInput.close();
        bpmnInput.close();
    }
}
