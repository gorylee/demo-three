package com.example.demothree.flowable;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author GoryLee
 * @Date 2025/9/16
 */
@SpringBootTest
public class FlowableApplicationTests {
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Test
    void testLeaveProcess() {
        // 启动流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveReason", "年度休假");
        variables.put("leaveDays", 5);
        variables.put("startDate", "2024-01-15");
        variables.put("endDate", "2024-01-19");

        var processInstance = runtimeService.startProcessInstanceByKey("", variables);
    }
}
