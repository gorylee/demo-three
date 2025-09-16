package com.example.demothree.config;

import com.example.demothree.flowable.service.AsyncProcessDeploymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncDeploymentListener implements ApplicationListener<ApplicationReadyEvent> {

    private final AsyncProcessDeploymentService asyncDeploymentService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("🎯 应用启动完成，开始异步部署工作流...");

        // 异步部署所有流程文件
        asyncDeploymentService.deployAllProcessesAsync()
                .thenRun(() -> log.info("🎉 所有工作流异步部署完成"))
                .exceptionally(ex -> {
                    log.error("💥 工作流异步部署异常", ex);
                    return null;
                });

        log.info("⏳ 工作流部署任务已提交到异步线程池，主线程继续执行...");
    }
}