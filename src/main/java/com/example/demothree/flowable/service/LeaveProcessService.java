package com.example.demothree.flowable.service;

import com.example.demothree.flowable.dto.TaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveProcessService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowExecutionService  workflowService;

    /**
     * 发起请假申请
     */
    @Transactional
    public ProcessInstance applyForLeave(String applicant, Map<String, Object> leaveInfo) {
        // 设置部门经理（这里简单模拟，实际应该从用户服务获取）
        String deptManager = getDeptManagerByApplicant(applicant);

        Map<String, Object> variables = new HashMap<>(leaveInfo);
        variables.put("applicant", applicant);
        variables.put("deptManager", deptManager);
        variables.put("generalManager", "generalManager"); // 总经理

        log.info("{} 发起请假申请: {}", applicant, leaveInfo);

        return workflowService.startProcessWithBusinessKey(
                "leaveProcess",
                "LEAVE-" + System.currentTimeMillis(),
                variables
        );
    }

    /**
     * 部门经理审批
     */
    @Transactional
    public void deptManagerApprove(String taskId, String comment, boolean approved) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("deptComment", comment);
        variables.put("deptApproved", approved);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.info("部门经理审批任务: {}, 意见: {}, 结果: {}", task.getName(), comment, approved ? "批准" : "拒绝");

        workflowService.completeTask(taskId, variables);
    }

    /**
     * HR备案
     */
    @Transactional
    public void hrRecord(String taskId, String comment) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("hrComment", comment);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.info("HR备案: {}, 意见: {}", task.getName(), comment);

        workflowService.completeTask(taskId, variables);
    }

    /**
     * 总经理审批
     */
    @Transactional
    public void generalManagerApprove(String taskId, String comment, boolean approved) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("gmComment", comment);
        variables.put("gmApproved", approved);

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.info("总经理审批: {}, 意见: {}, 结果: {}", task.getName(), comment, approved ? "批准" : "拒绝");

        workflowService.completeTask(taskId, variables);
    }

    /**
     * 获取用户的待办任务
     */
    public List<Task> getUserTasks(String userId) {
        List<Task> userTasks = workflowService.getUserTasks(userId);
        return userTasks;
    }

    /**
     * 获取请假申请详情
     */
    public Map<String, Object> getLeaveApplicationDetails(String processInstanceId) {
        Map<String, Object> details = new HashMap<>();

        // 获取流程变量
        details.put("processVariables", workflowService.getProcessVariables(processInstanceId));

        // 获取历史信息
        HistoricProcessInstance history = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (history != null) {
            details.put("startTime", history.getStartTime());
            details.put("endTime", history.getEndTime());
            details.put("status", history.getEndTime() != null ? "已完成" : "进行中");
        }

        return details;
    }

    /**
     * 模拟根据申请人获取部门经理
     */
    private String getDeptManagerByApplicant(String applicant) {
        // 这里简单模拟，实际应该从用户服务或组织服务获取
        Map<String, String> userManagerMap = Map.of(
                "zhangsan", "deptManager1",
                "lisi", "deptManager2",
                "wangwu", "deptManager1"
        );
        return userManagerMap.getOrDefault(applicant, "deptManager1");
    }
}