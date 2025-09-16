package com.example.demothree.flowable.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessProgressService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;

    /**
     * 获取流程进度详情（兼容已完成流程）
     */
    public Map<String, Object> getProcessProgress(String processInstanceId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 首先检查历史记录中是否存在该流程实例
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historicProcessInstance == null) {
            throw new RuntimeException("流程实例不存在: " + processInstanceId);
        }

        // 检查流程是否仍在运行中
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        boolean isCompleted = historicProcessInstance.getEndTime() != null;

        // 基础信息
        result.put("processInstanceId", processInstanceId);
        result.put("processDefinitionId", historicProcessInstance.getProcessDefinitionId());
        result.put("businessKey", historicProcessInstance.getBusinessKey());
        result.put("status", isCompleted ? "COMPLETED" : "RUNNING");
        result.put("startTime", historicProcessInstance.getStartTime());
        result.put("endTime", historicProcessInstance.getEndTime());

        // 获取流程定义信息
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(historicProcessInstance.getProcessDefinitionId())
                .singleResult();
        result.put("processName", processDefinition != null ? processDefinition.getName() : "未知流程");

        // 获取所有活动历史
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        // 获取所有任务历史
        List<HistoricTaskInstance> taskHistories = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricTaskInstanceStartTime()
                .asc()
                .list();

        // 构建进度时间线
        List<Map<String, Object>> timeline = buildTimeline(activities, taskHistories);
        result.put("timeline", timeline);

        // 当前任务（如果是运行中的流程）
        if (!isCompleted && processInstance != null) {
            List<Task> currentTasks = taskService.createTaskQuery()
                    .processInstanceId(processInstanceId)
                    .list();
            result.put("currentTasks", formatCurrentTasks(currentTasks));
        } else {
            result.put("currentTasks", Collections.emptyList());
        }

        // 进度百分比
        result.put("progressPercentage", calculateProgressPercentage(activities, isCompleted));

        // 流程变量 - 从历史变量表中获取
        Map<String, Object> variables = getProcessVariables(processInstanceId, isCompleted);
        result.put("processVariables", variables);

        // 添加持续时间信息
        if (isCompleted && historicProcessInstance.getStartTime() != null && historicProcessInstance.getEndTime() != null) {
            long duration = historicProcessInstance.getEndTime().getTime() - historicProcessInstance.getStartTime().getTime();
            result.put("totalDuration", duration);
            result.put("totalDurationFormatted", formatDuration(duration));
        }

        return result;
    }

    /**
     * 获取流程变量（兼容已完成流程）
     */
    private Map<String, Object> getProcessVariables(String processInstanceId, boolean isCompleted) {
        if (isCompleted) {
            // 从历史变量表获取
            return historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .list()
                    .stream()
                    .collect(Collectors.toMap(
                            instance -> instance.getVariableName(),
                            instance -> instance.getValue(),
                            (existing, replacement) -> existing
                    ));
        } else {
            // 从运行时变量表获取
            return runtimeService.getVariables(processInstanceId);
        }
    }

    /**
     * 计算进度百分比（兼容已完成流程）
     */
    private int calculateProgressPercentage(List<HistoricActivityInstance> activities, boolean isCompleted) {
        if (isCompleted) {
            return 100; // 已完成流程直接返回100%
        }

        if (activities.isEmpty()) {
            return 0;
        }

        long completed = activities.stream()
                .filter(activity -> activity.getEndTime() != null)
                .count();

        return (int) ((completed * 100) / activities.size());
    }

    /**
     * 构建时间线
     */
    private List<Map<String, Object>> buildTimeline(List<HistoricActivityInstance> activities,
                                                    List<HistoricTaskInstance> taskHistories) {
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 处理活动历史
        for (HistoricActivityInstance activity : activities) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "ACTIVITY");
            item.put("activityId", activity.getActivityId());
            item.put("activityName", activity.getActivityName());
            item.put("activityType", activity.getActivityType());
            item.put("startTime", activity.getStartTime());
            item.put("endTime", activity.getEndTime());
            item.put("duration", activity.getDurationInMillis());
            item.put("assignee", activity.getAssignee());
            item.put("status", activity.getEndTime() != null ? "COMPLETED" : "RUNNING");

            timeline.add(item);
        }

        // 处理任务历史
        for (HistoricTaskInstance task : taskHistories) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "TASK");
            item.put("taskId", task.getId());
            item.put("taskName", task.getName());
            item.put("taskDefinitionKey", task.getTaskDefinitionKey());
            item.put("assignee", task.getAssignee());
            item.put("owner", task.getOwner());
            item.put("startTime", task.getStartTime());
            item.put("endTime", task.getEndTime());
            item.put("duration", task.getDurationInMillis());
            item.put("dueDate", task.getDueDate());
            item.put("priority", task.getPriority());
            item.put("status", task.getEndTime() != null ? "COMPLETED" : "RUNNING");

            timeline.add(item);
        }

        // 按开始时间排序
        timeline.sort(Comparator.comparing(item -> (Date) item.get("startTime")));

        return timeline;
    }

    /**
     * 格式化当前任务
     */
    private List<Map<String, Object>> formatCurrentTasks(List<Task> tasks) {
        return tasks.stream().map(task -> {
            Map<String, Object> taskInfo = new LinkedHashMap<>();
            taskInfo.put("taskId", task.getId());
            taskInfo.put("taskName", task.getName());
            taskInfo.put("assignee", task.getAssignee());
            taskInfo.put("createTime", task.getCreateTime());
            taskInfo.put("dueDate", task.getDueDate());
            taskInfo.put("priority", task.getPriority());
            return taskInfo;
        }).collect(Collectors.toList());
    }

    /**
     * 格式化持续时间
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "天" + (hours % 24) + "小时";
        } else if (hours > 0) {
            return hours + "小时" + (minutes % 60) + "分钟";
        } else if (minutes > 0) {
            return minutes + "分钟" + (seconds % 60) + "秒";
        } else {
            return seconds + "秒";
        }
    }

    /**
     * 获取用户相关的所有流程进度
     */
    public List<Map<String, Object>> getUserProcessProgress(String userId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取用户参与的所有流程实例（包括已完成和运行中的）
        List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery()
                .involvedUser(userId)
                .orderByProcessInstanceStartTime()
                .desc()
                .list();

        for (HistoricProcessInstance process : processes) {
            try {
                Map<String, Object> progress = getProcessProgressSummary(process);
                result.add(progress);
            } catch (Exception e) {
                log.warn("获取流程进度失败: {}", process.getId(), e);
            }
        }

        return result;
    }

    /**
     * 获取流程进度摘要（性能优化）
     */
    private Map<String, Object> getProcessProgressSummary(HistoricProcessInstance process) {
        Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("processInstanceId", process.getId());
        progress.put("processDefinitionId", process.getProcessDefinitionId());
        progress.put("businessKey", process.getBusinessKey());
        progress.put("startTime", process.getStartTime());
        progress.put("endTime", process.getEndTime());
        progress.put("status", process.getEndTime() != null ? "COMPLETED" : "RUNNING");

        // 获取流程定义名称
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(process.getProcessDefinitionId())
                .singleResult();
        progress.put("processName", definition != null ? definition.getName() : "未知流程");

        // 计算任务完成情况
        long totalTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(process.getId())
                .count();

        long completedTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(process.getId())
                .finished()
                .count();

        progress.put("totalTasks", totalTasks);
        progress.put("completedTasks", completedTasks);
        progress.put("completionRate", totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 100);

        return progress;
    }

    /**
     * 获取流程图形化进度
     */
    public Map<String, Object> getGraphicalProgress(String processInstanceId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 首先检查流程是否存在
        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();

        if (historicProcessInstance == null) {
            throw new RuntimeException("流程实例不存在: " + processInstanceId);
        }

        // 获取所有活动节点
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime()
                .asc()
                .list();

        // 构建节点状态
        Map<String, String> nodeStatus = new LinkedHashMap<>();
        for (HistoricActivityInstance activity : activities) {
            String status = activity.getEndTime() != null ? "completed" : "active";
            if ("startEvent".equals(activity.getActivityType())) {
                nodeStatus.put(activity.getActivityId(), "completed");
            } else if ("endEvent".equals(activity.getActivityType())) {
                nodeStatus.put(activity.getActivityId(),
                        activity.getEndTime() != null ? "completed" : "pending");
            } else {
                nodeStatus.put(activity.getActivityId(), status);
            }
        }

        result.put("nodeStatus", nodeStatus);
        result.put("activities", activities.stream()
                .map(this::formatActivityForGraph)
                .collect(Collectors.toList()));
        result.put("processStatus", historicProcessInstance.getEndTime() != null ? "COMPLETED" : "RUNNING");

        return result;
    }

    private Map<String, Object> formatActivityForGraph(HistoricActivityInstance activity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", activity.getActivityId());
        item.put("name", activity.getActivityName());
        item.put("type", activity.getActivityType());
        item.put("startTime", activity.getStartTime());
        item.put("endTime", activity.getEndTime());
        item.put("duration", activity.getDurationInMillis());
        item.put("status", activity.getEndTime() != null ? "completed" : "active");
        return item;
    }
}