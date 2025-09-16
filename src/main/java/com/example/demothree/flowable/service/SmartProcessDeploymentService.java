package com.example.demothree.flowable.service;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Service
@Slf4j
public class SmartProcessDeploymentService {

    @Autowired
    private RepositoryService repositoryService;

    private final Map<String, String> processChecksums = new HashMap<>();

    /**
     * 智能部署所有流程定义
     */
    public void deployAllProcessesSmart() throws IOException {
        log.info("开始智能部署流程定义...");

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resourcesXml = resolver.getResources("classpath:/processes/*.bpmn20.xml");
        Resource[] resourcesBpmn = resolver.getResources("classpath:/processes/*.bpmn");
        List<Resource> resources = new ArrayList<>();
        resources.addAll(Arrays.asList(resourcesXml));
        resources.addAll(Arrays.asList(resourcesBpmn));

        for (Resource resource : resources) {
            String fullFilename = resource.getFilename();
            String filename =  this.getBeforeFirstDot(fullFilename);

            try (InputStream is = resource.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                if (StrUtil.isEmpty(filename)) {
                    log.error("⚠️ 文件名为空，跳过: " + fullFilename);
                    continue;
                }

                String processKey = extractProcessId(bytes);
                if (StrUtil.isEmpty(processKey)) {
                    log.error("⚠️ 无法从BPMN中解析process id，文件: " + fullFilename + "，跳过。");
                    continue;
                }

                // 将文件名参与校验和，文件名变动也视为变更
                String newChecksum = DigestUtils.md5DigestAsHex(fullFilename.getBytes())
                        + ":" + DigestUtils.md5DigestAsHex(bytes);

                String existingChecksum = getExistingChecksum(processKey);

                if (existingChecksum != null && existingChecksum.equals(newChecksum)) {
                    // 内容未改变，跳过部署
                    log.info("✅ 流程未改变，跳过部署: " + processKey);
                    continue;
                }


                // 内容改变或首次部署
                Deployment deployment = repositoryService.createDeployment()
                        .addBytes(fullFilename, bytes)
                        .name("智能部署 - " + filename)
                        .key(processKey)
                        .deploy();

                // 更新校验和（以process id为索引）
                processChecksums.put(processKey, newChecksum);

                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                        .deploymentId(deployment.getId())
                        .singleResult();

                log.info("🚀 部署新版本: " + processDefinition.getKey() +
                        " 版本: " + processDefinition.getVersion() +
                        " (内容已改变)");

            } catch (Exception e) {
                log.error("❌ 部署失败: " + filename + " - " + e.getMessage());
            }
        }

        printDeploymentSummary();
    }

    private String getBeforeFirstDot(String filename) {
        int dotIndex = filename.indexOf('.');
        if (dotIndex == -1) {
            return filename; // 如果没有点号，返回原字符串
        }
        return filename.substring(0, dotIndex);
    }

    // 已统一使用 calculateCompositeChecksum

    // 与部署时保持一致：联合校验和 = MD5(resourceName) + ":" + MD5(resourceBytes)
    private String calculateCompositeChecksum(String resourceName, InputStream inputStream) throws IOException {
        byte[] content = inputStream.readAllBytes();
        String namePart = DigestUtils.md5DigestAsHex(resourceName.getBytes());
        String contentPart = DigestUtils.md5DigestAsHex(content);
        return namePart + ":" + contentPart;
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
                    String checksum = calculateCompositeChecksum(latest.getResourceName(), resourceStream);
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
        String filename =  this.getBeforeFirstDot(processFileName);
        Deployment deployment = null;
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            if (StrUtil.isEmpty(filename)) {
                log.error("⚠️ 文件名为空，跳过: " + processFileName);
                return null;
            }

            String processKey = extractProcessId(bytes);
            if (StrUtil.isEmpty(processKey)) {
                log.error("⚠️ 无法从BPMN中解析process id，文件: " + processFileName + "，跳过。");
                return null;
            }

            String newChecksum = DigestUtils.md5DigestAsHex(processFileName.getBytes())
                    + ":" + DigestUtils.md5DigestAsHex(bytes);

            String existingChecksum = getExistingChecksum(processKey);

            if (existingChecksum != null && existingChecksum.equals(newChecksum)) {
                // 内容未改变，跳过部署
                log.info("✅ 流程未改变，跳过部署: " + processKey);
                return null;
            }


            // 内容改变或首次部署
            deployment = repositoryService.createDeployment()
                    .addBytes(processFileName, bytes)
                    .name("智能部署 - " + filename)
                    .key(processKey)
                    .deploy();

            // 更新校验和
            processChecksums.put(processKey, newChecksum);

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();

            log.info("🚀 部署新版本: " + processDefinition.getKey() +
                    " 版本: " + processDefinition.getVersion() +
                    " (内容已改变)");

        } catch (Exception e) {
            log.error("❌ 部署失败: " + filename + " - " + e.getMessage());
        }
        return deployment;
    }

    /**
     * 强制部署（忽略内容检查）
     */
    public Deployment deployProcessForce(String processFileName) throws IOException {
        Resource resource = new PathMatchingResourcePatternResolver()
                .getResource("classpath:/processes/" + processFileName);

        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String processKey = extractProcessId(bytes);
            if (StrUtil.isEmpty(processKey)) {
                log.error("⚠️ 无法从BPMN中解析process id，文件: " + processFileName + "，跳过。");
                return null;
            }

            Deployment deployment = repositoryService.createDeployment()
                    .addBytes(processFileName, bytes)
                    .name("强制部署 - " + processFileName)
                    .key(processKey)
                    .deploy();

            // 更新校验和
            String newChecksum = DigestUtils.md5DigestAsHex((processFileName + "|#|_").getBytes())
                    + ":" + DigestUtils.md5DigestAsHex(bytes);
            processChecksums.put(processKey, newChecksum);

            log.info("🔨 强制部署: " + processKey);
            return deployment;
        }
    }

    /**
     * 打印部署摘要
     */
    private void printDeploymentSummary() {
        log.info("\n=== 部署完成 ===");
        log.info("内存中缓存的流程校验和: " + processChecksums.size() + " 个");

        repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .list()
                .forEach(pd -> {
                    log.info("流程: {} 版本: {}",pd.getKey(), pd.getVersion());
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
                log.info("🗑️  清理旧版本: " + processKey + " v" + oldVersion.getVersion());
            }
        }
    }

    /**
     * 获取流程内容的校验和
     */
    public String getProcessChecksum(String processKey) {
        return processChecksums.get(processKey);
    }

    // 从BPMN XML字节中解析第一个process的id，忽略命名空间
    private String extractProcessId(byte[] xmlBytes) {
        try (InputStream in = new java.io.ByteArrayInputStream(xmlBytes)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().parse(in);

            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xPath.compile("/*[local-name()='definitions']/*[local-name()='process']/@id");
            Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            return node == null ? null : node.getNodeValue();
        } catch (Exception e) {
            log.error("解析BPMN process id失败: " + e.getMessage());
            return null;
        }
    }

}