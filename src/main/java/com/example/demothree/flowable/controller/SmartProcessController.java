package com.example.demothree.flowable.controller;

import com.example.demothree.flowable.service.SmartProcessDeploymentService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process")
public class SmartProcessController {

    @Autowired
    private SmartProcessDeploymentService smartDeploymentService;

    /**
     * 智能部署所有流程
     */
    @PostMapping("/deploy/smart")
    public Map<String, Object> deployAllSmart() {
        Map<String, Object> result = new HashMap<>();
        try {
            smartDeploymentService.deployAllProcessesSmart();
            result.put("success", true);
            result.put("message", "智能部署完成");
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "部署失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 智能部署单个流程
     */
    @PostMapping("/deploy/smart/{fileName}")
    public Map<String, Object> deploySmart(@PathVariable String fileName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Deployment deployment = smartDeploymentService.deployProcessSmart(fileName);
            if (deployment == null) {
                result.put("success", true);
                result.put("message", "流程内容未改变，跳过部署");
                result.put("deployed", false);
            } else {
                result.put("success", true);
                result.put("message", "部署成功");
                result.put("deployed", true);
                result.put("deploymentId", deployment.getId());
            }
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "部署失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 强制部署单个流程（忽略内容检查）
     */
    @PostMapping("/deploy/force/{fileName}")
    public Map<String, Object> deployForce(@PathVariable String fileName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Deployment deployment = smartDeploymentService.deployProcessForce(fileName);
            result.put("success", true);
            result.put("message", "强制部署成功");
            result.put("deploymentId", deployment.getId());
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "部署失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 清理旧版本
     */
    @PostMapping("/cleanup/{processKey}")
    public Map<String, Object> cleanupOldVersions(@PathVariable String processKey,
                                                  @RequestParam(defaultValue = "3") int keepVersions) {
        Map<String, Object> result = new HashMap<>();
        smartDeploymentService.cleanupOldDeployments(processKey, keepVersions);
        result.put("success", true);
        result.put("message", "清理完成，保留最近 " + keepVersions + " 个版本");
        return result;
    }

    /**
     * 获取流程校验和信息
     */
    @GetMapping("/checksum/{processKey}")
    public Map<String, Object> getChecksum(@PathVariable String processKey) {
        Map<String, Object> result = new HashMap<>();
        String checksum = smartDeploymentService.getProcessChecksum(processKey);
        result.put("processKey", processKey);
        result.put("checksum", checksum);
        result.put("exists", checksum != null);
        return result;
    }
}