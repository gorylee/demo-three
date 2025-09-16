package com.example.demothree.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 核心线程数
        executor.setMaxPoolSize(5); // 最大线程数
        executor.setQueueCapacity(100); // 队列容量
        executor.setThreadNamePrefix("Async-Deploy-"); // 线程名前缀
        executor.setWaitForTasksToCompleteOnShutdown(true); // 应用关闭时等待任务完成
        executor.setAwaitTerminationSeconds(30); // 等待超时时间
        executor.initialize();
        return executor;
    }
}