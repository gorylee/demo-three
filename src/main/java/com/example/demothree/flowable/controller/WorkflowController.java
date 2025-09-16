package com.example.demothree.flowable.controller;

import cn.hutool.json.JSONUtil;
import com.example.demothree.flowable.service.WorkflowService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    /**
     * 启动请假流程
     */
    @PostMapping("/leave/start")
    public Map<String, Object> startLeaveProcess(@RequestParam String initiator,
                                                 @RequestBody Map<String, Object> variables) {
        ProcessInstance processInstance = workflowService.startLeaveProcess(initiator, variables);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("processInstanceId", processInstance.getId());
        result.put("processDefinitionId", processInstance.getProcessDefinitionId());
        result.put("businessKey", processInstance.getBusinessKey());
        return result;
    }

    /**
     * 获取用户待办任务
     */
    @GetMapping("/tasks/user/{userId}")
    public List<Task> getUserTasks(@PathVariable String userId) {
        List<Task> userTasks = workflowService.getUserTasks(userId);
        System.out.println("用户待办任务：" + JSONUtil.toJsonStr(userTasks));
        return userTasks;
    }

    /**
     * 获取组待办任务
     */
    @GetMapping("/tasks/group/{groupId}")
    public List<Task> getGroupTasks(@PathVariable String groupId) {
        return workflowService.getGroupTasks(groupId);
    }

    /**
     * 认领任务
     */
    @PostMapping("/tasks/{taskId}/claim")
    public Map<String, Object> claimTask(@PathVariable String taskId,
                                         @RequestParam String userId) {
        workflowService.claimTask(taskId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务认领成功");
        return result;
    }

    /**
     * 完成任务
     */
    @PostMapping("/tasks/{taskId}/complete")
    public Map<String, Object> completeTask(@PathVariable String taskId,
                                            @RequestBody Map<String, Object> variables) {
        workflowService.completeTask(taskId, variables);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务完成成功");
        return result;
    }

    /**
     * 查询流程历史
     */
    @GetMapping("/process/{processInstanceId}/history")
    public List<HistoricTaskInstance> getProcessHistory(@PathVariable String processInstanceId) {
        return workflowService.getHistoricTasks(processInstanceId);
    }

    /**
     * 获取流程变量
     */
    @GetMapping("/process/{processInstanceId}/variables")
    public Map<String, Object> getProcessVariables(@PathVariable String processInstanceId) {
        return workflowService.getProcessVariables(processInstanceId);
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("processDefinitionCount", workflowService.getProcessDefinitionCount());
        return stats;
    }
}