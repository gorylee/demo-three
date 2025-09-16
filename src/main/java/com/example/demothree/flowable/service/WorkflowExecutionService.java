package com.example.demothree.flowable.service;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WorkflowExecutionService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    /**
     * 启动流程实例
     */
    @Transactional
    public ProcessInstance startProcess(String processDefinitionKey, Map<String, Object> variables) {
        log.info("启动流程: {}", processDefinitionKey);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
        log.info("流程启动成功: {}", instance.getId());
        return instance;
    }

    /**
     * 启动流程实例（带业务键）
     */
    @Transactional
    public ProcessInstance startProcessWithBusinessKey(String processDefinitionKey,
                                                       String businessKey,
                                                       Map<String, Object> variables) {
        log.info("启动流程: {}, 业务键: {}", processDefinitionKey, businessKey);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        log.info("流程启动成功: {}", instance.getId());
        return instance;
    }

    /**
     * 查询用户任务
     */
    public List<Task> getUserTasks(String userId) {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();
        return list;
    }

    /**
     * 查询候选用户任务
     */
    public List<Task> getCandidateUserTasks(String userId) {
        return taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 查询候选组任务
     */
    public List<Task> getCandidateGroupTasks(String groupId) {
        return taskService.createTaskQuery()
                .taskCandidateGroup(groupId)
                .orderByTaskCreateTime()
                .desc()
                .list();
    }

    /**
     * 完成任务
     */
    @Transactional
    public void completeTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.info("完成任务: {}, 流程: {}", task.getName(), task.getProcessInstanceId());

        if (variables != null && !variables.isEmpty()) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }

        log.info("任务完成成功");
    }

    /**
     * 认领任务
     */
    @Transactional
    public void claimTask(String taskId, String userId) {
        taskService.claim(taskId, userId);
        log.info("用户 {} 认领任务 {}", userId, taskId);
    }

    /**
     * 设置任务变量
     */
    public void setTaskVariables(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
        log.info("设置任务变量: {}", taskId);
    }

    /**
     * 获取流程变量
     */
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }
}