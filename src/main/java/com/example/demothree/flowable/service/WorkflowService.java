package com.example.demothree.flowable.service;

import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class WorkflowService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private IdentityService identityService;

    /**
     * 启动请假流程
     */
    @Transactional
    public ProcessInstance startLeaveProcess(String initiator, Map<String, Object> variables) {
        // 设置流程启动人
        identityService.setAuthenticatedUserId(initiator);

        try {
            return runtimeService.startProcessInstanceByKey("leaveProcess", variables);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    /**
     * 查询用户任务列表
     */
    public List<Task> getUserTasks(String userId) {
        return taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 查询组任务列表
     */
    public List<Task> getGroupTasks(String groupId) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(groupId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 认领任务
     */
    @Transactional
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * 完成任务
     */
    @Transactional
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.complete(taskId, variables);
    }

    /**
     * 查询流程实例信息
     */
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    /**
     * 查询历史任务
     */
    public List<HistoricTaskInstance> getHistoricTasks(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByTaskCreateTime()
                .asc()
                .list();
    }

    /**
     * 获取流程变量
     */
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    /**
     * 删除流程实例
     */
    @Transactional
    public void deleteProcessInstance(String processInstanceId, String deleteReason) {
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
    }

    /**
     * 获取流程定义数量
     */
    public long getProcessDefinitionCount() {
        return repositoryService.createProcessDefinitionQuery().count();
    }
}