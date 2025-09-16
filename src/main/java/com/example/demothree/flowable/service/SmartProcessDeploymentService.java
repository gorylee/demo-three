package com.example.demothree.flowable.service;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class SmartProcessDeploymentService {

    @Autowired
    private RepositoryService repositoryService;

    private final Map<String, String> processChecksums = new HashMap<>();

    /**
     * 智能部署所有流程定义
     */
    public void deployAllProcessesSmart() throws IOException {
        System.out.println("开始智能部署流程定义...");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/processes/*.bpmn20.xml");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            String processKey = filename.replace(".bpmn20.xml", "");

            try {
                String newChecksum = calculateChecksum(resource.getInputStream());
                String existingChecksum = getExistingChecksum(processKey);

                if (existingChecksum != null && existingChecksum.equals(newChecksum)) {
                    // 内容未改变，跳过部署
                    System.out.println("✅ 流程未改变，跳过部署: " + processKey);
                    continue;
                }

                // 内容改变或首次部署
                Deployment deployment = repositoryService.createDeployment()
                        .addInputStream(filename, resource.getInputStream())
                        .name("智能部署 - " + filename)
                        .key(processKey)
                        .deploy();

                // 更新校验和
                processChecksums.put(processKey, newChecksum);

                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId())
                        .singleResult();

                System.out.println("🚀 部署新版本: " + processDefinition.getKey() +
                        " 版本: " + processDefinition.getVersion() +
                        " (内容已改变)");

            } catch (Exception e) {
                System.err.println("❌ 部署失败: " + filename + " - " + e.getMessage());
            }
        }

        printDeploymentSummary();
    }

    /**
     * 计算文件内容的MD5校验和
     */
    private String calculateChecksum(InputStream inputStream) throws IOException {
        byte[] content = inputStream.readAllBytes();
        return DigestUtils.md5DigestAsHex(content);
    }

    /**
     * 获取已部署流程的校验和
     * 从最新版本中提取或使用其他方式存储
     */
    private String getExistingChecksum(String processKey) throws IOException {
        // 首先检查内存中的缓存
        if (processChecksums.containsKey(processKey)) {
            return processChecksums.get(processKey);
        }

        // 从数据库最新版本获取（这里简化处理，实际可以存储在校注变量中）
        ProcessDefinition latest = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();

        if (latest != null) {
            // 从部署资源中读取内容并计算校验和
            try (InputStream resourceStream = repositoryService.getResourceAsStream(
                    latest.getDeploymentId(),
                    latest.getResourceName())) {

                if (resourceStream != null) {
                    String checksum = calculateChecksum(resourceStream);
                    processChecksums.put(processKey, checksum);
                    return checksum;
                }
            }
        }

        return null; // 没有找到现有版本
    }

    /**
     * 部署单个流程（带内容检查）
     */
    public Deployment deployProcessSmart(String processFileName) throws IOException {
        Resource resource = new PathMatchingResourcePatternResolver()
                .getResource("classpath:/processes/" + processFileName);

        String processKey = processFileName.replace(".bpmn20.xml", "");
        String newChecksum = calculateChecksum(resource.getInputStream());
        String existingChecksum = getExistingChecksum(processKey);

        if (existingChecksum != null && existingChecksum.equals(newChecksum)) {
            System.out.println("✅ 流程内容未改变，跳过部署: " + processKey);
            return null;
        }

        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(processFileName, resource.getInputStream())
                .name("智能部署 - " + processFileName)
                .key(processKey)
                .deploy();

        // 更新校验和
        processChecksums.put(processKey, newChecksum);

        System.out.println("🚀 部署新版本: " + processKey + " (内容已改变)");
        return deployment;
    }

    /**
     * 强制部署（忽略内容检查）
     */
    public Deployment deployProcessForce(String processFileName) throws IOException {
        Resource resource = new PathMatchingResourcePatternResolver()
                .getResource("classpath:/processes/" + processFileName);

        String processKey = processFileName.replace(".bpmn20.xml", "");

        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(processFileName, resource.getInputStream())
                .name("强制部署 - " + processFileName)
                .key(processKey)
                .deploy();

        // 更新校验和
        String newChecksum = calculateChecksum(resource.getInputStream());
        processChecksums.put(processKey, newChecksum);

        System.out.println("🔨 强制部署: " + processKey);
        return deployment;
    }

    /**
     * 打印部署摘要
     */
    private void printDeploymentSummary() {
        System.out.println("\n=== 部署完成 ===");
        System.out.println("内存中缓存的流程校验和: " + processChecksums.size() + " 个");

        repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .list()
                .forEach(pd -> {
                    System.out.printf("流程: %-15s 版本: %d 部署时间: %s%n",
                            pd.getKey(), pd.getVersion(), pd.getDeploymentId());
                });
    }

    /**
     * 清理旧的部署（保留最近N个版本）
     */
    public void cleanupOldDeployments(String processKey, int keepVersions) {
        // 获取所有版本
        var allVersions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion().desc()
                .list();

        if (allVersions.size() > keepVersions) {
            // 删除旧版本（保留最近keepVersions个）
            for (int i = keepVersions; i < allVersions.size(); i++) {
                ProcessDefinition oldVersion = allVersions.get(i);
                repositoryService.deleteDeployment(oldVersion.getDeploymentId(), true);
                System.out.println("🗑️  清理旧版本: " + processKey + " v" + oldVersion.getVersion());
            }
        }
    }

    /**
     * 获取流程内容的校验和
     */
    public String getProcessChecksum(String processKey) {
        return processChecksums.get(processKey);
    }
}