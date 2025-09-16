package com.example.demothree.flowable.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncProcessDeploymentService {

    private final RepositoryService repositoryService;
    private final SmartProcessDeploymentService smartProcessDeploymentService;

    /**
     * 异步部署所有BPMN流程文件
     */
    @Async
    public CompletableFuture<Void> deployAllProcessesAsync() {
        StopWatch stopWatch = new StopWatch("Async Process Deployment");
        stopWatch.start();

        try {
            log.info("🚀 开始异步部署BPMN流程文件...");

            // 调用同步部署服务
            smartProcessDeploymentService.deployAllProcessesSmart();

            stopWatch.stop();
            log.info("✅ BPMN流程异步部署完成，耗时: {} ms", stopWatch.getTotalTimeMillis());

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("❌ BPMN流程异步部署失败", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 异步部署单个流程文件
     */
    @Async
    public CompletableFuture<Deployment> deployProcessAsync(String processFileName) {
        log.info("开始异步部署流程文件: {}", processFileName);

        try {
            Deployment deployment = smartProcessDeploymentService.deployProcessSmart(processFileName);
            log.info("✅ 流程文件异步部署成功: {}", processFileName);
            return CompletableFuture.completedFuture(deployment);
        } catch (Exception e) {
            log.error("❌ 流程文件异步部署失败: {}", processFileName, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 带回调的异步部署
     */
    @Async
    public void deployWithCallback(String processFileName, DeploymentCallback callback) {
        try {
            Deployment deployment = smartProcessDeploymentService.deployProcessSmart(processFileName);
            callback.onSuccess(deployment);
        } catch (Exception e) {
            callback.onFailure(processFileName, e);
        }
    }

    /**
     * 部署回调接口
     */
    public interface DeploymentCallback {
        void onSuccess(Deployment deployment);
        void onFailure(String fileName, Exception e);
    }
}