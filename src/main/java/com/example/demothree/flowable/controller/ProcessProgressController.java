package com.example.demothree.flowable.controller;

import com.example.demothree.flowable.service.ProcessProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/process/progress")
@RequiredArgsConstructor
public class ProcessProgressController {

    private final ProcessProgressService progressService;

    /**
     * 获取流程进度详情
     */
    @GetMapping("/{processInstanceId}")
    public ResponseEntity<?> getProcessProgress(@PathVariable String processInstanceId) {
        try {
            Map<String, Object> progress = progressService.getProcessProgress(processInstanceId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

    /**
     * 获取用户参与的所有流程进度
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserProcessProgress(@PathVariable String userId) {
        try {
            List<Map<String, Object>> progressList = progressService.getUserProcessProgress(userId);
            return ResponseEntity.ok(progressList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "获取用户流程进度失败",
                    "success", false
            ));
        }
    }

    /**
     * 获取流程图形化进度
     */
    @GetMapping("/graphical/{processInstanceId}")
    public ResponseEntity<?> getGraphicalProgress(@PathVariable String processInstanceId) {
        try {
            Map<String, Object> progress = progressService.getGraphicalProgress(processInstanceId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

}