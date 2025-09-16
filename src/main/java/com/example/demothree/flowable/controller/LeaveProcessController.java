package com.example.demothree.flowable.controller;


import cn.hutool.json.JSONUtil;
import com.example.demothree.flowable.dto.TaskDto;
import com.example.demothree.flowable.service.LeaveProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@Slf4j
public class LeaveProcessController {

    private final LeaveProcessService leaveProcessService;

    /**
     * 发起请假申请
     */
    @PostMapping("/apply")
    public ProcessInstance applyForLeave(@RequestParam String applicant,
                                         @RequestBody Map<String, Object> leaveInfo) {
        return leaveProcessService.applyForLeave(applicant, leaveInfo);
    }

    /**
     * 获取用户待办任务
     */
    @GetMapping("/tasks/{userId}")
    public String getUserTasks(@PathVariable String userId) {
        List<Task> userTasks = leaveProcessService.getUserTasks(userId);
        List<TaskDto> taskDtos = new ArrayList<>();
        for (Task task :userTasks) {
            TaskDto taskDto = new TaskDto();
            BeanUtils.copyProperties(task, taskDto);
            taskDtos.add(taskDto);
        }
        log.info("用户待办任务：" + JSONUtil.toJsonStr(taskDtos));
        return JSONUtil.toJsonStr(taskDtos);
    }

    /**
     * 部门经理审批
     */
    @PostMapping("/approve/dept/toApprove")
    public String deptManagerApprove(@RequestParam String taskId,
                                     @RequestParam String comment,
                                     @RequestParam boolean approved) {
        leaveProcessService.deptManagerApprove(taskId, comment, approved);
        return "审批完成";
    }

    /**
     * HR备案
     */
    @PostMapping("/record/hr/{taskId}")
    public String hrRecord(@PathVariable String taskId,
                           @RequestParam String comment) {
        leaveProcessService.hrRecord(taskId, comment);
        return "备案完成";
    }

    /**
     * 总经理审批
     */
    @PostMapping("/approve/gm/{taskId}")
    public String generalManagerApprove(@PathVariable String taskId,
                                        @RequestParam String comment,
                                        @RequestParam boolean approved) {
        leaveProcessService.generalManagerApprove(taskId, comment, approved);
        return "总经理审批完成";
    }

    /**
     * 获取请假申请详情
     */
    @GetMapping("/details/{processInstanceId}")
    public Map<String, Object> getLeaveDetails(@PathVariable String processInstanceId) {
        return leaveProcessService.getLeaveApplicationDetails(processInstanceId);
    }
}