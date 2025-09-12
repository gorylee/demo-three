package com.example.demothree.mybatisUtils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 代码生成器测试类
 * 
 * @author GoryLee
 * @since 2024-01-01
 */
@Slf4j
@SpringBootTest
public class CodeGeneratorTest {

    @Autowired
    private CodeGenerator codeGenerator;

    @Autowired
    private GeneratorProperties generatorProperties;

    /**
     * 测试配置加载
     */
    @Test
    public void testConfigLoad() {
        log.info("测试配置加载");
        log.info("作者: {}", generatorProperties.getGlobal().getAuthor());
        log.info("输出目录: {}", generatorProperties.getGlobal().getOutputDir());
        log.info("父包名: {}", generatorProperties.getPackageConfig().getParent());
        log.info("模块名: {}", generatorProperties.getPackageConfig().getModuleName());
    }

    /**
     * 测试代码生成（需要数据库连接）
     * 注意：此测试需要真实的数据库连接，请根据实际情况修改表名
     */
    // @Test
    public void testGenerateCode() {
        try {
            log.info("开始测试代码生成");
            // 请根据实际数据库表名修改
            codeGenerator.generateByTableName("test_table");
            log.info("代码生成测试完成");
        } catch (Exception e) {
            log.error("代码生成测试失败", e);
        }
    }
}
